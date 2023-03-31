import {Injectable} from "@angular/core";
import {CachingProjectRestClient} from "../rest/caching-project-rest-client";
import {BehaviorSubject, Observable, sampleTime, Subject} from "rxjs";
import {ProjectSearchResultEntry, SearchResult, VersionSearchResultEntry} from "./search.model";
import {map, mergeMap, shareReplay} from "rxjs/operators";

export interface SearchInputChanged {
  input: string;
  cause?: any;
}
@Injectable()
export class SearchMiddleware {

  public readonly searchInput$: Observable<SearchInputChanged> = new BehaviorSubject<SearchInputChanged>({input: ""});
  public readonly result$: Observable<SearchResult>;
  public readonly foundProjects$: Observable<Array<ProjectSearchResultEntry>>;
  public readonly foundVersions$: Observable<Array<VersionSearchResultEntry>>;

  constructor(private api: CachingProjectRestClient) {
    this.result$ = this.searchInput$.pipe(
      sampleTime(200),
      mergeMap(sic => this.api.findProjectsOrVersions(sic.input)),
      shareReplay()
    );

    this.foundProjects$ = this.result$.pipe(map(r => r.projects));
    this.foundVersions$ = this.result$.pipe(
      map(r => r.versions)
    );
  }

  public searchInputChanged(input: string, cause?: any) {
    (this.searchInput$ as Subject<SearchInputChanged>).next({input, cause});
  }

}
