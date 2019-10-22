import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/index";
import {map} from "rxjs/operators";
import {Activity, ActivityDTO} from "../activity/activity";

export class ActivityRestService {

  private readonly root_path: string;

  constructor(private http: HttpClient, parentRootPath: string) {
    this.root_path = parentRootPath + '/activity';
  }

  public getAllActivities(pageIndex: number = 0, pageSize: number = 10): Observable<Array<Activity>> {
    return this.http.get<Array<ActivityDTO>>(this.root_path, {
      params: {
        pageIndex: String(pageIndex),
        pageSize: String(pageSize)
      }
    }).pipe(map(dtos => dtos.map(dto => new Activity(dto))));
  }
}
