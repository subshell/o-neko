import {Injectable} from "@angular/core";
import {MatDialog} from "@angular/material/dialog";
import {MatSnackBar} from "@angular/material/snack-bar";
import {Observable} from "rxjs";
import {throwError} from "rxjs/internal/observable/throwError";
import {filter, mergeMap, shareReplay} from 'rxjs/operators';
import {DeployableStatus} from "../deployable/deployment";
import {RestService} from "../rest/rest.service";
import {User} from "../user/user";
import {UserRole} from "../user/user-role";
import {
  ConfirmWithTextDialog,
  ConfirmWithTextDialogData
} from "../util/confirm-with-text-dialog/confirm-with-text-dialog.component";
import {TimeoutSnackbarComponent} from "../util/timout-snackbar/timeout.snackbar.component";
import {Project} from "./project";
import {ProjectVersion} from "./project-version";
import {ProjectExportDTO} from './project-export';

@Injectable()
export class ProjectService {

  private static readonly SNACKBAR_DEFAULT_DURATION = 4000;
  private static readonly SNACKBAR_ERROR_DURATION = 6000;

  constructor(private rest: RestService,
              private dialog: MatDialog,
              private snackBar: MatSnackBar) {
  }

  public isUserAllowedToCreateProjects(user: User): boolean {
    return this.isUserAllowedToEditProjects(user);
  }

  public isUserAllowedToEditProjects(user: User): boolean {
    return user?.hasRolePermission(UserRole.ADMIN) || user?.hasRolePermission(UserRole.DOER);
  }

  public isUserAllowedToDeleteProjects(user: User): boolean {
    return this.isUserAllowedToEditProjects(user);
  }

  public isUserAllowedToDeployProjects(user: User): boolean {
    return !!user;
  }

  public isUserAllowedToExportProjects(user: User): boolean {
    return user?.hasRolePermission(UserRole.ADMIN) || user?.hasRolePermission(UserRole.DOER) || user?.hasRolePermission(UserRole.VIEWER);
  }

  public exportProject(project: Project, user: User): Observable<ProjectExportDTO> {
    if (!this.isUserAllowedToExportProjects(user)) {
      return throwError('User has no permissions to export projects');
    }

    return this.rest.project().exportProject(project);
  }

  public deleteProjectInteractively(project: Project, user: User): Observable<void> {
    if (!this.isUserAllowedToDeleteProjects(user)) {
      return throwError('User has no permissions to delete projects');
    }
    let deletionObservable = this.dialog.open(ConfirmWithTextDialog, {
      data: <ConfirmWithTextDialogData>{
        title: `Delete ${project.name}?`,
        message: 'Please confirm the deletion of this project by entering the project name below. This action cannot be undone.',
        confirmationTextPlaceholder: 'Confirm project name',
        confirmationText: project.name,
        okButtonText: 'Delete'
      },
      width: "50%"
    }).afterClosed()
      .pipe(filter(result => result === true))
      .pipe(mergeMap(() => this.deleteProject(project, user)))
      .pipe(shareReplay());
    deletionObservable.subscribe();
    return deletionObservable;
  }

  /**
   * Will save the given project.
   * Will show a notification in case of success.
   *
   * @param {Project} project
   * @returns {Observable<void>}
   */
  public saveProject(project: Project, user: User): Observable<Project> {
    if (!this.isUserAllowedToEditProjects(user)) {
      return throwError('User has no permissions to edit projects');
    }
    let isNewProject = project.isNew();
    let projectObservable = this.rest.project().persistProject(project)
      .pipe(shareReplay());
    projectObservable.subscribe(savedProject => {
      this.snackBar.openFromComponent(TimeoutSnackbarComponent, {
        data: {
          text: `Project ${savedProject.name} has been ${isNewProject ? 'created' : 'saved'}.`
        },
        duration: ProjectService.SNACKBAR_DEFAULT_DURATION
      });
      return savedProject;
    });
    return projectObservable;
  }

  /**
   * Deletes the given project straigt away - will show a notification to the user.
   *
   * @param {Project} project
   * @returns {Observable<void>}
   */
  public deleteProject(project: Project, user: User): Observable<void> {
    if (!this.isUserAllowedToDeleteProjects(user)) {
      return throwError('User has no permissions to delete projects');
    }
    let deletionObservable = this.rest.project().deleteProject(project)
      .pipe(shareReplay());

    deletionObservable.subscribe(() => {
      this.snackBar.openFromComponent(TimeoutSnackbarComponent, {
        data: {
          text: `Project ${project.name} has been deleted.`
        },
        duration: ProjectService.SNACKBAR_DEFAULT_DURATION
      });
    });
    return deletionObservable;
  }

  public deployProjectVersion(projectVersion: ProjectVersion, project: Project, user: User): Observable<Project> {
    if (!this.isUserAllowedToDeployProjects(user)) {
      return throwError('User has no permissions to trigger version deployments');
    }
    let triggerObservable = this.rest.project().deployProjectVersion(projectVersion, project)
      .pipe(shareReplay());
    triggerObservable.subscribe(() => {
      projectVersion.deployment.status = DeployableStatus.Pending;
      this.snackBar.openFromComponent(TimeoutSnackbarComponent, {
        data: {
          text: `Project ${project.name} version ${projectVersion.name} has been deployed.`
        },
        duration: ProjectService.SNACKBAR_DEFAULT_DURATION
      });
    }, (response) => {
      this.snackBar.openFromComponent(TimeoutSnackbarComponent, {
        data: {
          text: `Error: ${response.error.message}`
        },
        duration: ProjectService.SNACKBAR_ERROR_DURATION
      });
    });
    return triggerObservable;
  }

  public stopDeployment(projectVersion: ProjectVersion, project: Project, user: User) {
    if (!this.isUserAllowedToDeployProjects(user)) {
      return throwError('User has no permissions to trigger version deployments');
    }

    this.rest.project().stopDeployment(projectVersion, project).subscribe(() => {
      projectVersion.deployment.status = DeployableStatus.Pending;
      this.snackBar.openFromComponent(TimeoutSnackbarComponent, {
        data: {
          text: `Project ${project.name} version ${projectVersion.name} is stopped.`
        },
        duration: ProjectService.SNACKBAR_DEFAULT_DURATION
      });
    }, (response) => {
      this.snackBar.openFromComponent(TimeoutSnackbarComponent, {
        data: {
          text: `Error: ${response.error.message}`
        },
        duration: ProjectService.SNACKBAR_ERROR_DURATION
      });
    });

  }

  private saveProjectVersion(project: Project, user: User, version: ProjectVersion): Observable<Project> {
    project.versions
      .filter(v => v.uuid === version.uuid)
      .forEach(v => {
        v.mergeFrom(version);
      });
    return this.saveProject(project, user);
  }
}
