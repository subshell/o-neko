import {Directive, Input} from "@angular/core";
import {AbstractControl, NG_VALIDATORS, ValidationErrors, Validator, ValidatorFn} from "@angular/forms";

export function mustMatchValidator(mustMatch: string): ValidatorFn {
  return (control: AbstractControl): { [key: string]: any } => {
    const matches = control.value === mustMatch;
    return matches ? null : {'mustMatch': {}};
  };
}

@Directive({
  selector: '[mustMatch]',
  providers: [{provide: NG_VALIDATORS, useExisting: MustMatchValidatorDirective, multi: true}]
})
export class MustMatchValidatorDirective implements Validator {
  @Input('mustMatch') mustMatch: string;

  validate(control: AbstractControl): ValidationErrors | null {
    return this.mustMatch ? mustMatchValidator(this.mustMatch)(control) : null;
  }
}
