import { Injectable } from '@angular/core';
import {ValueInfo} from "../form/value-input/value-info";
import {TemplateVariable} from "./project";

@Injectable()
export class TemplateVariablesService {

  constructor() { }

  public createValueInfo(templateVariable: TemplateVariable, selectedValue: string): ValueInfo {
    return {
      selectedValue: selectedValue,
      defaultValue: templateVariable.defaultValue,
      values: templateVariable.values,
      singleValue: !templateVariable.useValues,
      label: templateVariable.label,
      name: templateVariable.name
    };
  }
}
