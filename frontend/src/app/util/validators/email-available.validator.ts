import {Directive, Input} from "@angular/core";
import {AbstractControl, AsyncValidator, AsyncValidatorFn, NG_ASYNC_VALIDATORS, ValidationErrors} from "@angular/forms";
import {Observable, of} from "rxjs";
import {map} from "rxjs/operators";
import {RestService} from "../../rest/rest.service";

export function emailAvailable(restService: RestService, ignoreEmail: string): AsyncValidatorFn {
  return (control: AbstractControl): Promise<ValidationErrors | null> | Observable<ValidationErrors | null> => {
    if (control.value === ignoreEmail) {
      return of(null);
    } else {
      return restService.isEmailAvailable(control.value).pipe(map(available => {
          return available ? null : {"emailAvailable": {}};
        }
      ));
    }
  };
}

@Directive({
  selector: '[emailAvailable]',
  providers: [{provide: NG_ASYNC_VALIDATORS, useExisting: EmailAvailableValidator, multi: true}]
})
export class EmailAvailableValidator implements AsyncValidator {
  @Input('emailAvailable') emailAvailable: string;

  constructor(private restService: RestService) {
  }

  validate(control: AbstractControl): Promise<ValidationErrors | null> | Observable<ValidationErrors | null> {
    return emailAvailable(this.restService, this.emailAvailable)(control);
  }
}
