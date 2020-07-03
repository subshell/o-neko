import {animate, style, transition, trigger} from "@angular/animations";
import {Component, ElementRef, HostListener, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {Subscription} from "rxjs";
import {ActivityRestService} from "../../rest/activity-rest.service";
import {RestService} from "../../rest/rest.service";
import {UserService} from "../../user/user.service";
import {WebSocketServiceWrapper} from "../../websocket/web-socket-service-wrapper.service";
import {Activity} from "../activity";

const activityIconMap = {
  setup: {
    icon: 'power'
  },
  unknown: {
    icon: 'help-circle-outline'
  },
  ScheduledTask: {
    icon: 'history'
  },
  UserRequest: {
    icon: 'account-circle-outline'
  }
};

@Component({
  selector: 'activity-log',
  templateUrl: './activity-log.component.html',
  styleUrls: ['./activity-log.component.scss'],
  animations: [
    trigger('slideInOut', [
      transition(':enter', [
        style([{transform: 'translateY(-40%)'}, {opacity: 0}, {height: 0}]),
        animate('220ms ease-out', style([{transform: 'translateY(0%)'}, {opacity: 1}, {height: '*'}]))
      ]),
      transition(':leave', [
        animate('220ms ease-in', style([{transform: 'translateY(-40%)'}, {opacity: 0}, {height: 0}]))
      ])
    ])
  ]
})
export class ActivityLogComponent implements OnInit, OnDestroy {

  private static readonly PAGE_SIZE = 10;
  @ViewChild('activityLogContainer', {static: true})
  public activityLogContainer: ElementRef<HTMLElement>;
  public currentlyLoading: boolean = false;
  public panelHeight: number = 500;
  private activityRest: ActivityRestService;
  private currentPageIndex: number;
  private loadedAll: boolean;
  private mostRecentDate: Date;
  private subscriptions: Array<Subscription> = [];

  constructor(rest: RestService,
              private ws: WebSocketServiceWrapper,
              private userService: UserService) {
    this.activityRest = rest.activity();
    this._results = [];
    this.currentPageIndex = 0;
    this.loadedAll = false;
  }

  private _results: Array<Activity>;

  public get results(): Array<Activity> {
    return this._results;
  }

  public get infiniteScrollDisabled(): boolean {
    return this.loadedAll || this.currentlyLoading;
  }

  public getIcon(activity: Activity): string {
    return (activityIconMap[activity.triggerType] || activityIconMap.unknown).icon;
  }

  ngOnInit(): void {
    this.userService.currentUser().subscribe(() => {
      this.internallyLoadMore(this.determineNumberOfInitialChargesToLoad());
      this.ws.getActivityStream().subscribe(activity => {
        this._results.unshift(activity);
        this.currentPageIndex = Math.floor(this.results.length / ActivityLogComponent.PAGE_SIZE);
      });
    });
  }

  ngOnDestroy() {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

  // the infinite scroll container needs a discrete height value
  @HostListener('window:resize', ['$event'])
  onResize() {
    this.panelHeight = this.getHeight();
  }

  public loadMore() {
    if (this.infiniteScrollDisabled) {
      return;
    }

    this.currentPageIndex++;
    this.internallyLoadMore();
  }

  private determineNumberOfInitialChargesToLoad(): number {
    let contentHeight = 88 * ActivityLogComponent.PAGE_SIZE;
    return Math.ceil(this.getHeight() / contentHeight);
  }

  private getHeight(): number {
    return document.body.offsetHeight - (this.activityLogContainer ? this.activityLogContainer.nativeElement.offsetTop : 0);
  }

  private internallyLoadMore(factor: number = 1) {
    this.currentlyLoading = true;
    this.activityRest.getAllActivities(this.currentPageIndex, ActivityLogComponent.PAGE_SIZE * factor).subscribe(result => {
      this.putActivitiesInOrder(result);
      if (result.length < ActivityLogComponent.PAGE_SIZE) {
        this.loadedAll = true;
      }
      if (factor > 1) {
        this.currentPageIndex += (factor - 1);
      }
      this.currentlyLoading = false;
      this.onResize();
    });
  }

  private putActivitiesInOrder(newlyLoadedActivities: Array<Activity>) {
    const resultMap = new Map<string, Activity>();
    this._results.forEach(activity => resultMap.set(activity.id, activity));
    newlyLoadedActivities.forEach(activity => resultMap.set(activity.id, activity));
    const sorted = Array.from(resultMap.values()).sort((a, b) => a.date.getTime() - b.date.getTime());
    if (sorted.length > 0) {
      this.mostRecentDate = sorted[0].date;
    }
    this._results = sorted;
  }
}


