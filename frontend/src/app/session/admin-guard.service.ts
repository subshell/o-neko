import {Injectable} from "@angular/core";
import { ActivatedRouteSnapshot, RouterStateSnapshot } from "@angular/router";
import {Observable} from "rxjs";
import {map} from "rxjs/operators";
import {UserRole} from "../user/user-role";
import {UserService} from "../user/user.service";


@Injectable()
export class AdminGuardService  {

  constructor(private userService: UserService) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    return this.userService.currentUser().pipe(map(user => user.hasRolePermission(UserRole.ADMIN)));
  }

}
