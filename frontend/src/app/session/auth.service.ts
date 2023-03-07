import {Injectable} from "@angular/core";
import {Observable, ReplaySubject} from "rxjs";
import {distinctUntilChanged} from "rxjs/operators";

@Injectable()
export class AuthService {
  private authenticatedSubject = new ReplaySubject<boolean>(1);
  public authenticated: Observable<boolean> = this.authenticatedSubject.asObservable();

  public setAuthenticated(to: boolean) {
    this.authenticatedSubject.next(to);
  }

  public isAuthenticated(): Observable<boolean> {
    return this.authenticatedSubject.asObservable().pipe(distinctUntilChanged());
  }

}
