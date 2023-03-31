import {ChangeDetectionStrategy, Component, OnDestroy} from "@angular/core";
import {PageEvent} from "@angular/material/paginator";
import {combineLatest, Observable, ReplaySubject, Subject, Subscription} from "rxjs";
import {ProjectSearchResultEntry, SearchResult, VersionSearchResultEntry} from "./search.model";
import {ProjectVersion} from "../project/project-version";
import {Project} from "../project/project";
import {ActivatedRoute} from "@angular/router";
import {CachingProjectRestClient} from "../rest/caching-project-rest-client";
import {map} from "rxjs/operators";
import {SearchMiddleware} from "./search-middleware.service";

interface EnrichedVersionSearchResult {
  version: ProjectVersion;
  project: Project;
  searchResult: VersionSearchResultEntry;
}

interface PageOptions {
  pageIndex: number,
  pageSize: number
}

@Component({
  selector: 'on-search-page',
  templateUrl: './search-page.component.html',
  styleUrls: ['./search-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SearchPageComponent implements OnDestroy {

  pageSizeOptions: [5, 10, 25, 50];

  result$: Observable<SearchResult>;
  paginatedProjects$: Observable<Array<ProjectSearchResultEntry>>;
  paginatedVersions$: Observable<Array<EnrichedVersionSearchResult>> = new ReplaySubject(1);

  currentPage$: Observable<PageOptions> = new ReplaySubject(1);

  private subscriptions: Array<Subscription> = [];

  constructor(route: ActivatedRoute,
              private api: CachingProjectRestClient,
              private search: SearchMiddleware) {
    this.setPage(0, 10);
    this.result$ = this.search.result$;
    this.subscriptions.push(
      route.queryParams.subscribe(params => {
        this.search.searchInputChanged(params.q || '', this);
      }),
      this.search.searchInput$.subscribe(value => {
        this.updateCurrentUrl(value.input);
        this.setPage(0, 10);
      })
    );
    this.paginatedProjects$ = combineLatest([this.currentPage$, this.search.foundProjects$]).pipe(
      map(([page, projects]) => {
        return projects.slice(page.pageIndex * page.pageSize, page.pageSize);
      })
    );
  }

  paginationEvent($event: PageEvent) {
    this.setPage($event.pageIndex, $event.pageSize);
  }

  private setPage(pageIndex: number, pageSize: number) {
    (this.currentPage$ as Subject<PageOptions>).next({pageSize: pageSize, pageIndex: pageIndex});
  }

  updateCurrentUrl(query: string) {
    let url = new URL(window.location.toString());
    url.searchParams.set("q", query);
    window.history.replaceState(null, '', url.toString());
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(s => s.unsubscribe());
  }
}
