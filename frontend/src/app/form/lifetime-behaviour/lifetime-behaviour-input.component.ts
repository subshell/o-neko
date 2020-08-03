import {Component, Input, OnInit} from "@angular/core";
import {LifetimeBehaviour} from "../../project/project";
import {TranslateService} from "@ngx-translate/core";

export type LabeledLifetimeBehaviour = { label: string, value: number };

@Component({
  selector: 'lifetime-behaviour-input',
  templateUrl: './lifetime-behaviour-input.component.html',
  styleUrls: ['./lifetime-behaviour-input.component.scss']
})
export class LifetimeBehaviourInputComponent implements OnInit {

  @Input() model: LifetimeBehaviour;
  @Input() additionalLifetimeBehaviourOptions?: Array<LabeledLifetimeBehaviour> = [];
  @Input() name?: string = "Lifetime behaviour";
  @Input() required?: boolean = false;
  @Input() readonly?: boolean = false;

  defaultLifetimeBehaviourOptions: Array<LabeledLifetimeBehaviour> = [];
  lifetimeBehaviourOptions?: Array<LabeledLifetimeBehaviour>;

  constructor(private translateService: TranslateService) {
    this.name = this.translateService.instant('components.forms.lifetimeBehaviourInput.lifetimeBehaviour');
    this.defaultLifetimeBehaviourOptions = [
      {
        label: translateService.instant('components.forms.lifetimeBehaviourInput.days', {count: 1}),
        value: 1
      },
      {
        label: translateService.instant('components.forms.lifetimeBehaviourInput.weeks', {count: 1}),
        value: 7
      },
      {
        label: translateService.instant('components.forms.lifetimeBehaviourInput.weeks', {count: 2}),
        value: 14
      },
      {
        label: translateService.instant('components.forms.lifetimeBehaviourInput.days', {count: 30}),
        value: 30
      },
      {
        label: translateService.instant('components.forms.lifetimeBehaviourInput.infinite'),
        value: 0
      },
    ];
    this.lifetimeBehaviourOptions = this.defaultLifetimeBehaviourOptions;
  }

  ngOnInit() {
    this.lifetimeBehaviourOptions = this.additionalLifetimeBehaviourOptions.concat(this.defaultLifetimeBehaviourOptions);
  }

}
