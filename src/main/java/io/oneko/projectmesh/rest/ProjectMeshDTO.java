package io.oneko.projectmesh.rest;

import io.oneko.automations.LifetimeBehaviourDTO;
import io.oneko.deployable.AggregatedDeploymentStatus;
import io.oneko.deployable.DeploymentBehaviour;
import io.oneko.namespace.rest.NamespaceDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Data
public class ProjectMeshDTO {
	private UUID id;
	private String name;
	private NamespaceDTO implicitNamespace;
	private NamespaceDTO namespace;
	private DeploymentBehaviour deploymentBehaviour;
	private LifetimeBehaviourDTO lifetimeBehaviour;
	private List<MeshComponentDTO> components = new ArrayList<>();
	private AggregatedDeploymentStatus status;
}
