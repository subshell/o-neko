import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";

export interface KeyValueChangeEvent {
  key: string
  value: string
  deletion?: boolean
}

@Component({
  selector: 'key-value-input',
  templateUrl: './key-value-input.component.html',
  styleUrls: ['./key-value-input.component.scss']
})
export class KeyValueInputComponent implements OnInit {

  @Input() keyValues: Map<string, string>;
  @Input() readonly?: boolean = false;

  @Output() onChange: EventEmitter<KeyValueChangeEvent> = new EventEmitter<KeyValueChangeEvent>();

  public orderedKeys: string[] = [];

  ngOnInit(): void {
    this.orderedKeys = this.keyValues ? Array.from(this.keyValues.keys()) : [];
  }

  public isAddingNewVariableAllowed(): boolean {
    return !this.readonly;
  }

  public addVariable(key: string = `new_variable_${(Math.random() * 1000).toFixed(0)}`): void {
    if (!this.isAddingNewVariableAllowed()) {
      return;
    }

    this.keyValues.set(key, "");
    this.orderedKeys.push(key);

    this.emitEvent(key, this.keyValues.get(key));
  }

  public removeVariable(key: string) {
    this.keyValues.delete(key);
    this.orderedKeys.splice(this.orderedKeys.indexOf(key), 1);

    this.emitEvent(key, undefined, true);
  }

  public changeKey($event: any, oldKey: string): void {
    const newKey = $event.target.value;
    if (this.keyValues.has(newKey)) {
      return;
    }

    this.keyValues.set(newKey, this.keyValues.get(oldKey));
    this.orderedKeys[this.orderedKeys.findIndex(key => key === oldKey)] = newKey;
    this.keyValues.delete(oldKey);

    this.emitEvent(oldKey, undefined, true);
    this.emitEvent(newKey, this.keyValues.get(newKey));
  }

  public emitEvent(key: string, value: string, deletion: boolean = false): void {
    this.keyValues.set(key, value);
    this.onChange.emit({key, value, deletion});
  }
}
