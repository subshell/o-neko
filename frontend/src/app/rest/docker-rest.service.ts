import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {map} from "rxjs/operators";
import {DockerRegistry, DockerRegistryDTO} from "../registries/docker/docker-registry";
import {RegistryPasswordDto} from "../registries/registry-password-dto";

export class DockerRestService {

  private readonly root_path: string;

  constructor(private http: HttpClient, parentRootPath: string) {
    this.root_path = parentRootPath + '/dockerRegistry';
  }

  public getAllDockerRegistries(): Observable<Array<DockerRegistry>> {
    return this.http.get<Array<DockerRegistryDTO>>(this.root_path).pipe(map(dtos => dtos.map(DockerRegistry.from)));
  }

  public getDockerRegistryById(uuid: string): Observable<DockerRegistry> {
    return this.http.get<DockerRegistryDTO>(`${this.root_path}/${uuid}`).pipe(map(dto => DockerRegistry.from(dto)));
  }

  public persistDockerRegistry(dockerRegistry: DockerRegistry): Observable<DockerRegistry> {
    if (dockerRegistry.isNew()) {
      //create new dockerRegistry call
      return this.http.post<DockerRegistryDTO>(this.root_path, dockerRegistry).pipe(map(dockerRegistryDTO => DockerRegistry.from(dockerRegistryDTO)));
    } else {
      //update the existing project
      return this.http.post<DockerRegistryDTO>(`/${this.root_path}/${dockerRegistry.uuid}`, dockerRegistry).pipe(map(dockerRegistryDTO => DockerRegistry.from(dockerRegistryDTO)));
    }
  }

  public changeDockerRegistryPassword(dockerRegistry: DockerRegistry, passwordDTO: RegistryPasswordDto): Observable<DockerRegistry> {
    return this.http.post<DockerRegistryDTO>(`/${this.root_path}/${dockerRegistry.uuid}/password`, passwordDTO).pipe(map(DockerRegistry.from));
  }

  public deleteDockerRegistry(dockerRegistry: DockerRegistry): Observable<any> {
    return this.http.delete(`/${this.root_path}/${dockerRegistry.uuid}`);
  }

  public getNamesOfProjectsUsingRegistry(dockerRegistry: DockerRegistry): Observable<Array<string>> {
    return this.http.get<Array<string>>(`/${this.root_path}/${dockerRegistry.uuid}/projects`);
  }

}
