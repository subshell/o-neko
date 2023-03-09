import {ChangeDetectionStrategy, Component, ElementRef, Inject, OnDestroy, OnInit, Renderer2, ViewChild} from "@angular/core";
import {DOCUMENT} from "@angular/common";
import {FormControl} from "@angular/forms";
import {combineLatest, Observable, of, ReplaySubject, sampleTime, Subject, Subscription} from "rxjs";
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
  styleUrls: ['./global-search.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class GlobalSearchComponent implements OnInit, OnDestroy {
  @ViewChild('inputElement') inputElement: ElementRef<HTMLInputElement>;
  showSearchResultBox$: Observable<boolean> = new ReplaySubject(1);
  inputControl = new FormControl("");
  displayedEntriesLimit = 5;
  displayShortcut = ""


  result$: Observable<SearchResult>;
  foundVersionsLimited$: Observable<Array<EnrichedVersionSearchResult>>;
  foundProjectsLimited$: Observable<Array<ProjectSearchResultEntry>>;
  versionsMultiDeployModel$: Observable<Array<ProjectAndVersion>>;
  private unsubscribeOnDestroy: Array<() => void> = [];

  constructor(private renderer: Renderer2,
              @Inject(DOCUMENT) document: Document,
              private api: CachingProjectRestClient,
              private elementRef: ElementRef) {
    this.hideResults();
  }

  ngOnDestroy(): void {
    this.unsubscribeOnDestroy.forEach(u => u());
  }

  ngOnInit(): void {
    this.initShortcutsAndHideEvents();
  }

  private initShortcutsAndHideEvents() {
    const platform = navigator?.platform || 'unknown';
    const isMac = platform.toUpperCase().indexOf("MAC") >= 0;
    this.displayShortcut = isMac ? "⌘ + K" : "ctrl + K";

    const eventName = `keydown.${isMac ? 'meta' : 'ctrl'}.k`;
    this.unsubscribeOnDestroy.push(
      this.renderer.listen(document, eventName, (event: KeyboardEvent) => {
        event.preventDefault();
        this.inputElement.nativeElement.focus();
        this.showResults();
      }),
      this.renderer.listen(document, 'keydown.escape', () => {
        this.clearSearch();
        this.hideResults();
      }),
      this.renderer.listen(document, 'click', (event: MouseEvent) => {
        if (!(event.composedPath().includes(this.elementRef.nativeElement))) { // detect outside clicks
          this.hideResults();
        } else if (event.composedPath().includes(this.inputElement.nativeElement)) {
          this.inputFocused();
        }
      })
    );
  }

  inputFocused() {
    this.startRenderingResults();
    this.showResults();
  }

  hideResults() {
    (this.showSearchResultBox$ as Subject<boolean>).next(false);
  }

  private showResults() {
    (this.showSearchResultBox$ as Subject<boolean>).next(true);
  }

  private startRenderingResults() {
    if (this.result$) {
      return;
    }
    this.result$ = this.inputControl.valueChanges.pipe(startWith(""), sampleTime(200), mergeMap(inputContent => this.api.findProjectsOrVersions(inputContent)), shareReplay());
    this.addUnsubscribe(this.inputControl.valueChanges.subscribe(() => this.showResults()));
    this.initFilteredResults();
  }

  private initFilteredResults() {
    if (this.foundVersionsLimited$) {
      return;
    }

    // show a maximum of $displayedEntriesLimit projects
    this.foundProjectsLimited$ = this.result$.pipe(map(r => r.projects.slice(0, this.displayedEntriesLimit)));

    // show a maximum of $displayedEntriesLimit versions. get the project to every version and map to EnrichedVersionSearchResult
    this.foundVersionsLimited$ = this.result$.pipe(
      map(r => r.versions.slice(0, this.displayedEntriesLimit)),
      mergeMap(r => combineLatest([of(r), ...r.map(v => this.api.getProjectById(v.projectId))])),
      map(([r, ...projects]) => {
        return r.map((v, i) => (<EnrichedVersionSearchResult>{
          searchResult: v,
          project: projects[i],
          version: projects[i].getVersionById(v.id)
        }));
      })
    );

    // create the model used by the multi-deploy button from all visible versions
    this.versionsMultiDeployModel$ = this.foundVersionsLimited$.pipe(map(versions => {
      return versions.map(version => ({
        version: version.version,
        project: version.project
      }));
    }));
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

  private addUnsubscribe(subscription: Subscription) {
    this.unsubscribeOnDestroy.push(() => subscription.unsubscribe());
  }
}
