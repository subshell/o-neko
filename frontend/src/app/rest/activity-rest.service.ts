import {HttpClient} from "@angular/common/http";
import {map} from "rxjs/operators";
import {Page} from "./page";
import {Observable} from "rxjs";
import {Activity, ActivityDTO} from "../activity/activity";

export class ActivityRestService {

  private readonly root_path: string;

  constructor(private http: HttpClient, parentRootPath: string) {
    this.root_path = parentRootPath + '/activity';
  }

  public getAllActivities(pageIndex: number = 0, pageSize: number = 10, sort='date,desc'): Observable<Page<Activity>> {
    return this.http.get<Page<ActivityDTO>>(this.root_path, {
      params: {
        page: pageIndex + '',
        size: pageSize + '',
        sort
      }
    }).pipe(map(page => ({
      ...page,
      content: page.content.map(content => new Activity(content))
    })));
  }
}
