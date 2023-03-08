import {Component, ElementRef, Inject, OnInit, Renderer2, ViewChild} from "@angular/core";
import {DOCUMENT} from "@angular/common";
import {FormControl} from "@angular/forms";
import {combineLatest, Observable, of} from "rxjs";
import {map, mergeMap, shareReplay, startWith} from "rxjs/operators";
import {ProjectSearchResultEntry, SearchResult, VersionSearchResultEntry} from "../../search/search.model";
import {ProjectVersion} from "../../project/project-version";
import {Project} from "../../project/project";
import {CachingProjectRestClient} from "../../rest/caching-project-rest-client";
import {ProjectAndVersion} from "../../project/project.service";

interface EnrichedVersionSearchResult {
  version: ProjectVersion;
  project: Project;
  searchResult: VersionSearchResultEntry;
}

@Component({
  selector: 'on-global-search',
  templateUrl: './global-search.component.html',
  styleUrls: ['./global-search.component.scss']
})
export class GlobalSearchComponent implements OnInit {
  @ViewChild('inputElement') inputElement: ElementRef<HTMLInputElement>;
  showSearchResultBox = false;
  inputControl = new FormControl("");
  displayedEntriesLimit = 5;
  displayShortcut = ""


  result$: Observable<SearchResult>;
  foundVersionsLimited$: Observable<Array<EnrichedVersionSearchResult>>;
  foundProjectsLimited$: Observable<Array<ProjectSearchResultEntry>>;
  versionsMultiDeployModel$: Observable<Array<ProjectAndVersion>>;

  constructor(private renderer: Renderer2,
              @Inject(DOCUMENT) document: Document,
              private api: CachingProjectRestClient,
              private elementRef: ElementRef) {
  }

  ngOnInit(): void {
    this.initShortcutsAndHideEvents();
  }

  private initShortcutsAndHideEvents() {
    const platform = navigator?.platform || 'unknown';
    const isMac = platform.toUpperCase().indexOf("MAC") >= 0;
    this.displayShortcut = isMac ? "⌘ + K" : "ctrl + K";

    const eventName = `keydown.${isMac ? 'meta' : 'ctrl'}.k`;
    this.renderer.listen(document, eventName, (event: KeyboardEvent) => {
      this.inputElement.nativeElement.focus();
      this.showSearchResultBox = true;
      event.preventDefault();
    });
    this.renderer.listen(document, 'keydown.escape', () => {
      this.clearSearch();
      this.hideResults();
    });
    this.renderer.listen(document, 'click', (event: MouseEvent) => {
      if (!(event.composedPath().includes(this.elementRef.nativeElement))) { // detect outside clicks
        this.hideResults();
      } else if (event.composedPath().includes(this.inputElement.nativeElement)) {
        this.inputFocused();
      }
    });
  }

  inputFocused() {
    this.startRenderingResults();
    this.showSearchResultBox = true;
  }

  hideResults() {
    this.showSearchResultBox = false;
  }

  private startRenderingResults() {
    if (this.result$) {
      return;
    }
    this.result$ = this.inputControl.valueChanges.pipe(startWith(""), mergeMap(inputContent => this.api.findProjectsOrVersions(inputContent)), shareReplay());
    this.inputControl.valueChanges.subscribe(() => this.showSearchResultBox = true);
    this.foundVersionsLimited$ = this.result$.pipe(
      map(r => r.versions.slice(0, this.displayedEntriesLimit)),
      mergeMap(r => combineLatest([of(r), ...r.map(v => this.api.getProjectById(v.projectId))])),
      map(combined => {
        const r = combined[0];
        const projects = combined.slice(1) as Array<Project>;
        return r.map((v, i) => (<EnrichedVersionSearchResult>{
          searchResult: v,
          project: projects[i],
          version: projects[i].getVersionById(v.id)
        }));
      })
    );
    this.foundProjectsLimited$ = this.result$.pipe(map(r => r.projects.slice(0, this.displayedEntriesLimit)));
    this.versionsMultiDeployModel$ = this.foundVersionsLimited$.pipe(map(versions => {
      return versions.map(version => ({
        version: version.version,
        project: version.project
      }));
    }))
  }

  clearButtonClicked() {
    this.clearSearch();
    this.hideResults();
  }

  clearSearch() {
    this.inputControl.setValue('');
  }

  focusInput() {
    this.inputElement.nativeElement.focus();
  }
}
