import {Component, Input} from "@angular/core";
import {map, tap} from "rxjs/operators";
import {DeployableStatus} from "../../deployable/deployment";
import {User} from "../../user/user";
import {UserService} from "../../user/user.service";
import {LogService} from "../../util/log.service";
import {Project} from "../project";
import {ProjectVersion} from "../project-version";
import {ProjectVersionVariableActionChangeEvent} from "../project-version-variable-actions/project-version-variable-actions.component";
import {ProjectService} from "../project.service";

@Component({
  selector: 'projectVersionActions',
  templateUrl: './project-version-actions.html'
})
export class ProjectVersionActionsComponent {

  @Input() version: ProjectVersion;
  @Input() project: Project;
  @Input() hideIcons: boolean = false;
  @Input() hideText: boolean = false;
  public hasDeployPermission: boolean;
  public hasEditPermissions: boolean;
  private log = LogService.getLogger(ProjectVersionActionsComponent);
  private editingUser: User;

  constructor(private projectService: ProjectService,
              private userService: UserService) {
    this.userService.currentUser()
      .pipe(
        tap(currentUser => this.editingUser = currentUser),
        map(currentUser => [
          projectService.isUserAllowedToDeployProjects(currentUser),
          projectService.isUserAllowedToEditProjects(currentUser)
        ])
      ).subscribe(([allowedToDeploy, allowedToEdit]) => {
      this.hasDeployPermission = allowedToDeploy;
      this.hasEditPermissions = allowedToEdit;
    });

  }

  public isDeployed(): boolean {
    return this.version.deployment.status === DeployableStatus.Running;
  }

  public deploy() {
    this.projectService.deployProjectVersion(this.version, this.project, this.editingUser);
  }

  public stop() {
    this.projectService.stopDeployment(this.version, this.project, this.editingUser);
  }

  public saveVersion($event: ProjectVersionVariableActionChangeEvent) {
    this.log.debug(`Version of ${$event.version.name} changed`);
    this.projectService.saveProject(this.project, this.editingUser);
  }
}
