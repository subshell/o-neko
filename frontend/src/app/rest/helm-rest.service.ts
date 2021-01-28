import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {map} from "rxjs/operators";
import {HelmRegistry, HelmRegistryDTO} from "../registries/helm/helm-registry";
import {RegistryPasswordDto} from "../registries/registry-password-dto";

export class HelmRestService {

  private readonly root_path: string;

  constructor(private http: HttpClient, parentRootPath: string) {
    this.root_path = parentRootPath + '/helm/registries';
  }

  public getAllHelmRegistries(): Observable<Array<HelmRegistry>> {
    return this.http.get<Array<HelmRegistryDTO>>(this.root_path).pipe(map(dtos => dtos.map(HelmRegistry.from)));
  }

  public getHelmRegistryById(uuid: string): Observable<HelmRegistry> {
    return this.http.get<HelmRegistryDTO>(`${this.root_path}/${uuid}`).pipe(map(dto => HelmRegistry.from(dto)));
  }

  public persistHelmRegistry(helmRegistry: HelmRegistry, password: string): Observable<HelmRegistry> {
    if (helmRegistry.isNew()) {
      //create new helmRegistry call
      return this.http.post<HelmRegistryDTO>(this.root_path, {...helmRegistry, password}).pipe(map(helmRegistryDTO => HelmRegistry.from(helmRegistryDTO)));;
    }

    //update the existing project
    return this.http.post<HelmRegistryDTO>(`/${this.root_path}/${helmRegistry.id}`, helmRegistry).pipe(map(helmRegistryDTO => HelmRegistry.from(helmRegistryDTO)));
  }

  public changeHelmRegistryPassword(helmRegistry: HelmRegistry, passwordDTO: RegistryPasswordDto): Observable<HelmRegistry> {
    return this.http.post<HelmRegistryDTO>(`/${this.root_path}/${helmRegistry.id}/password`, passwordDTO).pipe(map(HelmRegistry.from));
  }

  public deleteHelmRegistry(helmRegistry: HelmRegistry): Observable<any> {
    return this.http.delete(`/${this.root_path}/${helmRegistry.id}`);
  }

  public getNamesOfProjectsUsingRegistry(dockerRegistry: HelmRegistry): Observable<Array<string>> {
    return this.http.get<Array<string>>(`/${this.root_path}/${dockerRegistry.id}/projects`);
  }
}
