import {Component} from "@angular/core";
import {Select, Store} from "@ngxs/store";
import {Observable} from "rxjs";
import {I18nState, OnekoLocale, SetLocale} from "../../store/i18n/i18n.state";

@Component({
  selector: 'on-i18n-switcher',
  templateUrl: './i18n-switcher.component.html',
  styleUrls: ['./i18n-switcher.component.scss']
})
export class I18nSwitcherComponent {

  locales: Array<{ label: string, locale: OnekoLocale, icon: string }>;

  @Select(I18nState.locale) currentLocale$: Observable<OnekoLocale>;

  constructor(private store: Store) {
    this.locales = [
      {
        label: 'English',
        locale: 'en',
        icon: 'settings-brightness'
      }, {
        label: 'Deutsch',
        locale: 'de',
        icon: 'wb-sunny'
      }
    ];
  }

  setLocale(locale: OnekoLocale) {
    this.store.dispatch(new SetLocale(locale));
  }
}
