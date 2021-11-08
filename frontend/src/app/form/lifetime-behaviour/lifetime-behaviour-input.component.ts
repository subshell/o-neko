import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {LifetimeBehaviour, LifetimeType} from "../../project/project";
import {TranslateService} from "@ngx-translate/core";

export interface LabeledLifetimeBehaviour {
  label: string,
  lifetime: LifetimeBehaviour
};

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
  @Output() modelChange: EventEmitter<LifetimeBehaviour> = new EventEmitter();

  defaultLifetimeBehaviourOptions: Array<LabeledLifetimeBehaviour> = [];
  lifetimeBehaviourOptions?: Array<LabeledLifetimeBehaviour>;

  constructor(private translateService: TranslateService) {
    this.name = this.translateService.instant('components.forms.lifetimeBehaviourInput.lifetimeBehaviour');
    this.defaultLifetimeBehaviourOptions = [
      // special cases
      {
        label: translateService.instant('components.forms.lifetimeBehaviourInput.untilTonight'),
        lifetime: {
          type: 'UNTIL_TONIGHT'
        }
      },
      {
        label: translateService.instant('components.forms.lifetimeBehaviourInput.untilWeekend'),
        lifetime: {
          type: 'UNTIL_WEEKEND'
        }
      },

      // number of days
      {
        label: translateService.instant('components.forms.lifetimeBehaviourInput.days', {count: 1}),
        lifetime: {
          type: 'DAYS',
          value: 1
        }
      },
      {
        label: translateService.instant('components.forms.lifetimeBehaviourInput.weeks', {count: 1}),
        lifetime: {
          type: 'DAYS',
          value: 7
        }
      },
      {
        label: translateService.instant('components.forms.lifetimeBehaviourInput.weeks', {count: 2}),
        lifetime: {
          type: 'DAYS',
          value: 14
        }
      },
      {
        label: translateService.instant('components.forms.lifetimeBehaviourInput.days', {count: 30}),
        lifetime: {
          type: 'DAYS',
          value: 30
        }
      },

      // infinite
      {
        label: translateService.instant('components.forms.lifetimeBehaviourInput.infinite'),
        lifetime: {
          type: 'INFINITE'
        }
      },
    ];
    this.lifetimeBehaviourOptions = this.defaultLifetimeBehaviourOptions;
  }

  ngOnInit() {
    this.lifetimeBehaviourOptions = this.additionalLifetimeBehaviourOptions.concat(this.defaultLifetimeBehaviourOptions);
  }

  public onModelChange(model: LifetimeBehaviour) {
    this.modelChange.emit(model);
  }

  public compareLifetimeBehaviour(option: LifetimeBehaviour, value: LifetimeBehaviour): boolean {
    return option?.type === value?.type && option?.value === value?.value;
  }
}
