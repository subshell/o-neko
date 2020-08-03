import {Component} from "@angular/core";
import {SetThemeMode, ThemingMode, ThemingState} from "../../store/theming/theming.state";
import {Select, Store} from "@ngxs/store";
import {Observable} from "rxjs";
import {map} from "rxjs/operators";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'on-theme-switcher',
  templateUrl: './theme-switcher.component.html',
  styleUrls: ['./theme-switcher.component.scss']
})
export class ThemeSwitcherComponent {

  modes: Array<{ label: Observable<string>, mode: ThemingMode, icon: string }>;

  @Select(ThemingState.themingMode) themingMode$: Observable<ThemingMode>;
  currentModeIcon = this.themingMode$.pipe(map(mode => this.modes.find(m => m.mode === mode)?.icon ?? 'settings-brightness'))

  constructor(private store: Store,
              translate: TranslateService) {
    this.modes = [
      {
        label: translate.get('components.themeSwitcher.auto'),
        mode: 'auto',
        icon: 'settings-brightness'
      }, {
        label: translate.get('components.themeSwitcher.light'),
        mode: 'light',
        icon: 'wb-sunny'
      }, {
        label: translate.get('components.themeSwitcher.dark'),
        mode: 'dark',
        icon: 'nights-stay'
      }
    ];
  }

  setMode(mode: ThemingMode) {
    this.store.dispatch(new SetThemeMode(mode));
  }
}
