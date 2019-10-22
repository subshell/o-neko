import {Component, Input, ViewEncapsulation} from '@angular/core';
import {TemplateVariable} from "../../project";

@Component({
  selector: 'template-variable-editor',
  templateUrl: './template-variable-editor.component.html',
  styleUrls: ['./template-variable-editor.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class TemplateVariableEditorComponent {
  public defaultValueIndex: number;
  @Input()
  public readonly: boolean;

  constructor() {
  }

  private _templateVariable: TemplateVariable;

  public get templateVariable(): TemplateVariable {
    return this._templateVariable;
  }

  @Input()
  public set templateVariable(templateVariable: TemplateVariable) {
    this._templateVariable = templateVariable;
    if (this._templateVariable) {
      this.markDefaultVariable();
    }
  }

  public addNewValue(newValue: string = "") {
    this.templateVariable.values.push(newValue);
  }

  markAsDefault(value: string) {
    this.templateVariable.defaultValue = value;
  }

  deleteValue(index: number) {
    this.templateVariable.values.splice(index, 1);
  }

  private markDefaultVariable() {
    this.defaultValueIndex = this.templateVariable.values.indexOf(this.templateVariable.defaultValue) || 0;
    if (this.defaultValueIndex === -1) {
      this.defaultValueIndex = 0;
    }
    this.markAsDefault(this.templateVariable.values[this.defaultValueIndex]);
  }
}
