import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {map} from "rxjs/operators";
import {DefinedNamespace, DefinedNamespaceDTO} from "../namespace/defined-namespace";

export class DefinedNamespaceRestService {

  private readonly root_path: string;

  constructor(private http: HttpClient, parentRootPath: string) {
    this.root_path = parentRootPath + '/namespace';
  }

  public getAllDefinedNamespaces(): Observable<Array<DefinedNamespace>> {
    return this.http.get<Array<DefinedNamespaceDTO>>(this.root_path).pipe(map(dtos => dtos.map(DefinedNamespace.from)));
  }

  public getDefinedNamespaceById(uuid: string): Observable<DefinedNamespace> {
    return this.http.get<DefinedNamespaceDTO>(`${this.root_path}/${uuid}`).pipe(map(dto => DefinedNamespace.from(dto)));
  }

  public persistDefinedNamespace(namespace: DefinedNamespace): Observable<DefinedNamespace> {
    if (namespace.isNew()) {
      //create new dockerRegistry call
      return this.http.post<DefinedNamespaceDTO>(this.root_path, namespace).pipe(map(dto => DefinedNamespace.from(dto)));
    } else {
      //update the existing project
      return this.http.post<DefinedNamespaceDTO>(`/${this.root_path}/${namespace.id}`, namespace).pipe(map(dto => DefinedNamespace.from(dto)));
    }
  }

  public deleteDefinedNamespace(namespace: DefinedNamespace): Observable<any> {
    return this.http.delete(`/${this.root_path}/${namespace.id}`);
  }


}
