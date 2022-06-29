package io.oneko.docker;

import static io.oneko.deployable.DeploymentBehaviour.automatically;
import static io.oneko.kubernetes.deployments.DesiredState.Deployed;
import static io.oneko.kubernetes.deployments.DesiredState.NotDeployed;
import static io.oneko.util.DurationUtils.isLongerThan;
import static io.oneko.util.MoreStructuredArguments.IMAGE_UPDATED_DATE_KEY;
import static io.oneko.util.MoreStructuredArguments.projectKv;
import static io.oneko.util.MoreStructuredArguments.versionKv;
import static net.logstash.logback.argument.StructuredArguments.kv;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import io.oneko.docker.event.NewProjectVersionFoundEvent;
import io.oneko.docker.event.ObsoleteProjectVersionRemovedEvent;
import io.oneko.docker.v2.DockerRegistryClientFactory;
import io.oneko.docker.v2.model.manifest.Manifest;
import io.oneko.event.CurrentEventTrigger;
import io.oneko.event.Event;
import io.oneko.event.EventDispatcher;
import io.oneko.event.EventTrigger;
import io.oneko.event.ScheduledTask;
import io.oneko.kubernetes.DeploymentManager;
import io.oneko.project.ProjectRepository;
import io.oneko.project.ProjectVersion;
import io.oneko.project.ReadableProject;
import io.oneko.project.WritableProject;
import io.oneko.project.WritableProjectVersion;
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

	private final Duration MAX_IMAGE_CHECK_DURATION = Duration.ofMinutes(5);
	private final Duration MAX_UPDATE_DATES_DURATION = Duration.ofMinutes(10);

	private final ProjectRepository projectRepository;
	private final DockerRegistryClientFactory dockerRegistryClientFactory;
	private final DeploymentManager deploymentManager;
	private final EventDispatcher eventDispatcher;
	private final EventTrigger asTrigger;
	private final ExpiringBucket<UUID> failedManifestRequests = new ExpiringBucket<UUID>(Duration.ofMinutes(5)).concurrent();
	private final CurrentEventTrigger currentEventTrigger;

	DockerRegistryPolling(ProjectRepository projectRepository,
												DockerRegistryClientFactory dockerRegistryClientFactory,
												DeploymentManager deploymentManager,
												EventDispatcher eventDispatcher,
												CurrentEventTrigger currentEventTrigger) {
		this.projectRepository = projectRepository;
		this.dockerRegistryClientFactory = dockerRegistryClientFactory;
		this.deploymentManager = deploymentManager;
		this.eventDispatcher = eventDispatcher;
		this.currentEventTrigger = currentEventTrigger;
		this.asTrigger = new ScheduledTask("Docker Registry Polling");
	}

	@Scheduled(fixedDelay = 20000, initialDelay = 10000)
	protected void updateAndRedeployAllIfRequired() {
		try (var ignored = currentEventTrigger.forTryBlock(this.asTrigger)) {
			log.trace("starting polling job");

			final StopWatch stopWatch = new StopWatch();
			stopWatch.start();

			final List<WritableProject> projects = projectRepository.getAll().stream()
					.map(ReadableProject::writable)
					.collect(Collectors.toList());

			updateAndRedeployIfRequired(projects);
			stopWatch.stop();

			if (isLongerThan(Duration.ofMillis(stopWatch.getTime()), MAX_IMAGE_CHECK_DURATION)) {
				log.warn("checking for new images took longer than expected ({}, {})",
						kv("threshold_millis", MAX_IMAGE_CHECK_DURATION.toMillis()),
						kv("duration_millis", stopWatch.getTime()));
			}

			log.trace("finished polling job ({})", kv("duration_millis", stopWatch.getTime()));
		}
	}

	@Scheduled(fixedDelay = 1000 * 60 * 60 * 2, initialDelay = 60000) // Every 2 hours
	protected void updateDatesForAllImagesAndAllTags() {
		log.trace("updating dates for all projects and all versions");

		final StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		projectRepository.getAll()
				.stream()
				.map(ReadableProject::writable)
				.forEach(project -> fetchAndUpdateDatesForVersionsOfProject(project, project.getVersions()));
		stopWatch.stop();

		if (isLongerThan(Duration.ofMillis(stopWatch.getTime()), MAX_UPDATE_DATES_DURATION)) {
			log.warn("updating dates for all projects and all versions took longer than expected ({}, {})", kv("threshold_millis",
					MAX_UPDATE_DATES_DURATION.toMillis()),
					kv("duration_millis", stopWatch.getTime()));
		}

		log.trace("finished updating dates for all projects ({})", kv("duration_millis", stopWatch.getTime()));
	}

	/**
	 * Iterates through all project versions checking for updates. Triggers re-deployments if necessary.
	 */
	private void updateAndRedeployIfRequired(List<WritableProject> allProjects) {
		for (var project : allProjects) {
			try {
				project = updateProjectVersions(project);
			} catch (Exception e) {
				log.error("encountered an exception while checking new project versions ({})", kv("project", project.getName()), e);
			}

			try {
				project = fetchAndUpdateMissingDatesForImages(project);
			} catch (Exception e) {
				log.error("encountered an exception while fetching the docker image dates ({})", kv("project", project.getName()), e);
			}

			try {
				fetchManifestsAndRedeploy(project);
			} catch (Exception e) {
				log.error("error on checking for new images for projects", e);
			}
		}
	}

	private WritableProject fetchAndUpdateMissingDatesForImages(WritableProject project) {
		final List<WritableProjectVersion> versionsWithoutDate = project.getVersions().stream()
				.filter(version -> version.getImageUpdatedDate() == null)
				.filter(version -> !failedManifestRequests.contains(version.getUuid()))
				.collect(Collectors.toList());

		return fetchAndUpdateDatesForVersionsOfProject(project, versionsWithoutDate);
	}

	private WritableProject fetchAndUpdateDatesForVersionsOfProject(WritableProject project, List<WritableProjectVersion> versions) {
		if (versions.isEmpty()) {
			return project;
		}

		log.trace("updating dates for project versions ({}, {})", kv("project", project.getName()), kv("version_count", versions.size()));
		final var versionWithDockerManifestList = versions.parallelStream()
				.map(version -> getManifestWithContext(project, version))
				.collect(Collectors.toList());

		for (var versionWithDockerManifest : versionWithDockerManifestList) {
			final var manifest = versionWithDockerManifest.getManifest();
			final var version = versionWithDockerManifest.getVersion();

			if (manifest == null || manifest.getImageUpdatedDate().isEmpty()) {
				log.trace("failed to get manifest for project version ({}, {})", kv("project", project.getName()), kv("version", version.getName()));
				failedManifestRequests.add(version.getUuid());
				continue;
			}

			version.setImageUpdatedDate(manifest.getImageUpdatedDate().orElse(null));
			log.trace("setting date for project version ({}, {}, {})", projectKv(project), versionKv(version), kv(IMAGE_UPDATED_DATE_KEY, version.getImageUpdatedDate()));
		}

		return projectRepository.add(project).writable();
	}

	private VersionWithDockerManifest getManifestWithContext(WritableProject project, WritableProjectVersion version) {
		try {
			return dockerRegistryClientFactory.getDockerRegistryClient(project)
					.map(client -> new VersionWithDockerManifest(version, client.getManifest(version)))
					.orElseGet(() -> new VersionWithDockerManifest(version, null));
		} catch (Exception e) {
			log.error("failed to retrieve manifest", e);
			return new VersionWithDockerManifest(version, null);
		}
	}

	private WritableProject updateProjectVersions(WritableProject project) {
		log.trace("checking for new versions ({})", kv("project", project.getName()));
		final var dockerClient = dockerRegistryClientFactory.getDockerRegistryClient(project)
				.orElseThrow(() -> new RuntimeException(String.format("Project %s has no docker registry or an error occurred instantiating the docker registry client", project.getName())));

		final var tags = Objects.requireNonNullElse(dockerClient.getAllTags(project), Collections.<String>emptyList());
		log.trace("docker tags found ({}, {})", projectKv(project), kv("tags_count", tags.size()));

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
				log.info("found new project version ({}, {})", versionKv((String) newVersions.toArray()[0]), projectKv(project));
			} else {
				log.info("found new project versions ({}, {}, {})", kv("version_count", newVersions.size()), projectKv(project), kv("versions", newVersions));
			}
		}

		newVersions.forEach(version -> {
			WritableProjectVersion projectVersion = project.createVersion(version);
			resultingEvents.add(new NewProjectVersionFoundEvent(projectVersion));
		});

		removedVersions.forEach(version -> {
			WritableProjectVersion projectVersion = project.removeVersion(version);
			log.info("found an obsolete project version ({}, {}, {})", versionKv(version), kv("version_id", projectVersion.getId()), projectKv(project));
			resultingEvents.add(new ObsoleteProjectVersionRemovedEvent(projectVersion));
		});

		if (!newVersions.isEmpty() || !removedVersions.isEmpty()) {
			ReadableProject savedProject = projectRepository.add(project);
			resultingEvents.forEach(eventDispatcher::dispatch);
			return savedProject.writable();
		}

		return project;
	}

	private void fetchManifestsAndRedeploy(WritableProject project) {
		project.getVersions().forEach(version -> {

			if (version.getDesiredState() == NotDeployed ||
					version.getDesiredState() == Deployed && version.getDeploymentBehaviour() != automatically) {
				// nothing to do here...
				return;
			}

			final var manifestWithContext = getManifestWithContext(project, version);
			if (manifestWithContext.getManifest() == null) {
				log.trace("failed to get manifest for project version ({}, {})", projectKv(project), versionKv(version));
				failedManifestRequests.add(version.getUuid());
				return;
			}

			if (!StringUtils.isBlank(manifestWithContext.getManifest().getDockerContentDigest())) {
				this.redeployByManifest(manifestWithContext.getManifest(), version);
			}
		});
	}

	/**
	 * Tries to redeploy a project version
	 */
	private void redeployByManifest(Manifest manifest, WritableProjectVersion version) {
		String digest = manifest.getDockerContentDigest();
		if (!StringUtils.equals(version.getDockerContentDigest(), digest)) {
			log.info("found a new container image for project version ({}, {}, {})", kv("digest", digest), projectKv(version.getProject()), versionKv(version));
			version.setDockerContentDigest(digest);
			version.setImageUpdatedDate(manifest.getImageUpdatedDate().orElse(null));

			if (version.getDesiredState() == Deployed && version.getDeploymentBehaviour() == automatically) {
				deploymentManager.deploy(version);
			}
		}
	}
}
