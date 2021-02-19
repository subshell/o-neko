import {Component, OnDestroy, OnInit} from "@angular/core";
import {Subscription} from "rxjs";
import {RestService} from "../../rest/rest.service";
import {WebSocketServiceWrapper} from "../../websocket/web-socket-service-wrapper.service";
import {PageEvent} from "@angular/material/paginator";
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
})
export class ActivityLogComponent implements OnInit, OnDestroy {
  private subscriptions: Array<Subscription> = [];
  private _activities: Array<Activity>;

  public total = 0;
  public pageSettings = {
    pageSize: 10,
    pageSizeOptions: [10, 25, 50, 100],
    pageIndex: 0
  };
  public pendingActivities: Array<Activity> = [];

  constructor(private rest: RestService,
              private ws: WebSocketServiceWrapper) {
    this._activities = [];
  }

  public get activities(): Array<Activity> {
    return this._activities;
  }

  public get hasPendingActivities(): boolean {
    return this.pendingActivities.length > 0;
  }

  public getIcon(activity: Activity): string {
    return (activityIconMap[activity.triggerType] || activityIconMap.unknown).icon;
  }

  ngOnInit(): void {
    this.loadStartPage();
    this.subscriptions.push(this.ws.getActivityStream().subscribe(activity => {
      this.pendingActivities.push(activity);
    }));
  }

  ngOnDestroy() {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

  public paginationEvent(page: PageEvent) {
    this.loadPage(page);
    if (page.pageIndex === 0) {
      this.pendingActivities = [];
    }
  }

  private loadStartPage() {
    this.loadPage({
      pageIndex: 0,
      pageSize: this.pageSettings.pageSize
    });
  }

  private loadPage(pageData: { pageIndex: number, pageSize: number }) {
    this.rest.activity().getAllActivities(pageData.pageIndex, pageData.pageSize).subscribe(page => {
      this.total = page.totalElements;
      this._activities = page.content;
    });
  }

  public refresh() {
    this.loadStartPage();
    this.pageSettings.pageIndex = 0;
    this.pendingActivities = [];
  }
}
