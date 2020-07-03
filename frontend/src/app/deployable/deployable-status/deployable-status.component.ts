import {Component, Input, OnDestroy} from "@angular/core";
import {EMPTY, interval, Observable} from "rxjs";
import {Subscription} from "rxjs/internal/Subscription";
import {DeployableStatus} from "../deployment";
import {TranslateService} from "@ngx-translate/core";


@Component({
  selector: 'deployableStatus',
  templateUrl: './deployable-status.component.html',
  styleUrls: ['./deployable-status.component.scss']
})
export class DeployableStatusComponent implements OnDestroy {

  deployableStatus = DeployableStatus;
  public activeButton: boolean = true;
  @Input() outdated: boolean;
  private interval: Subscription;

  private _status: DeployableStatus;

  get status(): DeployableStatus {
    return this._status;
  }

  tooltip: Observable<string> = EMPTY;


  @Input() set status(status: DeployableStatus) {
    this._status = status;
    this.tooltip = this.translateService.get(`components.deployableStatus.${this.status || 'Unknown'}`);
    if (status === DeployableStatus.Pending) {
      this.createInterval();
    } else {
      this.destroyInterval();
    }
  }

  constructor(private translateService: TranslateService) {
  }

  get iconName(): string {
    switch (this.status) {
      case DeployableStatus.NotScheduled:
        return 'close-circle';
      case DeployableStatus.Unknown:
        return 'help-circle';
      case DeployableStatus.Failed:
        return 'alert-circle';
      case DeployableStatus.Succeeded:
        return 'star-circle';
      default:
        return 'check-circle';
    }
  }

  get iconClass(): string {
    switch (this.status) {
      case DeployableStatus.NotScheduled:
        return 'yellow';
      case DeployableStatus.Unknown:
        return 'gray';
      case DeployableStatus.Failed:
        return 'red';
      case DeployableStatus.Succeeded:
        return 'green';
      default:
        return 'green';
    }
  }

  ngOnDestroy(): void {
    this.destroyInterval();
  }

  private createInterval() {
    this.interval = interval(800).subscribe(() => {
      this.toggle();
    });
  }

  private destroyInterval() {
    if (this.interval) {
      this.interval.unsubscribe();
    }
  }

  private toggle() {
    this.activeButton = !this.activeButton;
  }
}
