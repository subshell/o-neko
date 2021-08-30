import {Action, Selector, State, StateContext, Store} from "@ngxs/store";
import {Injectable} from "@angular/core";
import {TranslateService} from "@ngx-translate/core";

export type OnekoLocale = 'de' | 'en';

// simple hack to allow the date-format-function from date-time-parser.ts to use the locale without
// the need for an angular service etc.
export function currentOnekoLocale(): OnekoLocale {
  return currentLocale;
}
let currentLocale: OnekoLocale;

export interface I18nStateModel {
  locale?: OnekoLocale
}

const defaultState: I18nStateModel = {
  locale: undefined
}

export class SetLocale {
  static readonly type = '[I18N] Set Locale';

  constructor(public locale: OnekoLocale) {
  }

}

@State({
  name: 'i18n',
  defaults: defaultState
})
@Injectable()
export class I18nState {

  private static defaultLocale: OnekoLocale;

  constructor(private translate: TranslateService, private store: Store) {
    switch(translate.getBrowserLang()) {
      case 'de': I18nState.defaultLocale = 'de'; break;
      default: I18nState.defaultLocale = 'en';
    }
    this.initGlobalVariable();
  }

  private initGlobalVariable() {
    this.store.select(I18nState.locale).subscribe(locale => currentLocale = locale);
  }

  @Selector()
  static locale(state: I18nStateModel): OnekoLocale {
    return state.locale || I18nState.defaultLocale;
  }

  @Action(SetLocale)
  setLocale(ctx: StateContext<I18nStateModel>, action: SetLocale) {
    ctx.setState({locale: action.locale})
    currentLocale = action.locale;
  }

}
