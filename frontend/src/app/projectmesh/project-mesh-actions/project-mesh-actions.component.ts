import {Component, Input} from "@angular/core";
import {map, tap} from "rxjs/operators";
import {DeployableStatus} from "../../deployable/deployment";
import {User} from "../../user/user";
import {UserService} from "../../user/user.service";
import {ProjectMesh} from "../project-mesh";
import {ProjectMeshService} from "../project-mesh.service";

@Component({
  selector: 'project-mesh-actions',
  templateUrl: './project-mesh-actions.html'
})
export class ProjectMeshActionsComponent {

  @Input() mesh: ProjectMesh;
  @Input() hideIcons: boolean = false;
  @Input() hideText: boolean = false;

  public hasUserDeployPermissions: boolean;

  private editingUser: User;

  constructor(private projectMeshService: ProjectMeshService,
              private userService: UserService) {
    this.userService.currentUser()
      .pipe(
        tap(currentUser => this.editingUser = currentUser),
        map(currentUser => projectMeshService.isUserAllowedToDeployProjectMeshes(currentUser))
      ).subscribe(hasPermissions => this.hasUserDeployPermissions = hasPermissions);
  }

  public isDeployed(): boolean {
    let atLeastOneRunningOrPendingComponent = this.mesh.components.find(comp => comp.deployment && (comp.deployment.status === DeployableStatus.Running || comp.deployment.status === DeployableStatus.Pending));
    return !!atLeastOneRunningOrPendingComponent;
  }

  public deploy() {
    this.projectMeshService.deployProjectMesh(this.mesh, this.editingUser);
  }

  public stop() {
    this.projectMeshService.stopDeploymentOfProjectMesh(this.mesh, this.editingUser);
  }
}
