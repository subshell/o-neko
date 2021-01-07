package io.oneko.docker;

import static io.oneko.deployable.DeploymentBehaviour.*;
import static io.oneko.kubernetes.deployments.DesiredState.*;
import static io.oneko.util.DurationUtils.*;
import static org.apache.commons.lang3.time.DurationFormatUtils.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import io.oneko.docker.event.NewProjectVersionFoundEvent;
import io.oneko.docker.event.ObsoleteProjectVersionRemovedEvent;
import io.oneko.docker.v2.DockerRegistryClientFactory;
import io.oneko.docker.v2.model.manifest.Manifest;
import io.oneko.domain.Identifiable;
import io.oneko.event.CurrentEventTrigger;
import io.oneko.event.Event;
import io.oneko.event.EventDispatcher;
import io.oneko.event.EventTrigger;
import io.oneko.event.ScheduledTask;
import io.oneko.kubernetes.KubernetesDeploymentManager;
import io.oneko.project.ProjectRepository;
import io.oneko.project.ProjectVersion;
import io.oneko.project.ReadableProject;
import io.oneko.project.ReadableProjectVersion;
import io.oneko.project.WritableProject;
import io.oneko.project.WritableProjectVersion;
import io.oneko.projectmesh.ProjectMeshRepository;
import io.oneko.projectmesh.ReadableProjectMesh;
import io.oneko.projectmesh.WritableMeshComponent;
import io.oneko.projectmesh.WritableProjectMesh;
import io.oneko.util.ExpiringBucket;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
class DockerRegistryPolling {

	@Data
	private static class VersionWithDockerManifest {
		private final WritableProjectVersion version;
		private final Manifest manifest;
	}

	private final ProjectRepository projectRepository;
	private final ProjectMeshRepository projectMeshRepository;
	private final DockerRegistryClientFactory dockerRegistryClientFactory;
	private final KubernetesDeploymentManager kubernetesDeploymentManager;
	private final EventDispatcher eventDispatcher;
	private final EventTrigger asTrigger;
	private final ExpiringBucket<UUID> failedManifestRequests = new ExpiringBucket<UUID>(Duration.ofMinutes(5)).concurrent();
	private final CurrentEventTrigger currentEventTrigger;

	DockerRegistryPolling(ProjectRepository projectRepository,
												ProjectMeshRepository projectMeshRepository,
												DockerRegistryClientFactory dockerRegistryClientFactory,
												KubernetesDeploymentManager kubernetesDeploymentManager,
												EventDispatcher eventDispatcher,
												CurrentEventTrigger currentEventTrigger) {
		this.projectRepository = projectRepository;
		this.projectMeshRepository = projectMeshRepository;
		this.dockerRegistryClientFactory = dockerRegistryClientFactory;
		this.kubernetesDeploymentManager = kubernetesDeploymentManager;
		this.eventDispatcher = eventDispatcher;
		this.currentEventTrigger = currentEventTrigger;
		this.asTrigger = new ScheduledTask("Docker Registry Polling");
	}

	@Scheduled(fixedDelay = 20000, initialDelay = 20000)
	protected void checkDockerForNewImages() {
		try (var ignored = currentEventTrigger.forTryBlock(this.asTrigger)) {
			final Instant start = Instant.now();
			log.trace("Starting polling job");
			final List<WritableProjectMesh> meshes = projectMeshRepository.getAll().stream()
					.map(ReadableProjectMesh::writable)
					.collect(Collectors.toList());
			final List<WritableProject> projects = projectRepository.getAll().stream()
					.map(ReadableProject::writable)
					.collect(Collectors.toList());

			checkDockerForNewImages(meshes, projects);
			final Instant stop = Instant.now();

			final Duration duration = Duration.between(start, stop);
			final Duration warnIfLongerThanThis = Duration.ofMinutes(5);
			if (isLongerThan(duration, warnIfLongerThanThis)) {
				log.warn("Checking for new images took longer than {} (took {}) [HH:mm:ss.SSS]", formatDurationHMS(warnIfLongerThanThis.toMillis()), formatDurationHMS(duration.toMillis()));
			}

			log.trace("Finished polling job");
		}
	}

	/**
	 * Iterates through all project versions checking for updates. Alongside handles all mesh components using these
	 * versions images alongside. Triggers re-deployments if necessary.
	 */
	private void checkDockerForNewImages(List<WritableProjectMesh> allMeshes, List<WritableProject> allProjects) {
		for (var project : allProjects) {
			try {
				project = this.updateProjectVersions(project);
			} catch (Exception e) {
				log.error("Encountered an exception while checking new project versions of {}", project.getName(), e);
			}

			try {
				project = this.fetchMissingDatesForImages(project);
			} catch (Exception e) {
				log.error("Encountered an exception while fetching the dates of the images {} {}", project.getName(), e);
			}

			try {
				checkForNewImages(project, allMeshes);
			} catch (Exception e) {
				log.error("Error on checking for new images for projects and meshes.", e);
			}
		}

		//finally we have to save the project meshes, that's some awkward mapping here
		allMeshes.forEach(projectMeshRepository::add);
	}


	private WritableProject fetchMissingDatesForImages(WritableProject project) {
		final List<WritableProjectVersion> versionsWithoutDate = project.getVersions().stream()
				.filter(version -> version.getImageUpdatedDate() == null)
				.filter(version -> !failedManifestRequests.contains(version.getUuid()))
				.collect(Collectors.toList());

		if (versionsWithoutDate.isEmpty()) {
			return project;
		}

		log.trace("Updating dates for {} versions of project {}", versionsWithoutDate.size(), project.getName());
		final var versionWithDockerManifestList = versionsWithoutDate.stream()
				.map(version -> getManifestWithContext(project, version))
				.collect(Collectors.toList());

		for (var versionWithDockerManifest : versionWithDockerManifestList) {
			final var manifest = versionWithDockerManifest.getManifest();
			final var version = versionWithDockerManifest.getVersion();

			if (manifest == null || manifest.getImageUpdatedDate().isEmpty()) {
				log.trace("Failed to get Manifest for version {} of project {}.", version.getName(), project.getName());
				failedManifestRequests.add(version.getUuid());
				continue;
			}

			version.setImageUpdatedDate(manifest.getImageUpdatedDate().orElse(null));
			log.trace("Setting date for version {} of project {} to {}.", version.getName(), project.getName(), version.getImageUpdatedDate());
		}

		return projectRepository.add(project).writable();
	}

	private VersionWithDockerManifest getManifestWithContext(WritableProject project, WritableProjectVersion version) {
		try {
			return dockerRegistryClientFactory.getDockerRegistryClient(project)
					.map(client -> new VersionWithDockerManifest(version, client.getManifest(version)))
					.orElseGet(() -> new VersionWithDockerManifest(version, null));
		} catch (Exception e) {
			log.error("Failed to retrieve manifest", e);
			return new VersionWithDockerManifest(version, null);
		}
	}

	private WritableProject updateProjectVersions(WritableProject project) {
		log.trace("Checking for new versions of project {}", project.getName());
		final var dockerClient = dockerRegistryClientFactory.getDockerRegistryClient(project)
				.orElseThrow(() -> new RuntimeException(String.format("Project %s has no docker client registry", project.getName())));

		final var tags = dockerClient.getAllTags(project);
		log.trace("Found {} tags for project {}", tags.size(), project.getName());

		return manageAvailableVersions(project, tags);
	}

	private WritableProject manageAvailableVersions(WritableProject project, List<String> tags) {
		final var versions = project.getVersions().stream()
				.map(ProjectVersion::getName)
				.collect(Collectors.toSet());

		final var newVersions = Sets.difference(Sets.newHashSet(tags), versions);
		final var removedVersions = Sets.difference(versions, Sets.newHashSet(tags));
		final var resultingEvents = new ArrayList<Event>();

		if (!newVersions.isEmpty()) {
			if (newVersions.size() == 1) {
				log.info("Found new version {} for project {}", newVersions.toArray()[0], project.getName());
			} else {
				log.info("Found {} new versions for project {}, {}", newVersions.size(), project.getName(), newVersions);
			}
		}

		newVersions.forEach(version -> {
			WritableProjectVersion projectVersion = project.createVersion(version);
			resultingEvents.add(new NewProjectVersionFoundEvent(projectVersion));
		});

		removedVersions.forEach(version -> {
			WritableProjectVersion projectVersion = project.removeVersion(version);
			log.info("Found an obsolete version {} [{}] for project {}", version, projectVersion.getId(), project.getName());
			resultingEvents.add(new ObsoleteProjectVersionRemovedEvent(projectVersion));
		});

		if (!newVersions.isEmpty() || !removedVersions.isEmpty()) {
			ReadableProject savedProject = projectRepository.add(project);
			resultingEvents.forEach(eventDispatcher::dispatch);
			return savedProject.writable();
		}

		return project;
	}

	/**
	 * Checks for new images in this projects and all mesh components that might require this image.
	 * Returns the project meshes after wards since they should not be saved during each project iteration.
	 */
	private void checkForNewImages(WritableProject project, Collection<WritableProjectMesh> allMeshes) {
		project.getVersions().forEach(version -> {
			final List<WritableMeshComponent> allComponentsUsingVersion = allMeshes.stream()
					.flatMap(mesh -> mesh.getComponents().stream())
					.filter(component -> component.getProjectVersionId().equals(version.getId()))
					.collect(Collectors.toList());

			if (version.getDesiredState() == NotDeployed && allComponentsUsingVersion.isEmpty()
					|| version.getDesiredState() == Deployed && version.getDeploymentBehaviour() != automatically) {
				//nothing to do here...
				return;
			}

			final var manifestWithContext = getManifestWithContext(project, version);
			if (manifestWithContext.getManifest() == null) {
				log.trace("Failed to get Manifest for version {} of project {}.", version.getName(), project.getName());
				failedManifestRequests.add(version.getUuid());
				return;
			}

			if (!StringUtils.isBlank(manifestWithContext.getManifest().getDockerContentDigest())) {
				this.redeployAllByManifest(manifestWithContext.getManifest(), version, allComponentsUsingVersion);
			}
		});
	}

	/**
	 * Tries to redeploy a project version and the dependent mesh components but considers the desired state of each component and the version.
	 */
	private List<Identifiable> redeployAllByManifest(Manifest manifest, WritableProjectVersion version, Collection<WritableMeshComponent> components) {
		List<Identifiable> depending = new ArrayList<>();
		for (WritableMeshComponent component : components) {
			if (component.getDesiredState() == Deployed && component.getOwner().getDeploymentBehaviour() == automatically) {
				this.redeployComponentByManifest(manifest, component).ifPresent(depending::add);
			}
		}

		String digest = manifest.getDockerContentDigest();
		if (!StringUtils.equals(version.getDockerContentDigest(), digest)) {
			log.info("Found a new image '{}' for project '{}' version '{}'", digest, version.getProject().getName(), version.getName());
			version.setDockerContentDigest(digest);
			version.setImageUpdatedDate(manifest.getImageUpdatedDate().orElse(null));
			this.redeployAndSaveVersion(version).ifPresent(depending::add);
		}

		return depending;
	}

	private Optional<ReadableProjectVersion> redeployAndSaveVersion(WritableProjectVersion version) {
		if (version.getDesiredState() == Deployed && version.getDeploymentBehaviour() == automatically) {
			return Optional.of(kubernetesDeploymentManager.deploy(version));
		}

		return Optional.empty();
	}

	private Optional<WritableMeshComponent> redeployComponentByManifest(Manifest manifest, WritableMeshComponent component) {
		String digest = manifest.getDockerContentDigest();
		if (!StringUtils.equals(component.getDockerContentDigest(), digest)) {
			log.info("Found a new image '{}' for component '{}' of mesh '{}'", digest, component.getName(), component.getOwner().getName());
			component.setDockerContentDigest(digest);

			kubernetesDeploymentManager.deploy(component);
			return Optional.of(component);
		}

		return Optional.empty();
	}

}
