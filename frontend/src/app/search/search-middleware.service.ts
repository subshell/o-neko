import {Injectable, OnDestroy} from "@angular/core";
import {CachingProjectRestClient} from "../rest/caching-project-rest-client";
import {BehaviorSubject, Observable, sampleTime, Subject, Subscription} from "rxjs";
import {ProjectSearchResultEntry, SearchResult, VersionSearchResultEntry} from "./search.model";
import {map, mergeMap, shareReplay} from "rxjs/operators";
import {AuthService} from "../session/auth.service";

export interface SearchInputChanged {
  input: string;
  cause?: any;
}

@Injectable()
export class SearchMiddleware implements OnDestroy {

  public readonly searchInput$: Observable<SearchInputChanged> = new BehaviorSubject<SearchInputChanged>({input: ""});
  public readonly result$: Observable<SearchResult>;
  public readonly foundProjects$: Observable<Array<ProjectSearchResultEntry>>;
  public readonly foundVersions$: Observable<Array<VersionSearchResultEntry>>;

  private subscriptions: Array<Subscription> = [];

  constructor(private api: CachingProjectRestClient, auth: AuthService) {
    this.result$ = this.searchInput$.pipe(
      sampleTime(150),
      mergeMap(sic => this.api.findProjectsOrVersions(sic.input)),
      shareReplay()
    );

    this.foundProjects$ = this.result$.pipe(
      map(r => r.projects)
    );
    this.foundVersions$ = this.result$.pipe(
      map(r => r.versions)
    );

    this.subscriptions.push(
      auth.isAuthenticated().subscribe(isAuthenticated => {
        // new user logged in, we reset the search input
        if (isAuthenticated) {
          this.searchInputChanged("", "authentication changed");
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(s => s.unsubscribe());
  }

  public searchInputChanged(input: string, cause?: any) {
    (this.searchInput$ as Subject<SearchInputChanged>).next({input, cause});
  }

}
