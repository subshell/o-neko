package io.oneko.projectmesh.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import io.oneko.automations.LifetimeBehaviour;
import io.oneko.deployable.DeploymentBehaviour;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class ProjectMeshMongo {
	@Id
	private UUID id;
	@Indexed(unique = true)
	private String name;
	private UUID namespace;
	private DeploymentBehaviour deploymentBehaviour;
	private LifetimeBehaviour lifetimeBehaviour;
	private List<MeshComponentMongo> components = new ArrayList<>();
}
