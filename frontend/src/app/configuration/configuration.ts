import {MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {MDI_SVG_ICONS} from './generated/mdi';

export const configureSvgIcons = (iconRegistry: MatIconRegistry, domSanitizer: DomSanitizer) => {
  // If the following produces errors in your IDE you need to install the npm dependencies
  // or run the postinstall script (O-NEKO-ROOT/frontend/extract-svg-icons.js) manually via node
  MDI_SVG_ICONS.icons.forEach(svgIcon => {
    iconRegistry.addSvgIcon(svgIcon, domSanitizer.bypassSecurityTrustResourceUrl(`${MDI_SVG_ICONS.basePath}/${svgIcon}.svg`));
  });
};
