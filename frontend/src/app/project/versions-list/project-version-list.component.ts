import {Component, OnDestroy, OnInit} from "@angular/core";
import {PageEvent} from "@angular/material/paginator";
import {Sort} from "@angular/material/sort";
import {ActivatedRoute, ParamMap} from "@angular/router";
import {Subscription} from "rxjs";
import {switchMap} from "rxjs/operators";
import {RestService} from "../../rest/rest.service";
import {User} from "../../user/user";
import {UserService} from "../../user/user.service";
import {WebSocketServiceWrapper} from "../../websocket/web-socket-service-wrapper.service";
import {Project} from "../project";
import {ProjectVersion} from "../project-version";
import {ProjectService} from "../project.service";
import {TranslateService} from "@ngx-translate/core";

class ColumnDefinition {

  constructor(private _key: string, private _label: string, private _getValue: (ProjectVersion) => any, private _getComparisonValue?: (ProjectVersion) => any) {
  }

  get key(): string {
    return this._key;
  }

  get label(): string {
    return this._label;
  }

  public getValue(projectVersion: ProjectVersion): any {
    return this._getValue(projectVersion);
  }

  public getComparisonValue(projectVersion: ProjectVersion) {
    return this._getComparisonValue ? this._getComparisonValue(projectVersion) : this._getValue(projectVersion);
  }

}

@Component({
  selector: 'project-version-list',
  templateUrl: './project-version-list.component.html',
  styleUrls: ['./project-version-list.component.scss']
})
export class ProjectVersionListComponent implements OnInit, OnDestroy {

  public filteredProjectVersions: Array<ProjectVersion> = [];
  public sortedProjectVersions: Array<ProjectVersion>;
  public nameColumn: ColumnDefinition;
  public imageDateColumn: ColumnDefinition;
  public urlColumn: ColumnDefinition;
  public deploymentStatusColumn: ColumnDefinition;
  public deploymentDateColumn: ColumnDefinition;
  public allColumns: Array<ColumnDefinition>;
  public activeColumnKeys: Array<string>;
  public project: Project;
  public pageSettings = {
    pageSize: 10,
    pageSizeOptions: [10, 25, 50, 100]
  };
  private projectVersions: Array<ProjectVersion> = [];
  private editingUser: User;
  private updateSubscription?: Subscription;
  private pageEvent: PageEvent;
  private sort: Sort;

  constructor(private rest: RestService,
              private userService: UserService,
              private projectService: ProjectService,
              private route: ActivatedRoute,
              private wsService: WebSocketServiceWrapper,
              private readonly translate: TranslateService) {
    this.nameColumn = new ColumnDefinition('name', this.translate.instant('components.project.versionList.name'), (projectVersion: ProjectVersion) => projectVersion.name);
    this.imageDateColumn = new ColumnDefinition('image_updated_date', this.translate.instant('components.project.versionList.imageUpdatedDate'), (projectVersion: ProjectVersion) => projectVersion.formattedImageUpdatedDate, (projectVersion: ProjectVersion) => projectVersion.imageUpdatedDate);
    this.urlColumn = new ColumnDefinition('url', this.translate.instant('components.project.versionList.deploymentUrl'), (projectVersion: ProjectVersion) => projectVersion.urls.length > 0 ? projectVersion.urls[0] : '');
    this.deploymentStatusColumn = new ColumnDefinition('deployment_status', this.translate.instant('components.project.versionList.deploymentStatus'), (projectVersion: ProjectVersion) => projectVersion.deployment.status);
    this.deploymentDateColumn = new ColumnDefinition('deployment_date', this.translate.instant('components.project.versionList.deploymentDate'), (projectVersion: ProjectVersion) => projectVersion.deployment.formattedTimestamp, (projectVersion: ProjectVersion) => projectVersion.deployment.timestamp);

    this.allColumns = [this.nameColumn, this.imageDateColumn, this.urlColumn, this.deploymentStatusColumn, this.deploymentDateColumn];
    this.activeColumnKeys = [this.nameColumn.key, this.imageDateColumn.key, this.deploymentDateColumn.key, this.deploymentStatusColumn.key];

    this.sort = {active: this.imageDateColumn.key, direction: 'desc'};
    this.userService.currentUser().subscribe(currentUser => this.editingUser = currentUser);
  }

  private static compare(a: any, b: any, isAsc: boolean): number {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }

  ngOnInit() {
    this.route.paramMap.pipe(
      switchMap((params: ParamMap) => this.rest.project().getProjectById(params.get('id')))
    ).subscribe(project => {
      this.project = project;
      this.projectVersions = project.versions;
      this.filteredProjectVersions = project.versions;
      this.sortProjectVersions(this.sort);
      this.updateSubscription = this.wsService.getProjectVersionChanges(this.project.uuid)
        .subscribe(message => {
          for (let i = 0; i < this.project.versions.length; i++) {
            let pv = this.project.versions[i];
            if (pv.uuid === message.projectVersionId) {
              this.project.versions[i].deployment.updateWith(message);
              this.project.versions[i].outdated = message.outdated;
              this.project.versions[i].imageUpdatedDate = message.imageUpdatedDate;
              break;
            }
          }
          this.sortProjectVersions(this.sort);
        });
    });
  }

  ngOnDestroy() {
    if (this.updateSubscription) {
      this.updateSubscription.unsubscribe();
    }
  }

  applyFilter(filter: string) {
    this.filteredProjectVersions = this.projectVersions.filter(version => version.name.toLocaleLowerCase().includes(filter.toLocaleLowerCase()));
    this.sortProjectVersions(this.sort);
  }

  public sortProjectVersions(sort?: Sort) {
    this.sort = sort;
    let first = 0;
    let pageSize = this.pageSettings.pageSize;
    if (this.pageEvent) {
      first = this.pageEvent.pageIndex * this.pageEvent.pageSize;
      pageSize = this.pageEvent.pageSize;
    }
    this.sortedProjectVersions = this.getSortedProjectVersions(sort).slice(first, first + pageSize);
  }

  public paginationEvent(event: PageEvent) {
    this.pageEvent = event;
    this.sortProjectVersions(this.sort);
  }

  public getSortedProjectVersions(sort?: Sort): Array<ProjectVersion> {
    const data = this.filteredProjectVersions.slice();
    if (!sort || !sort.active || sort.direction == '') {
      return data;
    }
    let columnDefinition = this.allColumns.find(c => c.key === sort.active);

    return data.sort((a, b) => {
      let isAsc = sort.direction === 'asc';
      return ProjectVersionListComponent.compare(columnDefinition.getComparisonValue(a), columnDefinition.getComparisonValue(b), isAsc);
    });
  }

  public isColumnActive(col: string): boolean {
    return this.activeColumnKeys.indexOf(col) !== -1;
  }

  public getActiveColumns(): Array<ColumnDefinition> {
    return this.allColumns.filter(c => this.activeColumnKeys.includes(c.key));
  }
}
