import {Component} from "@angular/core";
import {MatDialog} from "@angular/material/dialog";
import {PageEvent} from "@angular/material/paginator";
import {Sort} from "@angular/material/sort";
import {Router} from "@angular/router";
import {RestService} from "../../rest/rest.service";
import {User} from "../../user/user";
import {UserService} from "../../user/user.service";
import {CreateProjectDialogComponent} from "../create-project-dialog/create-project-dialog.component";
import {Project} from "../project";
import {ProjectService} from "../project.service";

class ColumnDefinition {

  constructor(private _key: string, private _label: string, private _getValue: (Project) => any) {

  }

  get key(): string {
    return this._key;
  }

  get label(): string {
    return this._label;
  }

  public getValue(project: Project): any {
    return this._getValue(project);
  }

}

@Component({
  selector: 'project-list',
  templateUrl: './project-list.component.html',
  styleUrls: ['./project-list.component.scss']
})
export class ProjectListComponent {

  public sortedProjects: Array<Project>;
  public projects: Array<Project> = [];

  public nameColumn = new ColumnDefinition('name', 'Name', (project: Project) => project.name);
  public statusColumn = new ColumnDefinition('status', 'Status', (project: Project) => project.isOrphan() ? 'orphaned' : project.status);
  public imageNameColumn = new ColumnDefinition('imagename', 'Image name', (project: Project) => project.imageName);
  public versionsColumn = new ColumnDefinition('versions', 'Versions', (project: Project) => project.versions.length);
  public allColumns = [this.nameColumn, this.statusColumn, this.imageNameColumn, this.versionsColumn];
  public activeColumnKeys = [this.nameColumn.key, this.statusColumn.key, this.versionsColumn.key];
  public pageSettings = {
    pageSize: 10,
    pageSizeOptions: [10, 25, 50, 100]
  };
  private editingUser: User;
  private pageEvent: PageEvent;
  private sort: Sort;

  constructor(private rest: RestService,
              private userService: UserService,
              private projectService: ProjectService,
              private dialog: MatDialog,
              private router: Router) {
    this.userService.currentUser().subscribe(currentUser => this.editingUser = currentUser);
    this.rest.project().getAllProjects().subscribe(projects => {
      this.projects = projects;
      this.sortProjects();
    });
  }

  public mayEditProjects(): boolean {
    return this.projectService.isUserAllowedToEditProjects(this.editingUser);
  }

  public createProject() {
    this.dialog.open(CreateProjectDialogComponent, {
      width: "80%",
      data: this.projects
    }).afterClosed().subscribe((newProject: Project) => {
      if (newProject) {
        this.projectService.saveProject(newProject, this.editingUser).subscribe(savedProject => {
          this.router.navigateByUrl(`/projects/${savedProject.uuid}`);
        })
      }
    });
  }

  public deleteProject(project: Project) {
    if (!this.mayEditProjects()) {
      return;
    }
    this.projectService.deleteProjectInteractively(project, this.editingUser).subscribe(() => {
      this.projects.splice(this.projects.indexOf(project), 1);
      this.sortProjects();
    });
  }

  public sortProjects(sort?: Sort) {
    this.sort = sort;
    let first = 0;
    let pageSize = this.pageSettings.pageSize;
    if (this.pageEvent) {
      first = this.pageEvent.pageIndex * this.pageEvent.pageSize;
      pageSize = this.pageEvent.pageSize;
    }
    this.sortedProjects = this.getSortedProjects(sort).slice(first, first + pageSize);
  }

  public paginationEvent(event: PageEvent) {
    this.pageEvent = event;
    this.sortProjects(this.sort);
  }

  public getSortedProjects(sort?: Sort): Array<Project> {
    const data = this.projects.slice();
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
