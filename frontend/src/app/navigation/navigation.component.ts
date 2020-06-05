import {Component, Input} from "@angular/core";
import {Router} from "@angular/router";
import {RestService} from "../rest/rest.service";
import {User} from "../user/user";
import {UserRole} from "../user/user-role";
import {UserService} from "../user/user.service";
import {MatSidenav} from "@angular/material/sidenav";
import {Observable} from "rxjs";

@Component({
  selector: 'navigation',
  templateUrl: './navigation.html',
  styleUrls: ['./navigation.component.scss']
})
export class NavigationComponent {
  public user: Observable<User>;
  public UserRole = UserRole;

  @Input() sidenav: MatSidenav;
  @Input() isDesktop: boolean;

  constructor(private rest: RestService, private router: Router, private userService: UserService) {
    this.user = this.userService.currentUser();
  }

  public logout() {
    this.rest.logout().subscribe(() => {
      this.router.navigate(['login']);
    });
  }
}
