import {Injectable} from "@angular/core";
import {EMPTY, Observable} from "rxjs";
import {mergeMap} from "rxjs/operators";
import {RestService} from "../rest/rest.service";
import {AuthService} from "../session/auth.service";
import {User} from "./user";

@Injectable()
export class UserService {

  constructor(private auth: AuthService, private rest: RestService) {
  }

  public currentUser(): Observable<User> {
    return this.auth.isAuthenticated()
      .pipe(mergeMap(isAuthenticated => isAuthenticated ? this.rest.currentUser() : EMPTY));
  }
}
