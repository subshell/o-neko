import {Injectable} from "@angular/core";
import {MatDialog} from "@angular/material/dialog";
import {MatSnackBar} from "@angular/material/snack-bar";
import {EMPTY, Observable, throwError} from "rxjs";
import {filter, mergeMap, share, shareReplay} from 'rxjs/operators';
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
import {TranslateService} from "@ngx-translate/core";

@Injectable()
export class ProjectService {

  private static readonly SNACKBAR_DEFAULT_DURATION = 4000;
  private static readonly SNACKBAR_ERROR_DURATION = 6000;

  constructor(private rest: RestService,
              private dialog: MatDialog,
              private snackBar: MatSnackBar,
              private readonly translate: TranslateService) {
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

  public isUserAllowedToEditProjectVersionVariables(user: User): boolean {
    return true;
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
        title: this.translate.instant('components.project.service.deleteDialog.title', {name: project.name}),
        message: this.translate.instant('components.project.service.deleteDialog.message'),
        confirmationTextPlaceholder: this.translate.instant('components.project.service.deleteDialog.confirmPlaceholder'),
        confirmationText: project.name,
        okButtonText: this.translate.instant('components.project.service.deleteDialog.okButtonText')
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
          text: this.translate.instant('components.project.service.projectSnackbarMessage', {name: savedProject.name, action: isNewProject ? 'created' : 'saved'})
        },
        duration: ProjectService.SNACKBAR_DEFAULT_DURATION
      });
      return savedProject;
    });
    return projectObservable;
  }

  public saveProjectVersionVariables(project: Project, projectVersion: ProjectVersion): Observable<Project> {
    if (project.isNew()) {
      return EMPTY;
    }
    const projectObservable = this.rest.project().persistProjectVersionVariables(project, projectVersion).pipe(shareReplay());
    projectObservable.subscribe(savedProject => {
      this.snackBar.openFromComponent(TimeoutSnackbarComponent, {
        data: {
          text: this.translate.instant('components.project.service.projectSnackbarMessage', {name: savedProject.name, action: 'saved'})
        },
        duration: ProjectService.SNACKBAR_DEFAULT_DURATION
      });
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
          text: this.translate.instant('components.project.service.projectSnackbarMessage', {name: project.name, action: 'deleted'})
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
    projectVersion.deployment.status = DeployableStatus.Pending;
    let triggerObservable = this.rest.project().deployProjectVersion(projectVersion, project)
      .pipe(shareReplay());
    triggerObservable.subscribe(() => {
      this.snackBar.openFromComponent(TimeoutSnackbarComponent, {
        data: {
          text: this.translate.instant('components.project.service.versionSnackbarMessage', {name: project.name, version: projectVersion.name, action: 'deployed'})
        },
        duration: ProjectService.SNACKBAR_DEFAULT_DURATION
      });
    }, (response) => {
      projectVersion.deployment.status = DeployableStatus.Unknown;
      this.snackBar.openFromComponent(TimeoutSnackbarComponent, {
        data: {
          text: this.translate.instant('components.project.service.errorMessage', {message: response.error.message})
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

    projectVersion.deployment.status = DeployableStatus.Pending;
    this.rest.project().stopDeployment(projectVersion, project).subscribe(() => {
      this.snackBar.openFromComponent(TimeoutSnackbarComponent, {
        data: {
          text: this.translate.instant('components.project.service.versionSnackbarMessage', {name: project.name, version: projectVersion.name, action: 'stopped'})
        },
        duration: ProjectService.SNACKBAR_DEFAULT_DURATION
      });
    }, (response) => {
      projectVersion.deployment.status = DeployableStatus.Unknown;
      this.snackBar.openFromComponent(TimeoutSnackbarComponent, {
        data: {
          text: this.translate.instant('components.project.service.errorMessage', {message: response.error.message})
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
