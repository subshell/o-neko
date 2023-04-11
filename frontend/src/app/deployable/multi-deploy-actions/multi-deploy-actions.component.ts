import {ChangeDetectionStrategy, Component, Input} from "@angular/core";
import {ProjectAndVersion, ProjectService} from "../../project/project.service";
import {UserService} from "../../user/user.service";
import {map, take} from "rxjs/operators";
import {User} from "../../user/user";
import {BehaviorSubject, Observable, Subject} from "rxjs";

@Component({
  selector: 'on-multi-deploy-actions',
  templateUrl: './multi-deploy-actions.component.html',
  styleUrls: ['./multi-deploy-actions.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MultiDeployActionsComponent {
  @Input() set versions(versions: Array<ProjectAndVersion>) {
    this._versions = versions;
    this.refreshIsLimitExceeded();
  }

  get versions(): Array<ProjectAndVersion> {
    return this._versions;
  }

  private _versions: Array<ProjectAndVersion> = [];

  @Input() set limit(limit: number) {
    this._limit = limit;
    this.refreshIsLimitExceeded();
  };

  get limit(): number {
    return this._limit;
  }

  private _limit: number = 10;

  hasDeployPermission$: Observable<boolean>;
  isLimitExceeded$: Observable<boolean> = new BehaviorSubject(false);

  private editingUser$: Observable<User>;

  constructor(private projectService: ProjectService,
              private userService: UserService) {
    this.editingUser$ = this.userService.currentUser();
    this.hasDeployPermission$ = this.editingUser$.pipe(
      map(currentUser => projectService.isUserAllowedToDeployProjects(currentUser))
    );
  }

  deploy() {
    if (!this.isLimitExceeded()) {
      this.editingUser$.pipe(take(1)).subscribe(user => {
        this.projectService.deployProjectVersions(this.versions, user);
      });
    }
  }

  stop() {
    if (!this.isLimitExceeded()) {
      this.editingUser$.pipe(take(1)).subscribe(user => {
        this.projectService.stopDeployments(this.versions, user);
      });
    }
  }

  private refreshIsLimitExceeded() {
    (this.isLimitExceeded$ as Subject<boolean>).next(this.isLimitExceeded());
  }

  private isLimitExceeded(): boolean {
    return this.limit < this.versions.length;
  }
}
