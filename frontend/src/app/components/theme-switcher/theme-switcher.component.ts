import {Component} from "@angular/core";
import {SetThemeMode, ThemingMode, ThemingState} from "../../store/theming/theming.state";
import {Select, Store} from "@ngxs/store";
import {Observable} from "rxjs";
import {map} from "rxjs/operators";

@Component({
  selector: 'on-theme-switcher',
  templateUrl: './theme-switcher.component.html',
  styleUrls: ['./theme-switcher.component.scss']
})
export class ThemeSwitcherComponent {

  modes: Array<{ label: string, mode: ThemingMode, icon: string }> = [
    {
      label: 'Auto',
      mode: 'auto',
      icon: 'settings-brightness'
    }, {
      label: 'Light',
      mode: 'light',
      icon: 'wb-sunny'
    }, {
      label: 'Dark',
      mode: 'dark',
      icon: 'nights-stay'
    }
  ];

  @Select(ThemingState.themingMode) themingMode$: Observable<ThemingMode>;
  currentModeIcon = this.themingMode$.pipe(map(mode => this.modes.find(m => m.mode === mode)?.icon ?? 'settings-brightness'))

  constructor(private store: Store) {
  }

  setMode(mode: ThemingMode) {
    this.store.dispatch(new SetThemeMode(mode));
  }
}
