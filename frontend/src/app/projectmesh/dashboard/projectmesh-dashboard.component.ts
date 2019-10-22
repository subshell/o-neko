import {Component, OnDestroy} from "@angular/core";
import {Subscription} from "rxjs";
import {DeployableStatus, DesiredState} from "../../deployable/deployment";
import {RestService} from "../../rest/rest.service";
import {WebSocketServiceWrapper} from "../../websocket/web-socket-service-wrapper.service";
import {MeshComponent} from "../mesh-component";
import {ProjectMesh} from "../project-mesh";

@Component({
  selector: 'projectmesh-dashboard',
  templateUrl: './projectmesh-dashboard.component.html',
  styleUrls: ['./projectmesh-dashboard.component.scss']
})
export class ProjectmeshDashboardComponent implements OnDestroy {

  public meshes: Array<ProjectMesh> = [];
  public currentlyShownMeshes: Array<string> = [];
  private updateSubscriptions: Array<Subscription> = [];

  constructor(private rest: RestService,
              private wsService: WebSocketServiceWrapper) {
    this.rest.projectMesh().getAllProjectMeshes().subscribe(meshes => {
      this.meshes = meshes.sort((a, b) => a.name.localeCompare(b.name, "en"));
      for (let mesh of this.meshes) {
        this.updateSubscriptions.push(this.wsService.getMeshComponentChanges(mesh.id)
          .subscribe(message => {
            for (let i = 0; i < mesh.components.length; i++) {
              let mc = mesh.components[i];
              if (mc.id === message.deployableId) {
                mesh.components[i].desiredState = message.desiredState;
                mesh.components[i].deployment.updateWith(message);
                break;
              }
            }
          }));
        this.currentlyShownMeshes.push(...this.meshes.filter(mesh => this.getDeployedComponentsOfMesh(mesh).length > 0).map(mesh => mesh.id));
      }
    });
  }

  get selectedMeshes(): Array<ProjectMesh> {
    if (this.currentlyShownMeshes.length === 0) {
      return this.meshes;
    } else {
      return this.meshes.filter(mesh => this.currentlyShownMeshes.includes(mesh.id));
    }
  }

  ngOnDestroy() {
    this.updateSubscriptions.forEach(subscription => subscription.unsubscribe());
  }

  public getDeployedComponentsOfMesh(mesh: ProjectMesh): Array<MeshComponent> {
    return mesh.components.filter(component => component.desiredState === DesiredState.Deployed || this.shouldShowStatusInDashboardRegardlessOfDesiredState(component));
  }

  private shouldShowStatusInDashboardRegardlessOfDesiredState(component: MeshComponent): boolean {
    if (!component.deployment) {
      return false;
    }

    switch (component.deployment.status) {
      case DeployableStatus.Pending:
      case DeployableStatus.Running:
      case DeployableStatus.Failed:
        return true;
      default:
        return false;
    }
  }


}
