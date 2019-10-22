import {Component, Input} from "@angular/core";
import {map, tap} from "rxjs/operators";
import {DeployableStatus} from "../../deployable/deployment";
import {User} from "../../user/user";
import {UserService} from "../../user/user.service";
import {MeshComponent} from "../mesh-component";
import {ProjectMesh} from "../project-mesh";
import {ProjectMeshService} from "../project-mesh.service";

@Component({
  selector: 'mesh-component-actions',
  templateUrl: './mesh-component-actions.html'
})
export class MeshComponentActionsComponent {

  @Input() meshComponent: MeshComponent;
  @Input() projectMesh: ProjectMesh;
  @Input() hideIcons: boolean = false;
  @Input() hideText: boolean = false;

  public hasUserRequiredPermissions: boolean;

  private editingUser: User;

  constructor(private projectMeshService: ProjectMeshService,
              private userService: UserService) {
    this.userService.currentUser()
      .pipe(
        tap(currentUser => this.editingUser = currentUser),
        map(currentUser => projectMeshService.isUserAllowedToDeployProjectMeshes(currentUser))
      ).subscribe(hasPermissions => this.hasUserRequiredPermissions = hasPermissions);
  }

  public isDeployed(): boolean {
    return this.meshComponent.deployment.status === DeployableStatus.Running;
  }

  public deploy() {
    this.projectMeshService.deployMeshComponent(this.meshComponent, this.projectMesh, this.editingUser);
  }

  public stop() {
    this.projectMeshService.stopDeploymentOfMeshComponent(this.meshComponent, this.projectMesh, this.editingUser);
  }
}
