import {Directive, Input} from "@angular/core";
import {AbstractControl, AsyncValidator, AsyncValidatorFn, NG_ASYNC_VALIDATORS, ValidationErrors} from "@angular/forms";
import {Observable, of} from "rxjs";
import {map} from "rxjs/operators";
import {RestService} from "../../rest/rest.service";

export function usernameAvailable(restService: RestService, ignoreUsername: string): AsyncValidatorFn {
  return (control: AbstractControl): Promise<ValidationErrors | null> | Observable<ValidationErrors | null> => {
    if (control.value === ignoreUsername) {
      return of(null);
    } else {
      return restService.isUsernameAvailable(control.value).pipe(map(available => {
          return available ? null : {"usernameAvailable": {}};
        }
      ));
    }
  };
}

@Directive({
  selector: '[usernameAvailable]',
  providers: [{provide: NG_ASYNC_VALIDATORS, useExisting: UsernameAvailableValidator, multi: true}]
})
export class UsernameAvailableValidator implements AsyncValidator {
  @Input('usernameAvailable') usernameAvailable: string;

  constructor(private restService: RestService) {
  }

  validate(control: AbstractControl): Promise<ValidationErrors | null> | Observable<ValidationErrors | null> {
    return usernameAvailable(this.restService, this.usernameAvailable)(control);
  }

}
