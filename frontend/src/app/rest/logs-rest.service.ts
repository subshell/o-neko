import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {PodAndContainer} from "../project/logs/model";

export class LogsRestService {

  private readonly root_path: string;
  constructor(private http: HttpClient, parentRootPath: string) {
    this.root_path = parentRootPath + '/logs';
  }

  public getPodsAndContainers(projectId: string, versionId: string): Observable<Array<PodAndContainer>> {
    return this.http.get<Array<PodAndContainer>>(`${this.root_path}/containers/project/${projectId}/version/${versionId}`);
  }

}
