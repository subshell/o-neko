import {Directive, Input, OnChanges, OnInit} from "@angular/core";
import {UntypedFormControl, NG_VALIDATORS, ValidationErrors, Validator, ValidatorFn} from "@angular/forms";

function validateForbiddenValuesFactory(values: Array<string>): ValidatorFn {
  return (control: UntypedFormControl) => {
    if (control.value && control.value.length > 0) {
      if (values.includes(control.value)) {
        return {forbiddenValues: {valid: false}};
      }
    }
    return null;
  }
}

@Directive({
  selector: '[forbiddenValues]',
  providers: [
    {provide: NG_VALIDATORS, useExisting: ForbiddenValuesValidator, multi: true}
  ]
})
export class ForbiddenValuesValidator implements OnInit, OnChanges, Validator {

  @Input('forbiddenValues')
  forbiddenValues: Array<string>;

  validator: Function;

  ngOnInit() {
    this.validator = validateForbiddenValuesFactory(this.forbiddenValues);
  }

  ngOnChanges() {
    this.validator = validateForbiddenValuesFactory(this.forbiddenValues);
  }

  validate(c: UntypedFormControl): ValidationErrors {
    if (this.validator) {
      return this.validator(c);
    }
  }

}
