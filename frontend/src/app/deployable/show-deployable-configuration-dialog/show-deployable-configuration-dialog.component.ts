import {Component, Inject, OnInit} from "@angular/core";
import {MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef} from "@angular/material/legacy-dialog";
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
