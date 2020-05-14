/**
 * This script:
 *
 * 1. Copies the SVG files from the node module into our assets directory (in the case of @mdi/svg)
 * 2. Generates a .ts file with a definition of the icon set. This definition contains a name of all available icons in
 *    the icon set.
 *
 * The generated .ts file can be used to add every icon individually to Angular Material. This is more work now, but
 * the result is that a user visiting our app will only download the icons we actually used in the frontend.
 * This happens in the ./src/app/configuration/configuration.ts file.
 *
 * Without this script we would have to import a svg sprite sheet which contains all icons in a given icon set, and
 * a user would download the whole icon set, even though we only use <=1% of the available icons there.
 */

const fs = require('fs');

function writeSvgDefinitionFile(filePath, constName, basePath, iconNames) {
  let file =
    `// THIS FILE IS GENERATED AUTOMATICALLY BY extract-svg-icons.js
// DO NOT EDIT IT MANUALLY - YOU WILL LOSE YOUR CHANGES
import {IconSet} from './icon';

export const ${constName}: IconSet = {
  basePath: '${basePath}',
  icons: [\n`;

  for (let i = 0; i < iconNames.length; i++) {
    file += `    '${iconNames[i]}'`;
    if (i < iconNames.length - 1) {
      file += `,\n`
    }
  }

  file += `\n  ]\n};\n`;

  fs.writeFileSync(filePath, new Uint8Array(Buffer.from(file)));
}

function getSvgIconNamesFromDirectory(path) {
  return fs.readdirSync(path)
    .filter(filename => filename.endsWith('.svg'))
    .map(filename => filename.slice(0, -4));
}

function extractMdiIconSet() {
  const iconNames = getSvgIconNamesFromDirectory('./node_modules/@mdi/svg/svg');
  fs.mkdirSync(`./src/assets/icons/mdi/`, {recursive: true});
  iconNames.forEach(iconName => fs.copyFileSync(`./node_modules/@mdi/svg/svg/${iconName}.svg`, `./src/assets/icons/mdi/${iconName}.svg`));
  writeSvgDefinitionFile('./src/app/configuration/mdi.ts', 'MDI_SVG_ICONS', 'assets/icons/mdi', iconNames);
}

extractMdiIconSet();
