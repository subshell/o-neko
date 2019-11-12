import {Component, Inject, OnInit} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import "brace/mode/yaml";
//manually import theme and mode for ace - else it will try to autoload it (and fail on doing so)...
import "brace/theme/chrome";
import {KeyValueChangeEvent} from "../../form/key-value-input/key-value-input.component";
import {EffectiveDeployableConfiguration} from "../effective-deployable-configuration";

@Component({
  selector: 'show-deployable-configuration-dialog',
  templateUrl: './show-deployable-configuration-dialog.component.html',
  styleUrls: ['./show-deployable-configuration-dialog.component.scss']
})
export class ShowDeployableConfigurationDialog implements OnInit {

  public deployableConfiguration: EffectiveDeployableConfiguration;
  public availableTemplateVariablesMap: Map<string, string> = new Map();
  constructor(public dialogRef: MatDialogRef<ShowDeployableConfigurationDialog>, @Inject(MAT_DIALOG_DATA) data: EffectiveDeployableConfiguration) {
    this.deployableConfiguration = data;
  }

  public close() {
    this.dialogRef.close();
  }

  ngOnInit(): void {
    this.availableTemplateVariablesMap = new Map(Object.entries(this.deployableConfiguration.availableTemplateVariables));
  }
}
