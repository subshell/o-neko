import {ChangeDetectionStrategy, Component, OnDestroy} from "@angular/core";
import {PageEvent} from "@angular/material/paginator";
import {BehaviorSubject, combineLatest, Observable, of, Subject, Subscription} from "rxjs";
import {ProjectSearchResultEntry, SearchResult} from "./search.model";
import {ActivatedRoute} from "@angular/router";
import {CachingProjectRestClient} from "../rest/caching-project-rest-client";
import {map, mergeMap} from "rxjs/operators";
import {SearchMiddleware} from "./search-middleware.service";
import {ProjectAndVersion} from "../project/project.service";

class PageOptions {
  constructor(public readonly pageIndex: number, public readonly pageSize: number) {
  }

  public startIndexIncl(): number {
    return this.pageIndex * this.pageSize;
  }

  public endIndexExcl(): number {
    return this.startIndexIncl() + this.pageSize;
  }
}

@Component({
  selector: 'on-search-page',
  templateUrl: './search-page.component.html',
  styleUrls: ['./search-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SearchPageComponent implements OnDestroy {

  pageSizeOptions = [5, 10, 25, 50];

  isInputBlank$: Observable<boolean>;
  result$: Observable<SearchResult>;
  paginatedProjects$: Observable<Array<ProjectSearchResultEntry>>;
  paginatedVersions$: Observable<Array<ProjectAndVersion>>;
  projectsCurrentPage$: Observable<PageOptions> = new BehaviorSubject(new PageOptions(0, 10));
  versionsCurrentPage$: Observable<PageOptions> = new BehaviorSubject(new PageOptions(0, 10));

  private subscriptions: Array<Subscription> = [];

  constructor(route: ActivatedRoute,
              private api: CachingProjectRestClient,
              private search: SearchMiddleware) {
    this.resetPagination();
    this.isInputBlank$ = this.search.searchInput$.pipe(
      map(input => input.input?.length === 0)
    );
    this.result$ = this.search.result$;
    this.subscriptions.push(
      route.queryParams.subscribe(params => {
        this.search.searchInputChanged(params.q || '', this);
      }),
      this.search.searchInput$.subscribe(value => {
        this.updateCurrentUrl(value.input);
        this.resetPagination();
      })
    );
    this.paginatedProjects$ = combineLatest([this.projectsCurrentPage$, this.search.foundProjects$]).pipe(
      map(([page, projects]) => {
        return projects.slice(page.startIndexIncl(), page.endIndexExcl());
      })
    );
    this.paginatedVersions$ = combineLatest([this.versionsCurrentPage$, this.search.foundVersions$]).pipe(
      map(([page, v]) => v.slice(page.startIndexIncl(), page.endIndexExcl())),
      mergeMap(r => combineLatest([of(r), ...r.map(v => this.api.getProjectById(v.projectId))])),
      map(([r, ...projects]) => {
        return r.map((v, i) => (<ProjectAndVersion>{
          project: projects[i],
          version: projects[i].getVersionById(v.id)
        }))
      })
    );
  }

  private resetPagination() {
    this.setProjectsPage(0, 10);
    this.setVersionsPage(0, 10);
  }

  projectsPaginationEvent($event: PageEvent) {
    this.setProjectsPage($event.pageIndex, $event.pageSize);
  }

  private setProjectsPage(pageIndex: number, pageSize: number) {
    (this.projectsCurrentPage$ as Subject<PageOptions>).next(new PageOptions(pageIndex, pageSize));
  }

  versionsPaginationEvent($event: PageEvent) {
    this.setVersionsPage($event.pageIndex, $event.pageSize);
  }

  private setVersionsPage(pageIndex: number, pageSize: number) {
    (this.versionsCurrentPage$ as Subject<PageOptions>).next(new PageOptions(pageIndex, pageSize));
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
