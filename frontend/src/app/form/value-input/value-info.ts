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
