import {Component, Input} from "@angular/core";
import {LifetimeBehaviour} from "../../project/project";

export type LabeledLifetimeBehaviour = { label: string, value: number };

@Component({
  selector: 'lifetime-behaviour-input',
  templateUrl: './lifetime-behaviour-input.component.html',
  styleUrls: ['./lifetime-behaviour-input.component.scss']
})
export class LifetimeBehaviourInputComponent {

  @Input() model: LifetimeBehaviour;
  @Input() lifetimeBehaviourOptions?: Array<LabeledLifetimeBehaviour> = LifetimeBehaviourInputComponent.defaultLifetimeBehaviourOptions;
  @Input() name?: string = "Lifetime behaviour";
  @Input() required?: boolean = false;
  @Input() readonly?: boolean = false;

  constructor() {
  }

  public static get defaultLifetimeBehaviourOptions(): Array<LabeledLifetimeBehaviour> {
    return [
      {
        label: '1 Day',
        value: 1
      },
      {
        label: '1 Week',
        value: 7
      },
      {
        label: '2 Weeks',
        value: 14
      },
      {
        label: '30 Days',
        value: 30
      },
      {
        label: 'Infinite',
        value: 0
      },
    ]
  }

}
