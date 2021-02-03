import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {map} from "rxjs/operators";
import {Namespace, NamespaceDTO} from "../namespace/namespace";

export class DefinedNamespaceRestService {

  private readonly root_path: string;

  constructor(private http: HttpClient, parentRootPath: string) {
    this.root_path = parentRootPath + '/namespace';
  }

  public getAllDefinedNamespaces(): Observable<Array<Namespace>> {
    return this.http.get<Array<NamespaceDTO>>(this.root_path).pipe(map(dtos => dtos.map(Namespace.from)));
  }

  public getDefinedNamespaceById(uuid: string): Observable<Namespace> {
    return this.http.get<NamespaceDTO>(`${this.root_path}/${uuid}`).pipe(map(dto => Namespace.from(dto)));
  }

  public persistDefinedNamespace(namespace: Namespace): Observable<Namespace> {
    if (namespace.isNew()) {
      //create new dockerRegistry call
      return this.http.post<NamespaceDTO>(this.root_path, namespace).pipe(map(dto => Namespace.from(dto)));
    } else {
      //update the existing project
      return this.http.post<NamespaceDTO>(`/${this.root_path}/${namespace.id}`, namespace).pipe(map(dto => Namespace.from(dto)));
    }
  }

  public deleteDefinedNamespace(namespace: Namespace): Observable<any> {
    return this.http.delete(`/${this.root_path}/${namespace.id}`);
  }


}
