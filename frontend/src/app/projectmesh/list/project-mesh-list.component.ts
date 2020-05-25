import {Component} from "@angular/core";
import { MatDialog } from "@angular/material/dialog";
import { PageEvent } from "@angular/material/paginator";
import { Sort } from "@angular/material/sort";
import {Router} from "@angular/router";
import {RestService} from "../../rest/rest.service";
import {User} from "../../user/user";
import {UserService} from "../../user/user.service";
import {CreateProjectMeshDialogComponent} from "../create-mesh-dialog/create-project-mesh-dialog.component";
import {ProjectMesh} from "../project-mesh";
import {ProjectMeshService} from "../project-mesh.service";

class ColumnDefinition {

  constructor(private _key: string, private _label: string, private _getValue: (projectMesh) => any, private _getComparisonValue?: (ProjectMesh) => any) {
  }

  get key(): string {
    return this._key;
  }

  get label(): string {
    return this._label;
  }

  public getValue(projectMesh: ProjectMesh): any {
    return this._getValue(projectMesh);
  }

  public getComparisonValue(projectMesh: ProjectMesh) {
    return this._getComparisonValue ? this._getComparisonValue(projectMesh) : this._getValue(projectMesh);
  }

}

@Component({
  selector: 'project-mesh-list',
  templateUrl: './project-mesh-list.component.html',
  styleUrls: ['./project-mesh-list.component.scss']
})
export class ProjectMeshListComponent {

  public sortedMeshes: Array<ProjectMesh>;
  public meshes: Array<ProjectMesh> = [];

  public nameColumn = new ColumnDefinition('name', 'Name', (mesh: ProjectMesh) => mesh.name);
  public statusColumn = new ColumnDefinition('status', 'Status', (mesh: ProjectMesh) => mesh.status);
  public componentsColumn = new ColumnDefinition('components', 'Components', (mesh: ProjectMesh) => mesh.components.length);
  public deploymentDatesColumn = new ColumnDefinition('deployment_date', 'Deployment Date', (mesh: ProjectMesh) => mesh.formattedMostRecentDeploymentDate, mesh => (mesh: ProjectMesh) => mesh.mostRecentDeploymentDate);
  public allColumns = [this.nameColumn, this.statusColumn, this.componentsColumn, this.deploymentDatesColumn];
  public activeColumnKeys = [this.nameColumn.key, this.statusColumn.key, this.componentsColumn.key, this.deploymentDatesColumn.key];
  public pageSettings = {
    pageSize: 10,
    pageSizeOptions: [10, 25, 50, 100]
  };
  private editingUser: User;
  private pageEvent: PageEvent;
  private sort: Sort;

  constructor(private rest: RestService,
              private userService: UserService,
              private projectMeshService: ProjectMeshService,
              private dialog: MatDialog,
              private router: Router) {
    this.userService.currentUser().subscribe(currentUser => this.editingUser = currentUser);
    this.rest.projectMesh().getAllProjectMeshes().subscribe(meshes => {
      this.meshes = meshes;
      this.sortProjectMeshes();
    });
  }

  public mayEditProjectMeshes(): boolean {
    return this.projectMeshService.isUserAllowedToEditProjectMeshes(this.editingUser);
  }

  public mayDeleteProjectMeshes(): boolean {
    return this.projectMeshService.isUserAllowedToDeleteProjectMeshes(this.editingUser);
  }

  public mayDeployProjectMeshes(): boolean {
    return this.projectMeshService.isUserAllowedToDeployProjectMeshes(this.editingUser);
  }

  public createProjectMesh() {
    this.dialog.open(CreateProjectMeshDialogComponent, {
      width: "80%",
      data: this.meshes
    }).afterClosed().subscribe((newMesh: ProjectMesh) => {
      if (newMesh) {
        this.projectMeshService.saveProjectMesh(newMesh, this.editingUser).subscribe(savedMesh => {
          this.router.navigateByUrl(`/project-meshes/${savedMesh.id}`);
        })
      }
    })
  }

  public deleteProjectMesh(mesh: ProjectMesh) {
    if (!this.mayEditProjectMeshes()) {
      return;
    }
    this.projectMeshService.deleteProjectMeshInteractively(mesh, this.editingUser).subscribe(() => {
      this.meshes.splice(this.meshes.indexOf(mesh), 1);
      this.sortProjectMeshes();
    });
  }

  public deployProjectMesh(mesh: ProjectMesh) {
    if (!this.mayDeployProjectMeshes()) {
      return
    }
    this.projectMeshService.deployProjectMesh(mesh, this.editingUser).subscribe();
  }

  public stopProjectMesh(mesh: ProjectMesh) {
    if (!this.mayDeployProjectMeshes()) {
      return
    }
    this.projectMeshService.stopDeploymentOfProjectMesh(mesh, this.editingUser).subscribe();
  }

  public sortProjectMeshes(sort?: Sort) {
    this.sort = sort;
    let first = 0;
    let pageSize = this.pageSettings.pageSize;
    if (this.pageEvent) {
      first = this.pageEvent.pageIndex * this.pageEvent.pageSize;
      pageSize = this.pageEvent.pageSize;
    }
    this.sortedMeshes = this.getSortedProjectMeshes(sort).slice(first, first + pageSize);
  }

  public paginationEvent(event: PageEvent) {
    this.pageEvent = event;
    this.sortProjectMeshes(this.sort);
  }

  public getSortedProjectMeshes(sort?: Sort): Array<ProjectMesh> {
    const data = this.meshes.slice();
    if (!sort || !sort.active || sort.direction == '') {
      return data;
    }
    let columnDefinition = this.allColumns.find(c => c.key === sort.active);

    return data.sort((a, b) => {
      let isAsc = sort.direction === 'asc';
      return this.compare(columnDefinition.getValue(a), columnDefinition.getValue(b), isAsc);
    });
  }

  public isColumnActive(col: string): boolean {
    return this.activeColumnKeys.indexOf(col) !== -1;
  }

  public getActiveColumns(): Array<ColumnDefinition> {
    return this.allColumns.filter(c => this.activeColumnKeys.includes(c.key));
  }

  private compare(a: any, b: any, isAsc: boolean): number {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }
}
