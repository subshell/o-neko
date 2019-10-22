import {Component, OnDestroy} from "@angular/core";
import {Subscription} from "rxjs";
import {DeployableStatus, DesiredState} from "../../deployable/deployment";
import {RestService} from "../../rest/rest.service";
import {WebSocketServiceWrapper} from "../../websocket/web-socket-service-wrapper.service";
import {Project} from "../project";
import {ProjectVersion} from "../project-version";

@Component({
  selector: 'project-dashboard',
  templateUrl: './project-dashboard.component.html',
  styleUrls: ['./project-dashboard.component.scss']
})
export class ProjectDashboardComponent implements OnDestroy {

  public projects: Array<Project> = [];
  public currentlyShownProjects: Array<string> = [];
  private updateSubscriptions: Array<Subscription> = [];

  constructor(private rest: RestService,
              private wsService: WebSocketServiceWrapper) {
    this.rest.project().getAllProjects().subscribe(projects => {
      this.projects = projects.sort((a, b) => a.name.localeCompare(b.name, "en"));
      for (let project of this.projects) {
        this.updateSubscriptions.push(this.wsService.getProjectVersionChanges(project.uuid)
          .subscribe(message => {
            for (let i = 0; i < project.versions.length; i++) {
              let pv = project.versions[i];
              if (pv.uuid === message.deployableId) {
                project.versions[i].desiredState = message.desiredState;
                project.versions[i].deployment.updateWith(message);
                project.versions[i].outdated = message.outdated;
                project.versions[i].imageUpdatedDate = message.imageUpdatedDate;
                break;
              }
            }
          }));
        this.currentlyShownProjects.push(...this.projects.filter(project => this.getDeployedVersionsOfProject(project).length > 0).map(project => project.uuid));
      }
    });
  }

  get selectedProjects(): Array<Project> {
    if (this.currentlyShownProjects.length === 0) {
      return this.projects;
    } else {
      return this.projects.filter(project => this.currentlyShownProjects.includes(project.uuid));
    }
  }

  ngOnDestroy() {
    this.updateSubscriptions.forEach(subscription => subscription.unsubscribe());
  }

  public getDeployedVersionsOfProject(project: Project): Array<ProjectVersion> {
    return project.versions.filter(version => version.desiredState === DesiredState.Deployed || this.shouldShowStatusInDashboardRegardlessOfDesiredState(version));
  }

  private shouldShowStatusInDashboardRegardlessOfDesiredState(version: ProjectVersion): boolean {
    if (!version.deployment) {
      return false;
    }

    switch (version.deployment.status) {
      case DeployableStatus.Pending:
      case DeployableStatus.Running:
      case DeployableStatus.Failed:
        return true;
      default:
        return false;
    }
  }

}
