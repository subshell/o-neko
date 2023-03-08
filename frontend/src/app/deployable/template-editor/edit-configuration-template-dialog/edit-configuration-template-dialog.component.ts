import {Component, Inject} from '@angular/core';
import {MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef} from '@angular/material/legacy-dialog';
import {ConfigurationTemplateEditorModel} from '../template-editor.component';

export interface EditConfigurationTemplateDialogComponentData {
  model: ConfigurationTemplateEditorModel;
  models: Array<ConfigurationTemplateEditorModel>;
}

@Component({
  selector: 'oneko-edit-configuration-template-dialog',
  templateUrl: './edit-configuration-template-dialog.component.html',
  styleUrls: ['./edit-configuration-template-dialog.component.scss']
})
export class EditConfigurationTemplateDialogComponent {
  public valid: boolean = true;

  public filename: string;
  public description: string;

  public model: ConfigurationTemplateEditorModel;
  private readonly models: Array<ConfigurationTemplateEditorModel>;


  constructor(public readonly dialogRef: MatDialogRef<EditConfigurationTemplateDialogComponent>,
              @Inject(MAT_DIALOG_DATA) readonly data: EditConfigurationTemplateDialogComponentData) {
    this.model = data.model;
    this.filename = data.model.name;
    this.description = data.model.description;
    this.models = data.models;
  }

  public close() {
    this.dialogRef.close({
      valid: this.valid,
      filename: this.filename,
      description: this.description
    });
  }

  public checkValidNames(): void {
    const names = this.models.map(ct => ct.name);
    let counts = [];
    let namesValid = true;
    for (let name of names) {
      if (name && counts[name] === undefined) {
        counts[name] = 1;
      } else {
        namesValid = false;
      }
    }
    this.valid = namesValid;
  }

  public getIllegalTemplateNamesFor(template: ConfigurationTemplateEditorModel): Array<string> {
    return this.models
      .filter(t => t !== template)
      .map(t => t.name);
  }
}
