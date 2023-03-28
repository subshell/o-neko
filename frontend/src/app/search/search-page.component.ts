import {ChangeDetectionStrategy, Component} from "@angular/core";
import {PageEvent} from "@angular/material/paginator";
import {FormControl} from "@angular/forms";
import {combineLatest, Observable, ReplaySubject, sampleTime, Subject} from "rxjs";
import {ProjectSearchResultEntry, SearchResult, VersionSearchResultEntry} from "./search.model";
import {ProjectVersion} from "../project/project-version";
import {Project} from "../project/project";
import {ActivatedRoute} from "@angular/router";
import {CachingProjectRestClient} from "../rest/caching-project-rest-client";
import {map, mergeMap, shareReplay, startWith} from "rxjs/operators";

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
export class SearchPageComponent {
  pageSizeOptions: [5, 10, 25, 50];
  inputControl = new FormControl("");

  result$: Observable<SearchResult>;
  paginatedProjects$: Observable<Array<ProjectSearchResultEntry>>;
  paginatedVersions$: Observable<Array<EnrichedVersionSearchResult>> = new ReplaySubject(1);

  currentPage$: Observable<PageOptions> = new ReplaySubject(1);

  constructor(route: ActivatedRoute,
              private api: CachingProjectRestClient) {
    this.setPage(0, 10);
    route.queryParams.subscribe(params => {
      this.inputControl.setValue(params.q || '');
    });
    this.inputControl.valueChanges.subscribe(value => {
      this.updateCurrentUrl(value);
      this.setPage(0, 10);
    });
    this.result$ = this.inputControl.valueChanges
      .pipe(startWith(""), sampleTime(200), mergeMap(inputContent => this.api.findProjectsOrVersions(inputContent)), shareReplay());

    this.paginatedProjects$ = combineLatest([this.currentPage$, this.result$]).pipe(
      map(([page, result]) => {
        return result.projects.slice(page.pageIndex * page.pageSize, page.pageSize);
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
}
