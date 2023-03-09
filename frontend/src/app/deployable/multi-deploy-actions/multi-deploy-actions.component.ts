import {ChangeDetectionStrategy, Component, Input} from "@angular/core";
import {ProjectAndVersion, ProjectService} from "../../project/project.service";
import {UserService} from "../../user/user.service";
import {map, tap} from "rxjs/operators";
import {User} from "../../user/user";
import {Observable} from "rxjs";

@Component({
  selector: 'on-multi-deploy-actions',
  templateUrl: './multi-deploy-actions.component.html',
  styleUrls: ['./multi-deploy-actions.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MultiDeployActionsComponent {
  @Input() versions: Array<ProjectAndVersion> = [];
  private editingUser$: Observable<User>;
  hasDeployPermission$: Observable<boolean>;

  constructor(private projectService: ProjectService,
              private userService: UserService) {

    this.editingUser$ = this.userService.currentUser();
    this.hasDeployPermission$ = this.editingUser$.pipe(
      map(currentUser => projectService.isUserAllowedToDeployProjects(currentUser))
    );
  }

  deploy() {
    this.editingUser$.subscribe(user => {
      this.projectService.deployProjectVersions(this.versions, user);
    });
  }

  stop() {
    this.editingUser$.subscribe(user => {
      this.projectService.stopDeployments(this.versions, user);
    });
  }
}
