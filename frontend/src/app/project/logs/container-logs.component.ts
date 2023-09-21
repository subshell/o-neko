import {AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {ActivatedRoute, ParamMap} from "@angular/router";
import {RestService} from "../../rest/rest.service";
import {Project} from "../project";
import {ProjectVersion} from "../project-version";
import {PodAndContainer} from "./model";
import {MatLegacySelectChange} from "@angular/material/legacy-select";
import {WebSocketServiceWrapper} from "../../websocket/web-socket-service-wrapper.service";
import {Observable, ReplaySubject, Subject, zip} from "rxjs";
import {map, shareReplay, tap} from "rxjs/operators";

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
  selectedContainer: string;
  lines: Array<string> = [];
  lines$: Observable<Array<string>> = new ReplaySubject(1);
  filteredLines$: Observable<Array<string>>;
  filterString: string = '';
  error = false;

  @ViewChild('console') console: ElementRef<HTMLDivElement>;

  constructor(private route: ActivatedRoute,
              private rest: RestService,
              private wsService: WebSocketServiceWrapper) {
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
    this.filteredLines$ = this.lines$.pipe(map(lns => lns.filter(l => l.toLowerCase().includes(this.filterString.toLowerCase()))));
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
      this.selectedContainer = this.selectedPod ? this.selectedPod.containerNames[0] : null;
    }));
  }

  ngOnDestroy() {
    this.wsService.unsubscribeFromLogs();
  }

  selectedPodChanged($event: MatLegacySelectChange) {
    this.selectedPod = $event.value;
    this.selectedContainer = this.selectedPod.containerNames[0];
    this.updateLogSubscription();
  }

  selectedContainerChanged($event: MatLegacySelectChange) {
    this.selectedContainer = $event.value;
    this.updateLogSubscription();
  }

  private updateLogSubscription() {
    this.wsService.unsubscribeFromLogs();
    this.lines = [];
    this.wsService.streamLogs(this.project.uuid, this.projectVersion.uuid, this.selectedPod.podName, this.selectedContainer);
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
      element.setAttribute('download', `${this.selectedPod.podName}_${this.selectedContainer}.log`);
      element.style.display = 'none';
      document.body.appendChild(element);
      element.click();
    } finally {
      document.body.removeChild(element);
    }
  }

  filterChanged() {
    this.filteredLines$ = this.lines$.pipe(map(lns => lns.filter(l => l.toLowerCase().includes(this.filterString.toLowerCase()))));
    this.scrollToBottom();
  }

  clearFilter() {
    this.filterString = '';
    this.filterChanged();
  }
}
