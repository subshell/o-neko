import {Component, ViewEncapsulation} from "@angular/core";
import {MatLegacySnackBar as MatSnackBar} from "@angular/material/legacy-snack-bar";
import {Router} from "@angular/router";
import {Select, Store} from "@ngxs/store";
import {Observable} from "rxjs";
import {RestService} from "../../rest/rest.service";
import {I18nState, OnekoLocale, SetLocale} from "../../store/i18n/i18n.state";
import {SetThemeMode, ThemingState} from "../../store/theming/theming.state";
import {TimeoutSnackbarComponent} from "../../util/timout-snackbar/timeout.snackbar.component";

@Component({
  selector: 'login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class LoginComponent {

  // Must be at least the duration of the .exiting animation in login.component.scss (.45s).
  private static readonly EXIT_NAVIGATION_DELAY_MS = 500;

  username = '';
  password = '';
  loggingIn = false;
  exiting = false;

  @Select(I18nState.locale) locale$: Observable<OnekoLocale>;
  @Select(ThemingState.isDarkMode) isDarkMode$: Observable<boolean>;

  constructor(private rest: RestService,
              private router: Router,
              private snackBar: MatSnackBar,
              private store: Store) {
  }

  public login() {
    if (this.loggingIn) {
      return;
    }
    this.loggingIn = true;
    this.rest.login(this.username, this.password).subscribe(success => {
      this.exiting = true;
      setTimeout(() => {
        this.username = '';
        this.password = '';
        this.router.navigate(['']);
        this.loggingIn = false;
      }, LoginComponent.EXIT_NAVIGATION_DELAY_MS);
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

  public setLocale(locale: OnekoLocale) {
    this.store.dispatch(new SetLocale(locale));
  }

  public toggleTheme() {
    const isDark = this.store.selectSnapshot(ThemingState.isDarkMode);
    this.store.dispatch(new SetThemeMode(isDark ? 'light' : 'dark'));
  }
}
