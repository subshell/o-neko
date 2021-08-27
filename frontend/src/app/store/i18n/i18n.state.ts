import {Action, NgxsOnInit, Selector, State, StateContext, Store} from "@ngxs/store";
import {Injectable} from "@angular/core";
import {TranslateService} from "@ngx-translate/core";

export type OnekoLocale = 'de' | 'en';

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

  constructor(private translate: TranslateService) {
    switch(translate.getBrowserLang()) {
      case 'de': I18nState.defaultLocale = 'de'; break;
      default: I18nState.defaultLocale = 'en';
    }
  }

  @Selector()
  static locale(state: I18nStateModel): OnekoLocale {
    return state.locale || I18nState.defaultLocale;
  }

  @Action(SetLocale)
  setLocale(ctx: StateContext<I18nStateModel>, action: SetLocale) {
    ctx.setState({locale: action.locale})
  }

}
