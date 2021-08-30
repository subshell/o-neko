import {Component, EventEmitter, Input, Output} from '@angular/core';
import { MatDialog } from "@angular/material/dialog";
import {ConfirmDialog, ConfirmDialogData} from "../../util/confirm-dialog/confirm-dialog.component";
import {TemplateVariable} from "../project";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'template-variables',
  templateUrl: './template-variables.component.html',
  styleUrls: ['./template-variables.component.scss']
})
export class TemplateVariablesComponent {

  @Input()
  public customTemplateVariables: TemplateVariable[] = [];

  @Input()
  public readonly: boolean;

  @Output()
  public onAddTemplateVariable: EventEmitter<TemplateVariable> = new EventEmitter<TemplateVariable>();

  @Output()
  public onDeleteTemplateVariable: EventEmitter<TemplateVariable> = new EventEmitter<TemplateVariable>();

  public selectedTemplateVariable: TemplateVariable = null;

  constructor(private dialog: MatDialog,
              private readonly translate: TranslateService) {
  }

  public addTemplateVariable() {
    const templateNumber = this.customTemplateVariables.length + 1;
    const templateVariable: TemplateVariable = {
      id: null,
      name: this.translate.instant('components.project.templateVariables.newTemplateVariableName', {index: templateNumber}),
      label: this.translate.instant('components.project.templateVariables.newTemplateVariableLabel', {index: templateNumber}),
      useValues: true,
      showOnDashboard: false,
      values: [
        "value-1",
        "value-2"
      ],
      defaultValue: null
    };
    this.onAddTemplateVariable.emit(templateVariable);
    this.selectedTemplateVariable = templateVariable;
  }

  deleteTemplateVariable(templateVariable: TemplateVariable) {
    this.dialog.open(ConfirmDialog, {
      data: <ConfirmDialogData>{
        title: this.translate.instant('components.project.templateVariables.deleteDialog.title'),
        okButtonText: this.translate.instant('components.project.templateVariables.deleteDialog.confirm')
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        if (this.selectedTemplateVariable === templateVariable) {
          this.selectedTemplateVariable = null;
        }
        this.onDeleteTemplateVariable.emit(templateVariable);
      }
    });
  }
}
