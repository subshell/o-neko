import {MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {MDI_SVG_ICONS} from './generated/mdi';
import {TWO_TONE_ICONS} from './generated/two-tone';
import {AnimationDriver, ɵNoopAnimationDriver as NoopAnimationDriver, ɵWebAnimationsDriver as WebAnimationsDriver} from '@angular/animations/browser';
import {TranslateService} from "@ngx-translate/core";
import {MatPaginatorIntl} from "@angular/material/paginator";

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

export const configureTranslations = (translate: TranslateService) => {
  translate.setDefaultLang('en');
  //translate.use(translate.getBrowserLang()); // todo: use once translation is finished
  translate.use('en'); // todo: remove once translation is finished
};

export const configureMatPaginatorI18n = (translate: TranslateService): MatPaginatorIntl => {
  return new TranslatedMatPaginatorIntl(translate);
};

class TranslatedMatPaginatorIntl extends MatPaginatorIntl {

  constructor(private translate: TranslateService) {
    super();
    this.initialize();
  }

  private initialize() {
    const firstPage = 'material.paginator.firstPage';
    const previousPage = 'material.paginator.previousPage';
    const nextPage = 'material.paginator.nextPage';
    const lastPage = 'material.paginator.lastPage';
    const itemsPerPage = 'material.paginator.itemsPerPage';
    this.translate.get([
      firstPage,
      previousPage,
      nextPage,
      lastPage,
      itemsPerPage
    ]).subscribe((translations) => {
      this.firstPageLabel = translations[firstPage];
      this.nextPageLabel = translations[nextPage];
      this.previousPageLabel = translations[previousPage];
      this.lastPageLabel = translations[lastPage];
      this.itemsPerPageLabel = translations[itemsPerPage];
      this.changes.next();
    });
    this.getRangeLabel = (page, pageSize, length) => {
      if (length === 0 || pageSize === 0) {
        return `0 of ${length}`;
      }
      length = Math.max(length, 0);
      const startIndex = page * pageSize;
      const endIndex = startIndex < length ? Math.min(startIndex + pageSize, length) : startIndex + pageSize;
      return this.translate.instant('material.paginator.itemsVisible', {from: startIndex + 1, to: endIndex, length});
    };
  }
}
