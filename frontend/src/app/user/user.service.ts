import {Injectable} from "@angular/core";
import {EMPTY, Observable} from "rxjs";
import {mergeMap, shareReplay} from "rxjs/operators";
import {RestService} from "../rest/rest.service";
import {AuthService} from "../session/auth.service";
import {User} from "./user";

@Injectable()
export class UserService {

  private user: Observable<User>;
  constructor(private auth: AuthService, private rest: RestService) {
    auth.isAuthenticated().subscribe(authenticated => {
      if (authenticated) {
        this.user = this.rest.currentUser().pipe(shareReplay());
      } else {
        this.user = EMPTY;
      }
    });
  }

  public currentUser(): Observable<User> {
    return this.user;
  }
}
