import {map} from 'lodash';
import {Namespace, NamespaceDTO} from "../namespace/namespace";
import {AggregatedDeploymentStatus, DeploymentBehaviour, LifetimeBehaviour} from "../project/project";
import {MeshComponent, MeshComponentDTO} from "./mesh-component";

export interface ProjectMeshDTO {
  id?: string;
  name?: string;
  namespace: NamespaceDTO;
  implicitNamespace: NamespaceDTO;
  deploymentBehaviour: DeploymentBehaviour;
  lifetimeBehaviour: LifetimeBehaviour;
  components: Array<MeshComponentDTO>;
  status: AggregatedDeploymentStatus;
}

export class ProjectMesh implements ProjectMeshDTO {
  name?: string;
  namespace: Namespace;
  implicitNamespace: Namespace;
  deploymentBehaviour: DeploymentBehaviour;
  lifetimeBehaviour: LifetimeBehaviour;
  components: Array<MeshComponent>;
  status: AggregatedDeploymentStatus;
  mostRecentDeploymentDate: Date;
  formattedMostRecentDeploymentDate: string;

  constructor(public readonly id?: string) {
  }

  static from(from: ProjectMeshDTO): ProjectMesh {
    let mesh = new ProjectMesh(from.id);
    mesh.name = from.name;
    mesh.namespace = Namespace.from(from.namespace);
    mesh.implicitNamespace = Namespace.from(from.implicitNamespace);
    mesh.deploymentBehaviour = from.deploymentBehaviour;
    mesh.lifetimeBehaviour = from.lifetimeBehaviour;
    mesh.components = map(from.components, component => MeshComponent.from(component));
    mesh.status = from.status;
    let orderedByDeploymentDate = mesh.components.filter(component => component.deployment && component.deployment.timestamp)
      .sort((comp1, comp2) => comp1.deployment.timestamp.getTime() - comp2.deployment.timestamp.getTime());
    if (orderedByDeploymentDate.length > 0) {
      mesh.mostRecentDeploymentDate = orderedByDeploymentDate[0].deployment.timestamp;
      mesh.formattedMostRecentDeploymentDate = orderedByDeploymentDate[0].deployment.formattedTimestamp;
    }
    return mesh;
  }

  public isNew(): boolean {
    return !this.id;
  }
}
