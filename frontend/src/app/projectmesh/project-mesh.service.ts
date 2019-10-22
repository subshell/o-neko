import {Injectable} from "@angular/core";
import {MatDialog, MatSnackBar} from "@angular/material";
import {Observable} from "rxjs";
import {throwError} from "rxjs/internal/observable/throwError";
import {filter, mergeMap, shareReplay} from 'rxjs/operators';
import {DeployableStatus} from "../deployable/deployment";
import {AggregatedDeploymentStatus} from "../project/project";
import {RestService} from "../rest/rest.service";
import {User} from "../user/user";
import {UserRole} from "../user/user-role";
import {
  ConfirmWithTextDialog,
  ConfirmWithTextDialogData
} from "../util/confirm-with-text-dialog/confirm-with-text-dialog.component";
import {TimeoutSnackbarComponent} from "../util/timout-snackbar/timeout.snackbar.component";
import {MeshComponent} from "./mesh-component";
import {ProjectMesh} from "./project-mesh";

@Injectable()
export class ProjectMeshService {

  private static readonly SNACKBAR_DEFAULT_DURATION = 4000;
  private static readonly SNACKBAR_ERROR_DURATION = 6000;

  constructor(private rest: RestService,
              private dialog: MatDialog,
              private snackBar: MatSnackBar) {
  }

  public isUserAllowedToCreateProjectMeshess(user: User): boolean {
    return this.isUserAllowedToEditProjectMeshes(user);
  }

  public isUserAllowedToEditProjectMeshes(user: User): boolean {
    return user && (user.hasRolePermission(UserRole.ADMIN) || user.hasRolePermission(UserRole.DOER));
  }

  public isUserAllowedToDeleteProjectMeshes(user: User): boolean {
    return this.isUserAllowedToEditProjectMeshes(user);
  }

  public isUserAllowedToDeployProjectMeshes(user: User): boolean {
    return user && true;
  }

  public deleteProjectMeshInteractively(projectMesh: ProjectMesh, user: User): Observable<void> {
    if (!this.isUserAllowedToDeleteProjectMeshes(user)) {
      return throwError('User has no permissions to delete project meshes');
    }
    let deletionObservable = this.dialog.open(ConfirmWithTextDialog, {
      data: <ConfirmWithTextDialogData>{
        title: `Delete ${projectMesh.name}?`,
        message: 'Please confirm the deletion of this project by entering the mesh name below. This action cannot be undone.',
        confirmationTextPlaceholder: 'Confirm project name',
        confirmationText: projectMesh.name,
        okButtonText: 'Delete'
      },
      width: "50%"
    }).afterClosed()
      .pipe(filter(result => result === true))
      .pipe(mergeMap(() => this.deleteProjectMesh(projectMesh, user)))
      .pipe(shareReplay());
    deletionObservable.subscribe();
    return deletionObservable;
  }

  public deployProjectMesh(projectMesh: ProjectMesh, user: User): Observable<ProjectMesh> {
    if (!this.isUserAllowedToDeployProjectMeshes(user)) {
      return throwError('User has no permissions to trigger mesh deployments');
    }
    let triggerObservable = this.rest.projectMesh().deployMesh(projectMesh)
      .pipe(shareReplay());
    triggerObservable.subscribe(() => {
      projectMesh.components.forEach(component => component.deployment.status = DeployableStatus.Pending);
      projectMesh.status = AggregatedDeploymentStatus.Pending;
      this.snackBar.openFromComponent(TimeoutSnackbarComponent, {
        data: {
          text: `Project Mesh ${projectMesh.name} has been deployed.`
        },
        duration: ProjectMeshService.SNACKBAR_DEFAULT_DURATION
      });
    }, (response) => {
      this.snackBar.openFromComponent(TimeoutSnackbarComponent, {
        data: {
          text: `Error: ${response.error.message}`
        },
        duration: ProjectMeshService.SNACKBAR_ERROR_DURATION
      });
    });
    return triggerObservable;
  }

  public stopDeploymentOfProjectMesh(projectMesh: ProjectMesh, user: User) {
    if (!this.isUserAllowedToDeployProjectMeshes(user)) {
      return throwError('User has no permissions to stop deployments of meshes');
    }

    this.rest.projectMesh().stopDeployingMesh(projectMesh).subscribe(() => {
      projectMesh.components.forEach(component => component.deployment.status = DeployableStatus.Pending);
      projectMesh.status = AggregatedDeploymentStatus.Pending;
      this.snackBar.openFromComponent(TimeoutSnackbarComponent, {
        data: {
          text: `Project Mesh ${projectMesh.name} is stopped.`
        },
        duration: ProjectMeshService.SNACKBAR_DEFAULT_DURATION
      });
    }, (response) => {
      this.snackBar.openFromComponent(TimeoutSnackbarComponent, {
        data: {
          text: `Error: ${response.error.message}`
        },
        duration: ProjectMeshService.SNACKBAR_ERROR_DURATION
      });
    });

  }

  public deployMeshComponent(meshComponent: MeshComponent, projectMesh: ProjectMesh, user: User): Observable<ProjectMesh> {
    if (!this.isUserAllowedToDeployProjectMeshes(user)) {
      return throwError('User has no permissions to trigger mesh component deployments');
    }
    let triggerObservable = this.rest.projectMesh().deployMeshComponent(meshComponent, projectMesh)
      .pipe(shareReplay());
    triggerObservable.subscribe(() => {
      meshComponent.deployment.status = DeployableStatus.Pending;
      projectMesh.status = AggregatedDeploymentStatus.Pending;
      this.snackBar.openFromComponent(TimeoutSnackbarComponent, {
        data: {
          text: `Mesh component ${meshComponent.name} has been deployed.`
        },
        duration: ProjectMeshService.SNACKBAR_DEFAULT_DURATION
      });
    }, (response) => {
      this.snackBar.openFromComponent(TimeoutSnackbarComponent, {
        data: {
          text: `Error: ${response.error.message}`
        },
        duration: ProjectMeshService.SNACKBAR_ERROR_DURATION
      });
    });
    return triggerObservable;
  }

  public stopDeploymentOfMeshComponent(meshComponent: MeshComponent, projectMesh: ProjectMesh, user: User) {
    if (!this.isUserAllowedToDeployProjectMeshes(user)) {
      return throwError('User has no permissions to stop deployments of mesh components');
    }

    this.rest.projectMesh().stopDeployingMeshComponent(meshComponent, projectMesh).subscribe(() => {
      meshComponent.deployment.status = DeployableStatus.Pending;
      projectMesh.status = AggregatedDeploymentStatus.Pending;
      this.snackBar.openFromComponent(TimeoutSnackbarComponent, {
        data: {
          text: `Mesh component ${meshComponent.name} is stopped.`
        },
        duration: ProjectMeshService.SNACKBAR_DEFAULT_DURATION
      });
    }, (response) => {
      this.snackBar.openFromComponent(TimeoutSnackbarComponent, {
        data: {
          text: `Error: ${response.error.message}`
        },
        duration: ProjectMeshService.SNACKBAR_ERROR_DURATION
      });
    });

  }

  /**
   * Will save the given project mesh.
   * Will show a notification in case of success.
   */
  public saveProjectMesh(mesh: ProjectMesh, user: User): Observable<ProjectMesh> {
    if (!this.isUserAllowedToEditProjectMeshes(user)) {
      return throwError('User has no permissions to edit projects');
    }
    let isNewProject = mesh.isNew();
    let meshObservable = this.rest.projectMesh().persistProjectMesh(mesh)
      .pipe(shareReplay());
    meshObservable.subscribe(savedMesh => {
      this.snackBar.openFromComponent(TimeoutSnackbarComponent, {
        data: {
          text: `Project mesh ${savedMesh.name} has been ${isNewProject ? 'created' : 'saved'}.`
        },
        duration: ProjectMeshService.SNACKBAR_DEFAULT_DURATION
      });
      return savedMesh;
    });
    return meshObservable;
  }

  /**
   * Deletes the given project mesh straight away - will show a notification to the user.
   */
  public deleteProjectMesh(mesh: ProjectMesh, user: User): Observable<void> {
    if (!this.isUserAllowedToDeleteProjectMeshes(user)) {
      return throwError('User has no permissions to delete project meshes');
    }
    let deletionObservable = this.rest.projectMesh().deleteProjectMesh(mesh)
      .pipe(shareReplay());

    deletionObservable.subscribe(() => {
      this.snackBar.openFromComponent(TimeoutSnackbarComponent, {
        data: {
          text: `Project mesh ${mesh.name} has been deleted.`
        },
        duration: ProjectMeshService.SNACKBAR_DEFAULT_DURATION
      });
    });
    return deletionObservable;
  }

}
