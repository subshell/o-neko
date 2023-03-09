import {Component, EventEmitter, Input, Output} from "@angular/core";

@Component({
  selector: 'deployable-actions',
  templateUrl: './deployable-actions.html',
  styleUrls: ['./deployable-actions.scss']
})
export class DeployableActionsComponent {

  @Input() hideIcons: boolean = false;
  @Input() hideText: boolean = false;
  @Input() isDeployed: boolean = false;
  @Input() isDeployDisabled: boolean = false;
  @Input() isStopDisabled: boolean = false;
  @Input() userHasPermissions: boolean = false;

  @Input() overrideDeployLabel: string = '';
  @Input() overrideStopLabel: string = '';

  @Output()
  public onDeploy: EventEmitter<void> = new EventEmitter();
  @Output()
  public onStop: EventEmitter<void> = new EventEmitter();

  constructor() {
  }

  public deploy(): void {
    this.onDeploy.emit();
  }

  public stop(): void {
    this.onStop.emit();
  }

}
