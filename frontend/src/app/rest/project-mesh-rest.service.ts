import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {map} from "rxjs/operators";
import {EffectiveDeployableConfiguration} from "../deployable/effective-deployable-configuration";
import {MeshComponent} from "../projectmesh/mesh-component";
import {ProjectMesh, ProjectMeshDTO} from "../projectmesh/project-mesh";

export class ProjectMeshRestService {

  private readonly root_path: string;

  constructor(private http: HttpClient, parentRootPath: string) {
    this.root_path = parentRootPath + '/projectMesh';
  }

  public getAllProjectMeshes(): Observable<Array<ProjectMesh>> {
    return this.http.get<Array<ProjectMeshDTO>>(this.root_path).pipe(map(dtos => dtos.map(ProjectMesh.from)));
  }

  public getProjectMeshById(id: string): Observable<ProjectMesh> {
    return this.http.get<ProjectMeshDTO>(`${this.root_path}/${id}`).pipe(map(dto => ProjectMesh.from(dto)));
  }

  public persistProjectMesh(mesh: ProjectMesh): Observable<ProjectMesh> {
    if (mesh.isNew()) {
      return this.http.post<ProjectMeshDTO>(this.root_path, mesh).pipe(map(dto => ProjectMesh.from(dto)));
    } else {
      return this.http.post<ProjectMeshDTO>(`/${this.root_path}/${mesh.id}`, mesh).pipe(map(dto => ProjectMesh.from(dto)));
    }
  }

  public deleteProjectMesh(mesh: ProjectMesh): Observable<any> {
    return this.http.delete(`/${this.root_path}/${mesh.id}`);
  }

  public deployMesh(mesh: ProjectMesh): Observable<ProjectMesh> {
    return this.http.post<ProjectMeshDTO>(`${this.root_path}/${mesh.id}/deploy`, {}).pipe(map(dto => ProjectMesh.from(dto)));
  }

  public stopDeployingMesh(mesh: ProjectMesh): Observable<void> {
    return this.http.post(`${this.root_path}/${mesh.id}/stop`, {}).pipe(map(() => null));
  }

  public getCalculatedMeshComponentConfiguration(component: MeshComponent, mesh: ProjectMesh): Observable<EffectiveDeployableConfiguration> {
    return this.http.get<EffectiveDeployableConfiguration>(`${this.root_path}/${mesh.id}/component/${component.id}/configuration`);
  }

  public deployMeshComponent(component: MeshComponent, mesh: ProjectMesh): Observable<ProjectMesh> {
    return this.http.post<ProjectMeshDTO>(`${this.root_path}/${mesh.id}/component/${component.id}/deploy`, {}).pipe(map(dto => ProjectMesh.from(dto)));
  }

  public stopDeployingMeshComponent(component: MeshComponent, mesh: ProjectMesh): Observable<ProjectMeshDTO> {
    return this.http.post<ProjectMeshDTO>(`${this.root_path}/${mesh.id}/component/${component.id}/stop`, {}).pipe(map(dto => ProjectMesh.from(dto)));
  }

}
