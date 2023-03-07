import {HttpClient} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import {map} from 'rxjs/operators';
import {EffectiveDeployableConfiguration} from '../deployable/effective-deployable-configuration';
import {Project, ProjectDTO} from '../project/project';
import {ProjectVersion} from '../project/project-version';
import {ProjectRestClient} from './project-rest-client';
import {ProjectExportDTO} from '../project/project-export';
import {SearchResult} from "../search/search.model";

export class ProjectRestService implements ProjectRestClient {

  private readonly root_path: string;

  constructor(private http: HttpClient, parentRootPath: string) {
    this.root_path = parentRootPath + '/project';
  }

  public getAllProjects(): Observable<Array<Project>> {
    return this.http.get<Array<ProjectDTO>>(this.root_path).pipe(map(projectDTOs => projectDTOs.map(Project.from)));
  }

  public getProjectById(uuid: string): Observable<Project> {
    return this.http.get<ProjectDTO>(`${this.root_path}/${uuid}`).pipe(map(projectDTO => Project.from(projectDTO)));
  }

  public persistProject(project: Project): Observable<Project> {
    if (project.isNew()) {
      //create new project call
      return this.http.post<ProjectDTO>(this.root_path, project).pipe(map(projectDTO => Project.from(projectDTO)));
    } else {
      //update the existing project
      return this.http.post<ProjectDTO>(`${this.root_path}/${project.uuid}`, project).pipe(map(projectDTO => Project.from(projectDTO)));
    }
  }

  public persistProjectVersionVariables(project: Project, projectVersion: ProjectVersion) {
    if (project.isNew()) {
      return throwError("Cannot persist variables for a new project");
    }
    return this.http.post<ProjectDTO>(`${this.root_path}/${project.uuid}/version/${projectVersion.uuid}/templateVariables`, project).pipe(map(projectDTO => Project.from(projectDTO)));
  }

  public deleteProject(project: Project): Observable<void> {
    return this.http.delete(`${this.root_path}/${project.uuid}`).pipe(map(() => null));
  }

  public exportProject(project: Project): Observable<ProjectExportDTO> {
    return this.http.get(`${this.root_path}/${project.uuid}/export`) as Observable<ProjectExportDTO>;
  }

  public deployProjectVersion(version: ProjectVersion, project: Project): Observable<void> {
    return this.http.post(`${this.root_path}/${project.uuid}/version/${version.uuid}/deploy`, {}).pipe(map(() => null));
  }

  public stopDeployment(version: ProjectVersion, project: Project): Observable<void> {
    return this.http.post(`${this.root_path}/${project.uuid}/version/${version.uuid}/stop`, {}).pipe(map(() => null));
  }

  public getCalculatedProjectVersionConfiguration(version: ProjectVersion, project: Project): Observable<EffectiveDeployableConfiguration> {
    return this.http.get<EffectiveDeployableConfiguration>(`${this.root_path}/${project.uuid}/version/${version.uuid}/configuration`);
  }

  public findProjectsOrVersions(query: string): Observable<SearchResult> {
    return this.http.get<SearchResult>(`${this.root_path}/search`, {
      params: {
        query
      }
    });
  }
}
