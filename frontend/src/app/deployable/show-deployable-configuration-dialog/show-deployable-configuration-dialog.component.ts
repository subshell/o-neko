import {Component, Inject} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import "brace/mode/yaml";
//manually import theme and mode for ace - else it will try to autoload it (and fail on doing so)...
import "brace/theme/chrome";
import {EffectiveDeployableConfiguration} from "../effective-deployable-configuration";

@Component({
  selector: 'show-deployable-configuration-dialog',
  templateUrl: './show-deployable-configuration-dialog.component.html',
  styleUrls: ['./show-deployable-configuration-dialog.component.scss']
})
export class ShowDeployableConfigurationDialog {

  public deployableConfiguration: EffectiveDeployableConfiguration;

  constructor(public dialogRef: MatDialogRef<ShowDeployableConfigurationDialog>, @Inject(MAT_DIALOG_DATA) data: EffectiveDeployableConfiguration) {
    this.deployableConfiguration = data;
  }

  public close() {
    this.dialogRef.close();
  }

}
