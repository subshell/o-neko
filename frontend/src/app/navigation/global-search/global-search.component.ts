import {Component, ElementRef, Inject, OnInit, Renderer2, ViewChild} from "@angular/core";
import {DOCUMENT} from "@angular/common";
import {RestService} from "../../rest/rest.service";
import {FormControl} from "@angular/forms";
import {SearchResultEntry} from "../../search/search.model";
import {Observable} from "rxjs";
import {map, mergeMap, startWith} from "rxjs/operators";

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


  results$: Observable<Array<SearchResultEntry>>;
  foundVersions$: Observable<Array<SearchResultEntry>>;
  foundVersionsLimited$: Observable<Array<SearchResultEntry>>;
  foundVersionCount$: Observable<number>;
  foundProjects$: Observable<Array<SearchResultEntry>>;
  foundProjectsLimited$: Observable<Array<SearchResultEntry>>;
  foundProjectCount$: Observable<number>;

  constructor(private renderer: Renderer2,
              @Inject(DOCUMENT) document: Document,
              private api: RestService,
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
    this.renderer.listen(document, eventName, () => {
      this.inputElement.nativeElement.focus();
      this.showSearchResultBox = true;
    });
    this.renderer.listen(document, 'keydown.escape', () => this.hideResults());
    this.renderer.listen(document, 'click', (event: MouseEvent) => {
      if (!(event.composedPath().includes(this.elementRef.nativeElement))) { // detect outside clicks
        this.hideResults();
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
    if (this.results$) {
      return;
    }
    this.results$ = this.inputControl.valueChanges.pipe(startWith(""), mergeMap(inputContent => this.api.project().findProjectsOrVersions(inputContent)));
    this.inputControl.valueChanges.subscribe(() => this.showSearchResultBox = true);
    this.foundVersions$ = this.results$.pipe(map(results => results.filter(entry => entry.type === "PROJECT_VERSION")));
    this.foundVersionsLimited$ = this.foundVersions$.pipe(map(r => r.slice(0, this.displayedEntriesLimit)));
    this.foundVersionCount$ = this.foundVersions$.pipe(map(r => r.length));
    this.foundProjects$ = this.results$.pipe(map(results => results.filter(entry => entry.type === "PROJECT")));
    this.foundProjectsLimited$ = this.foundProjects$.pipe(map(r => r.slice(0, this.displayedEntriesLimit)));
    this.foundProjectCount$ = this.foundProjects$.pipe(map(r => r.length));
  }

  clearSearch() {
    this.inputControl.setValue('');
    this.inputElement.nativeElement.focus();
  }
}
