import {Injectable, OnDestroy} from "@angular/core";
import {Observable, of, Subscription} from "rxjs";
import {tap} from "rxjs/operators";
import {EffectiveDeployableConfiguration} from "../deployable/effective-deployable-configuration";
import {Project, ProjectDTO} from "../project/project";
import {ProjectVersion} from "../project/project-version";
import {Cache} from "../util/cache";
import {WebSocketServiceWrapper} from "../websocket/web-socket-service-wrapper.service";
import {ProjectRestClient} from "./project-rest-client";
import {RestService} from "./rest.service";
import {SearchResult} from "../search/search.model";
import {ProjectExportDTO} from "../project/project-export";

@Injectable()
export class CachingProjectRestClient implements ProjectRestClient, OnDestroy {

  private delegate: ProjectRestClient;
  private cache: Cache;
  private subscriptions: Array<Subscription> = [];
  private allLoaded: boolean = false;

  constructor(restService: RestService, private wsService: WebSocketServiceWrapper) {
    this.delegate = restService.project();
    this.cache = new Cache();
    this.subscriptions.push(this.wsService.getActivityStream().subscribe(activity => {
      if (activity.entityType === 'Project') {
        this.cache.invalidate(activity.entityId);
        if (activity.changeType === 'Saved') {
          this.allLoaded = false;
        }
        //else case is 'Deleted', but then we just have to drop it from our cache and we're good to go.
      }
    }), this.wsService.getProjectVersionChanges().subscribe(message => {
      this.cache.invalidate(message.ownerId);
    }));
  }

  ngOnDestroy() {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
    this.cache.destroy();
    this.allLoaded = false;
  }

  getAllProjects(): Observable<Array<Project>> {
    if (this.allLoaded) {
      return of(this.cache.getAll<Project>());
    } else {
      return this.delegate.getAllProjects().pipe(tap(projects => {
        projects.forEach(project => this.cache.put(project.uuid, project));
        this.allLoaded = true;
      }));
    }
  }

  getProjectById(uuid: string): Observable<Project> {
    return this.cache.getOrElse(uuid, this.delegate.getProjectById(uuid));
  }

  persistProject(project: Project): Observable<Project> {
    return this.delegate.persistProject(project).pipe(tap(persistedProject => this.cache.put(persistedProject.uuid, persistedProject)));
  }

  persistProjectVersionVariables(project: Project, projectVersion: ProjectVersion): Observable<Project> {
    return this.delegate.persistProjectVersionVariables(project, projectVersion);
  }

  deleteProject(project: Project): Observable<void> {
    return this.delegate.deleteProject(project).pipe(tap(() => this.cache.invalidate(project.uuid)));
  }

  exportProject(project: Project): Observable<ProjectExportDTO> {
    return this.delegate.exportProject(project);
  }

  deployProjectVersion(version: ProjectVersion, project: Project): Observable<void> {
    return this.delegate.deployProjectVersion(version, project);
  }

  stopDeployment(version: ProjectVersion, project: Project): Observable<void> {
    return this.delegate.stopDeployment(version, project);
  }

  getCalculatedProjectVersionConfiguration(version: ProjectVersion, project: Project): Observable<EffectiveDeployableConfiguration> {
    return this.delegate.getCalculatedProjectVersionConfiguration(version, project);
  }

  findProjectsOrVersions(query: string): Observable<SearchResult> {
    return this.delegate.findProjectsOrVersions(query);
  }

}
