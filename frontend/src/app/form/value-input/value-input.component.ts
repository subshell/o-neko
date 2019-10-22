import {Component, EventEmitter, Input, Output} from '@angular/core';
import {ValueInfo, ValueInfoChangeEvent, ValueInfoMap} from "./value-info";

@Component({
  selector: 'value-input',
  templateUrl: './value-input.component.html',
  styleUrls: ['./value-input.component.scss']
})
export class ValueInputComponent {

  @Output()
  public onChange: EventEmitter<ValueInfoChangeEvent> = new EventEmitter<ValueInfoChangeEvent>();
  @Input()
  public readonly: boolean = false;
  public ids: string[] = [];

  private _valuesByKey: ValueInfoMap;

  public get valuesByKey(): { [key: string]: ValueInfo } {
    return this._valuesByKey;
  }

  @Input()
  public set valuesByKey(valuesByKey: ValueInfoMap) {
    if (!valuesByKey) {
      return;
    }
    for (let key in valuesByKey) {
      if (!valuesByKey.hasOwnProperty(key)) continue;

      const valueByKey = valuesByKey[key];
      if (valueByKey.singleValue && (!valueByKey.values || valueByKey.values.length === 0)) {
        valueByKey.values = [""];
      }
    }

    this._valuesByKey = valuesByKey;
    this.ids = Object.keys(this._valuesByKey);
  }

  public emitChange(id: string): void {
    this.onChange.emit({
      id,
      changedValue: this.valuesByKey[id],
      values: this.valuesByKey
    });
  }
}
