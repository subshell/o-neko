import {Component} from "@angular/core";
import {Router} from "@angular/router";
import {RestService} from "../rest/rest.service";
import {User} from "../user/user";
import {UserRole} from "../user/user-role";
import {UserService} from "../user/user.service";

@Component({
  selector: 'navigation',
  templateUrl: './navigation.html',
  styleUrls: ['./navigation.component.scss']
})
export class NavigationComponent {
  public user: User;
  public UserRole = UserRole;

  constructor(private rest: RestService, private router: Router, private userService: UserService) {
    this.userService.currentUser().subscribe(user => this.user = user);
  }

  public logout() {
    this.rest.logout().subscribe(() => {
      this.router.navigate(['login']);
    });
  }
}
