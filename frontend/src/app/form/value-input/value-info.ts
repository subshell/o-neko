import {TemplateVariable} from "../../project/project";

export interface ValueInfoChangeEvent {
  id: string
  changedValue: ValueInfo
  values: ValueInfoMap
}

export type ValueInfoMap = { [key: string]: ValueInfo };

export interface ValueInfo {
  defaultValue: string
  selectedValue: string
  singleValue: boolean
  values: string[]
  label?: string
  name: string
}

export function createValueInfoFromTemplateVariable(templateVariable: TemplateVariable, selectedValue: string): ValueInfo {
  return {
    selectedValue: selectedValue,
    defaultValue: templateVariable.defaultValue,
    values: templateVariable.values,
    singleValue: !templateVariable.useValues,
    label: templateVariable.label,
    name: templateVariable.name
  };
}
