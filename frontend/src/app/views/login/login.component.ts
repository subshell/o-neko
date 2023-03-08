import {Component, ViewEncapsulation} from "@angular/core";
import {MatLegacySnackBar as MatSnackBar} from "@angular/material/legacy-snack-bar";
import {Router} from "@angular/router";
import {RestService} from "../../rest/rest.service";
import {TimeoutSnackbarComponent} from "../../util/timout-snackbar/timeout.snackbar.component";

@Component({
  selector: 'login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class LoginComponent {

  username = '';
  password = '';
  loggingIn = false;

  constructor(private rest: RestService, private router: Router, private snackBar: MatSnackBar) {
  }

  public login() {
    this.loggingIn = true;
    this.rest.login(this.username, this.password).subscribe(success => {
      this.username = '';
      this.password = '';
      this.router.navigate(['']);
      this.loggingIn = false;
    }, error => {
      this.loggingIn = false;
      this.snackBar.openFromComponent(TimeoutSnackbarComponent, {
        data: {
          text: 'Login failed'
        },
        duration: 5000
      });
    });
  }

}
