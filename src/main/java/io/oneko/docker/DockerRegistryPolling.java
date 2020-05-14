package io.oneko.docker;

import static io.oneko.deployable.DeploymentBehaviour.*;
import static io.oneko.kubernetes.deployments.DesiredState.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import io.oneko.docker.event.NewProjectVersionFoundEvent;
import io.oneko.docker.event.ObsoleteProjectVersionRemovedEvent;
import io.oneko.docker.v2.DockerRegistryV2Client;
import io.oneko.docker.v2.DockerRegistryV2ClientFactory;
import io.oneko.docker.v2.model.manifest.Manifest;
import io.oneko.event.Event;
import io.oneko.event.EventDispatcher;
import io.oneko.event.EventTrigger;
import io.oneko.event.ScheduledTask;
import io.oneko.kubernetes.KubernetesDeploymentManager;
import io.oneko.project.Project;
import io.oneko.project.ProjectRepository;
import io.oneko.project.ProjectVersion;
import io.oneko.projectmesh.MeshComponent;
import io.oneko.projectmesh.ProjectMesh;
import io.oneko.projectmesh.ProjectMeshRepository;
import io.oneko.util.ExpiringBucket;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Component
@Slf4j
class DockerRegistryPolling {

	private final ProjectRepository projectRepository;
	private final ProjectMeshRepository projectMeshRepository;
	private final DockerRegistryV2ClientFactory dockerRegistryV2ClientFactory;
	private final KubernetesDeploymentManager kubernetesDeploymentManager;
	private final EventDispatcher eventDispatcher;
	private final Duration pollingTimeoutDuration;
	private final EventTrigger asTrigger;
	private final AtomicBoolean pollingInProgress = new AtomicBoolean(false);
	private final ExpiringBucket<UUID> failedManifestRequests = new ExpiringBucket<UUID>(Duration.ofMinutes(5)).concurrent();

	private DockerRegistryPollingJob currentPollingJob;

	DockerRegistryPolling(ProjectRepository projectRepository,
						  ProjectMeshRepository projectMeshRepository,
						  DockerRegistryV2ClientFactory dockerRegistryV2ClientFactory,
						  KubernetesDeploymentManager kubernetesDeploymentManager,
						  EventDispatcher eventDispatcher,
						  @Value("${o-neko.docker.polling.timeoutInMinutes:5}") int pollingTimeoutInMinutes) {
		this.projectRepository = projectRepository;
		this.projectMeshRepository = projectMeshRepository;
		this.dockerRegistryV2ClientFactory = dockerRegistryV2ClientFactory;
		this.kubernetesDeploymentManager = kubernetesDeploymentManager;
		this.eventDispatcher = eventDispatcher;
		this.pollingTimeoutDuration = Duration.ofMinutes(pollingTimeoutInMinutes);
		this.asTrigger = new ScheduledTask("Docker Registry Polling");
	}

	@Scheduled(fixedDelay = 20000, initialDelay = 20000)
	protected void checkDockerForNewImages() {
		if (!this.pollingInProgress.getAndSet(true)) {
				startPollingJob();
			} else if (this.currentPollingJob != null && (this.currentPollingJob.shouldCancel() || this.currentPollingJob.isCancelled())) {
				cancelCurrentPollingJob();
			} else {
				log.debug("Skipping job because another polling job is still running.");
			}
	}

	private void cancelCurrentPollingJob() {
		log.info("The job timed out. Cancelling it.");
		this.currentPollingJob.cancel();
		this.currentPollingJob = null;
		setPollingJobToNotRunning();
	}

	private void startPollingJob() {
		log.trace("Starting polling job, Memory usage: {}", FileUtils.byteCountToDisplaySize(Runtime.getRuntime().totalMemory()));
		final Mono<List<ProjectMesh>> meshes = projectMeshRepository.getAll().collectList();
		final Mono<List<Project>> projects = projectRepository.getAll().collectList();

		Disposable dockerRegistryPollingJob = meshes.zipWith(projects)
				.flatMap(pair -> this.checkDockerForNewImages(pair.getT1(), pair.getT2()))
				.doOnTerminate(() -> log.trace("Finished polling job")).subscribe();
		this.currentPollingJob = new DockerRegistryPollingJob(dockerRegistryPollingJob).withTimeoutDuration(pollingTimeoutDuration);
	}

	private void setPollingJobToNotRunning() {
		this.pollingInProgress.set(false);
	}

	/**
	 * Iterates through all project versions checking for updates. Alongside handles all mesh components using these
	 * versions images alongside. Triggers re-deployments if necessary.
	 */
	private Mono<Void> checkDockerForNewImages(List<ProjectMesh> allMeshes, List<Project> allProjects) {
		Flux<Project> updatedProjectVersions = Flux.fromIterable(allProjects)
				.flatMap(this::updateProjectVersions);

		return updatedProjectVersions
				.onErrorContinue((ex, o) -> log.error("Encountered an exception while checking new project versions of {}", o, ex))
				.flatMap(this::fetchMissingDatesForImages)
				.onErrorContinue((ex, o) -> log.error("Encountered an exception while fetching the dates of the images {}", o, ex))
				.flatMap(project -> checkForNewImages(project, allMeshes))
				.onErrorContinue((e, o) -> log.error("Error on checking for new images for projects and meshes.", e))
				//finally we have to save the project meshes, that's some awkward mapping here
				.thenMany(Flux.fromIterable(allMeshes))
				.flatMap(this.projectMeshRepository::add)
				.doOnTerminate(this::setPollingJobToNotRunning)
				.subscriberContext(Context.of(EventTrigger.class, this.asTrigger))
				.then();
	}

	private Mono<Project> fetchMissingDatesForImages(Project project) {
		final List<ProjectVersion> versionsWithoutDate = project.getVersions().stream()
				.filter(version -> version.getImageUpdatedDate() == null)
				.filter(version -> !failedManifestRequests.contains(version.getUuid()))
				.collect(Collectors.toList());

		if (versionsWithoutDate.isEmpty()) {
			return Mono.just(project);
		}

		log.trace("Updating dates for {} versions of project {}", versionsWithoutDate.size(), project.getName());
		return Flux.fromIterable(versionsWithoutDate)
				.flatMap(version -> dockerRegistryV2ClientFactory.getDockerRegistryClient(project)
						.flatMap(client -> client.getManifest(version))
						.onErrorReturn(HttpClientErrorException.class, new Manifest() /* I cannot put null in here... */)
						.map(manifest -> {
							if (manifest.getImageUpdatedDate().isPresent()) {
								version.setImageUpdatedDate(manifest.getImageUpdatedDate().orElse(null));
								log.trace("Setting date for version {} of project {} to {}.", version.getName(), project.getName(), version.getImageUpdatedDate());
							} else {
								log.trace("Failed to get Manifest for version {} of project {}.", version.getName(), project.getName());
								failedManifestRequests.add(version.getUuid());
							}
							return version;
						}))
				.collectList()
				.flatMap(list -> projectRepository.add(project));
	}

	private Mono<Project> updateProjectVersions(Project project) {
		log.trace("Checking for new versions of project {}", project.getName());
		final Mono<DockerRegistryV2Client> projectDockerClient = dockerRegistryV2ClientFactory.getDockerRegistryClient(project);
		return projectDockerClient
				.flatMap(client -> client.getAllTags(project))
				.doOnNext(tags -> log.trace("Found {} tags for project {}", tags.size(), project.getName()))
				.flatMap(tags -> {
					ImmutableList<ProjectVersion> versions = project.getVersions();

					Set<String> knownVersions = versions.stream()
							.map(ProjectVersion::getName)
							.collect(Collectors.toSet());

					Set<String> newVersions = Sets.difference(Sets.newHashSet(tags), knownVersions);
					Set<String> removedVersions = Sets.difference(knownVersions, Sets.newHashSet(tags));
					List<Event> resultingEvents = new ArrayList<>();

					if (!newVersions.isEmpty()) {
						if (newVersions.size() == 1) {
							log.info("Found new version {} for project {}", newVersions.toArray()[0], project.getName());
						} else {
							log.info("Found {} new versions for project {}, {}", newVersions.size(), project.getName(), newVersions);
						}
					}

					newVersions.forEach(version -> {
						ProjectVersion projectVersion = project.createVersion(version);
						resultingEvents.add(new NewProjectVersionFoundEvent(projectVersion, this.asTrigger));
					});

					removedVersions.forEach(version -> {
						ProjectVersion projectVersion = project.removeVersion(version);
						log.info("Found an obsolete version {} [{}] for project {}", version, projectVersion.getId(), project.getName());
						resultingEvents.add(new ObsoleteProjectVersionRemovedEvent(projectVersion, this.asTrigger));
					});

					if (newVersions.size() > 0 || removedVersions.size() > 0) {
						Mono<Project> projectMono = projectRepository.add(project)
								.subscriberContext(Context.of(EventTrigger.class, this.asTrigger));

						return projectMono.doOnSuccess((p) ->
								resultingEvents.forEach(eventDispatcher::dispatch));
					}

					return Mono.just(project);
				});
	}

	/**
	 * Checks for new images in this projects and all mesh components that might require this image.
	 * Returns the project meshes after wards since they should not be saved during each project iteration.
	 */
	private Flux<?> checkForNewImages(Project project, Collection<ProjectMesh> allMeshes) {
		return Flux.fromIterable(project.getVersions())
				.flatMap(version -> {
					final List<MeshComponent> allComponentsUsingVersion = allMeshes.stream()
							.flatMap(mesh -> mesh.getComponents().stream())
							.filter(component -> component.getProjectVersion().equals(version))
							.collect(Collectors.toList());

					if (version.getDesiredState() == NotDeployed && allComponentsUsingVersion.isEmpty() || version.getDesiredState() == Deployed && version.getDeploymentBehaviour() != automatically) {
						//nothing to do here...
						return Mono.empty();
					}

					return dockerRegistryV2ClientFactory.getDockerRegistryClient(project)
							.flatMap(client -> client.getManifest(version))
							.onErrorResume(e -> {
								log.trace("Failed to get Manifest for version {} of project {}.", version.getName(), project.getName(), e);
								failedManifestRequests.add(version.getUuid());
								return Mono.just(new Manifest());
							}).flatMap(manifest -> {
								if (!StringUtils.isBlank(manifest.getDockerContentDigest())) {
									return this.redeployAllByManifest(manifest, version, allComponentsUsingVersion);
								} else {
									return Mono.empty();
								}
							});
				})
				.thenMany(Flux.fromIterable(allMeshes));
	}

	/**
	 * Tries to redeploy a project version and the depentend mesh components but considers the desired state of each component and the version.
	 */
	private Mono<?> redeployAllByManifest(Manifest manifest, ProjectVersion version, Collection<MeshComponent> components) {
		List<Mono<?>> dependingMonos = new ArrayList<>();
		for (MeshComponent component : components) {
			if (component.getDesiredState() == Deployed && component.getOwner().getDeploymentBehaviour() == automatically) {
				dependingMonos.add(this.redeployComponentByManifest(manifest, component));
			}
		}
		String digest = manifest.getDockerContentDigest();
		if (!StringUtils.equals(version.getDockerContentDigest(), digest)) {
			log.info("Found a new image '{}' for project '{}' version '{}'", digest, version.getProject().getName(), version.getName());
			version.setDockerContentDigest(digest);
			version.setImageUpdatedDate(manifest.getImageUpdatedDate().orElse(null));
			dependingMonos.add(this.redeployAndSaveVersion(version).switchIfEmpty(Mono.just(version)));
		}
		return Flux.concat(dependingMonos).collectList();
	}

	private Mono<ProjectVersion> redeployAndSaveVersion(ProjectVersion version) {
		if (version.getDesiredState() == Deployed && version.getDeploymentBehaviour() == automatically) {
			return projectRepository.add(version.getProject())
					.then(kubernetesDeploymentManager.deploy(version))
					.subscriberContext(Context.of(EventTrigger.class, this.asTrigger));
		} else {
			return Mono.empty();
		}
	}

	private Mono<MeshComponent> redeployComponentByManifest(Manifest manifest, MeshComponent component) {
		String digest = manifest.getDockerContentDigest();
		if (!StringUtils.equals(component.getDockerContentDigest(), digest)) {
			log.info("Found a new image '{}' for component '{}' of mesh '{}'", digest, component.getName(), component.getOwner().getName());
			component.setDockerContentDigest(digest);
			return kubernetesDeploymentManager.deploy(component)
					.subscriberContext(Context.of(EventTrigger.class, this.asTrigger))
					.map(dontCare -> component);
		} else {
			return Mono.empty();
		}
	}

}
