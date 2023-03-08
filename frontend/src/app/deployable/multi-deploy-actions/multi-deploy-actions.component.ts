import {Component, Input} from "@angular/core";
import {ProjectAndVersion, ProjectService} from "../../project/project.service";
import {UserService} from "../../user/user.service";
import {map, tap} from "rxjs/operators";
import {User} from "../../user/user";

@Component({
  selector: 'on-multi-deploy-actions',
  templateUrl: './multi-deploy-actions.component.html',
  styleUrls: ['./multi-deploy-actions.component.scss']
})
export class MultiDeployActionsComponent {
  @Input() versions: Array<ProjectAndVersion> = [];
  private editingUser: User;
  hasDeployPermission: boolean;

  constructor(private projectService: ProjectService,
              private userService: UserService) {
    this.userService.currentUser()
      .pipe(
        tap(currentUser => this.editingUser = currentUser),
        map(currentUser => projectService.isUserAllowedToDeployProjects(currentUser))
      ).subscribe(allowedToDeploy => {
      this.hasDeployPermission = allowedToDeploy;
    });
  }

  deploy() {
    this.projectService.deployProjectVersions(this.versions, this.editingUser);
  }

  stop() {
    this.projectService.stopDeployments(this.versions, this.editingUser);
  }
}
