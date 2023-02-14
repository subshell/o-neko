import {Observable} from "rxjs";
import {EffectiveDeployableConfiguration} from "../deployable/effective-deployable-configuration";
import {Project} from "../project/project";
import {ProjectVersion} from "../project/project-version";

export interface ProjectRestClient {

  getAllProjects(): Observable<Array<Project>>;

  getProjectById(uuid: string): Observable<Project>;

  persistProject(project: Project): Observable<Project>;

  deleteProject(project: Project): Observable<void>;

  deployProjectVersion(version: ProjectVersion, project: Project): Observable<void>;

  stopDeployment(version: ProjectVersion, project: Project): Observable<void>;

  getCalculatedProjectVersionConfiguration(version: ProjectVersion, project: Project): Observable<EffectiveDeployableConfiguration>;

}
