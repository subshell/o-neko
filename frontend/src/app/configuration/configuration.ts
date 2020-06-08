import {MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {MDI_SVG_ICONS} from './generated/mdi';
import {TWO_TONE_ICONS} from './generated/two-tone';
import {AnimationDriver, ɵNoopAnimationDriver as NoopAnimationDriver, ɵWebAnimationsDriver as WebAnimationsDriver} from '@angular/animations/browser';

export const configureSvgIcons = (iconRegistry: MatIconRegistry, domSanitizer: DomSanitizer) => {
  // If the following produces errors in your IDE you need to install the npm dependencies
  // or run the postinstall script (O-NEKO-ROOT/frontend/extract-svg-icons.js) manually via node
  MDI_SVG_ICONS.icons.forEach(svgIcon => {
    iconRegistry.addSvgIconInNamespace('mdi', svgIcon, domSanitizer.bypassSecurityTrustResourceUrl(`${MDI_SVG_ICONS.basePath}/${svgIcon}.svg`));
  });
  TWO_TONE_ICONS.icons.forEach(svgIcon => {
    iconRegistry.addSvgIcon(svgIcon, domSanitizer.bypassSecurityTrustResourceUrl(`${TWO_TONE_ICONS.basePath}/${svgIcon}.svg`));
  });
};

export const provideAnimationDriverBasedOnUserPreferences = (): AnimationDriver => {
  const noop = new NoopAnimationDriver();
  const driver = new WebAnimationsDriver();
  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  return prefersReducedMotion ? noop : driver;
};
