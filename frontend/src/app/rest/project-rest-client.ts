import {Observable} from "rxjs";
import {EffectiveDeployableConfiguration} from "../deployable/effective-deployable-configuration";
import {Project, ProjectDTO} from "../project/project";
import {ProjectVersion} from "../project/project-version";
import {SearchResult} from "../search/search.model";
import {ProjectExportDTO} from "../project/project-export";

export interface ProjectRestClient {

  getAllProjects(): Observable<Array<Project>>;

  getProjectById(uuid: string): Observable<Project>;

  persistProject(project: Project): Observable<Project>;

  persistProjectVersionVariables(project: Project, projectVersion: ProjectVersion): Observable<Project>;

  deleteProject(project: Project): Observable<void>;

  exportProject(project: Project): Observable<ProjectExportDTO>;

  deployProjectVersion(version: ProjectVersion, project: Project): Observable<void>;

  stopDeployment(version: ProjectVersion, project: Project): Observable<void>;

  getCalculatedProjectVersionConfiguration(version: ProjectVersion, project: Project): Observable<EffectiveDeployableConfiguration>;

  findProjectsOrVersions(query: string): Observable<SearchResult>;

}
