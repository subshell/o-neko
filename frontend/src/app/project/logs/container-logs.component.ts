import {AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {ActivatedRoute, ParamMap} from "@angular/router";
import {RestService} from "../../rest/rest.service";
import {Project} from "../project";
import {ProjectVersion} from "../project-version";
import {Container, PodAndContainer} from "./model";
import {MatLegacySelectChange} from "@angular/material/legacy-select";
import {WebSocketServiceWrapper} from "../../websocket/web-socket-service-wrapper.service";
import {combineLatest, Observable, ReplaySubject, Subject, zip} from "rxjs";
import {map, shareReplay, startWith, tap} from "rxjs/operators";
import {AnsiUp} from "ansi_up";
import {FormControl} from "@angular/forms";

@Component({
  selector: 'on-container-logs',
  templateUrl: './container-logs.component.html',
  styleUrls: ['./container-logs.component.scss']
})
export class ContainerLogsComponent implements OnInit, AfterViewInit, OnDestroy {

  project: Project;
  projectVersion: ProjectVersion;
  podAndContainers: Array<PodAndContainer> = [];
  selectedPod: PodAndContainer;
  selectedContainer: Container;
  lines: Array<string> = [];
  lines$: Observable<Array<string>> = new ReplaySubject(1);
  filteredLines$: Observable<Array<string>>;
  filterControl = new FormControl("");
  error = false;

  private ansiUp = new AnsiUp();

  @ViewChild('console') console: ElementRef<HTMLDivElement>;

  constructor(private route: ActivatedRoute,
              private rest: RestService,
              private wsService: WebSocketServiceWrapper) {
    this.ansiUp.use_classes = true;
  }

  ngOnInit() {
    this.route.paramMap.subscribe((params: ParamMap) => {
      let projectId = params.get('id');
      let projectVersionId = params.get('versionId');
      zip(this.initPodsAndContainers(projectId, projectVersionId), this.initProjectAndVersion(projectId, projectVersionId))
        .subscribe({
          next: () => {
            this.updateLogSubscription();
          },
          error: error => {
            this.error = true;
          }
        })
    });
    this.wsService.getLogStream().subscribe(message => {
      const keepScrollPositionAtBottom = this.isScrolledToBottom();
      this.lines.push(...message.lines);
      (this.lines$ as Subject<Array<string>>).next(this.lines)
      if (keepScrollPositionAtBottom) {
        this.scrollToBottom();
      }
    });
    this.filterControl.valueChanges.subscribe(() => this.scrollToBottom());
    this.filteredLines$ = combineLatest([this.lines$, this.filterControl.valueChanges.pipe(startWith(""))]).pipe(
      map(([lns, filterValue]) => lns.filter(l => l.toLowerCase().includes(filterValue.toLowerCase()))),
      map(lns => lns.map(l => this.ansiUp.ansi_to_html(l)))
    );
  }

  ngAfterViewInit() {
    this.scrollToBottom();
  }

  private initProjectAndVersion(projectId: string, versionId: string): Observable<any> {
    return this.rest.project().getProjectById(projectId).pipe(tap(project => {
      this.project = project;
      this.projectVersion = project.versions.find(v => v.uuid === versionId);
    }), shareReplay());
  }

  private initPodsAndContainers(projectId: string, versionId: string): Observable<any> {
    return this.rest.logs().getPodsAndContainers(projectId, versionId).pipe(tap(pac => {
      this.podAndContainers = pac;
      this.selectedPod = this.podAndContainers.length ? this.podAndContainers[0] : null;
      this.selectedContainer = this.selectedPod ? this.selectedPod.containers[0] : null;
    }));
  }

  ngOnDestroy() {
    this.wsService.unsubscribeFromLogs();
  }

  selectedPodChanged($event: MatLegacySelectChange) {
    this.selectedPod = $event.value;
    this.selectedContainer = this.selectedPod.containers[0];
    this.updateLogSubscription();
  }

  selectedContainerChanged($event: MatLegacySelectChange) {
    this.selectedContainer = $event.value;
    this.updateLogSubscription();
  }

  private updateLogSubscription() {
    this.wsService.unsubscribeFromLogs();
    this.lines = [];
    this.wsService.streamLogs(this.project.uuid, this.projectVersion.uuid, this.selectedPod.podName, this.selectedContainer.name);
  }

  scrollToBottom() {
    if (this.console?.nativeElement) {
      setTimeout(() => { // required to let the browser render changes first
        this.console.nativeElement.scrollTo({
          top: this.console.nativeElement.scrollHeight
        });
      });
    }
  }

  private isScrolledToBottom(): boolean {
    const nativeElement = this.console.nativeElement;
    if (nativeElement) {
      return this.equalsWithDelta(nativeElement.scrollTop, (nativeElement.scrollHeight - nativeElement.clientHeight), 12);
    } else {
      return true;
    }
  }

  private equalsWithDelta(a: number, b: number, delta: number): boolean {
    return Math.abs(a - b) <= delta;
  }

  download() {
    const element = document.createElement('a');
    try {
      element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(this.lines.join('\n')));
      element.setAttribute('download', `${this.selectedPod.podName}_${this.selectedContainer.name}.log`);
      element.style.display = 'none';
      document.body.appendChild(element);
      element.click();
    } finally {
      document.body.removeChild(element);
    }
  }

  clearFilter() {
    this.filterControl.setValue("");
  }
}
