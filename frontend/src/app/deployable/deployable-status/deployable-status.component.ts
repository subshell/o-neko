import {Component, Input, OnDestroy} from "@angular/core";
import {interval} from "rxjs";
import {Subscription} from "rxjs/internal/Subscription";
import {DeployableStatus} from "../deployment";


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

  @Input() set status(status: DeployableStatus) {
    this._status = status;
    if (status === DeployableStatus.Pending) {
      this.createInterval();
    } else {
      this.destroyInterval();
    }
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

  get tooltip(): string {
    switch (this.status) {
      case DeployableStatus.NotScheduled:
        return 'Not Scheduled';
      case DeployableStatus.Unknown:
        return 'Unknown';
      case DeployableStatus.Failed:
        return 'Failed';
      case DeployableStatus.Succeeded:
        return 'Succeeded';
      case DeployableStatus.Pending:
        return 'Pending';
      case DeployableStatus.Running:
        return 'Running';
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
