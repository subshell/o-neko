import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MatDialog} from "@angular/material";
import {ConfirmDialog, ConfirmDialogData} from "../../util/confirm-dialog/confirm-dialog.component";
import {TemplateVariable} from "../project";

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

  constructor(private dialog: MatDialog) {
  }

  public addTemplateVariable() {
    const templateNumber = this.customTemplateVariables.length + 1;
    const templateVariable: TemplateVariable = {
      id: null,
      name: `New_${templateNumber}`,
      label: `New ${templateNumber}`,
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
        title: `Delete Template Variable?`,
        okButtonText: 'Delete'
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
