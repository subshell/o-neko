/**
 * This script:
 *
 * 1. Copies the SVG files from the node module into our assets directory (in the case of @mdi/svg)
 *
 */

const fs = require('fs');

function getSvgIconNamesFromDirectory(path) {
  return fs.readdirSync(path)
    .filter(filename => filename.endsWith('.svg'))
    .map(filename => filename.slice(0, -4));
}

function extractMdiIconSet() {
  const iconNames = getSvgIconNamesFromDirectory('./node_modules/@mdi/svg/svg');
  fs.mkdirSync(`./src/assets/icons/mdi/`, {recursive: true});
  iconNames.forEach(iconName => fs.copyFileSync(`./node_modules/@mdi/svg/svg/${iconName}.svg`, `./src/assets/icons/mdi/${iconName}.svg`));
}

extractMdiIconSet();
