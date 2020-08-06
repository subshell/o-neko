import {TranslateMessageFormatCompiler} from 'ngx-translate-messageformat-compiler';
import {flatten} from './functions';

export class AliasingTranslateCompiler extends TranslateMessageFormatCompiler {

  static readonly ALIAS_MARKER = '!alias:';

  compile(value: string, lang: string): (params: any) => string {
    return super.compile(value, lang);
  }

  compileTranslations(translations: any, lang: string): any {
    const flattenedTranslations = flatten(translations);
    const aliases = Object.entries(flattenedTranslations)
      .filter(([key, value]: [string, string]) => value.startsWith(AliasingTranslateCompiler.ALIAS_MARKER))
      .map(([key, value]: [string, string]) => ({
        key,
        alias: value.substring(AliasingTranslateCompiler.ALIAS_MARKER.length)
      }));

    const compileTranslations = super.compileTranslations(translations, lang);
    return this.replaceAliases(compileTranslations, aliases);
  }

  private replaceAliases(translations: any, aliases: Array<{ key: string, alias: string }>): any {
    aliases.forEach(alias => {
      const pathToKey = alias.key.split('.');
      let pointer = translations;
      for (let i = 0; i < pathToKey.length - 1; i++) {
        const segment = pathToKey[i];
        pointer = pointer[segment];
      }
      const keyParent = pointer;

      const pathToAlias = alias.alias.split('.');
      pointer = translations;
      for (let i = 0; i < pathToAlias.length - 1; i++) {
        const segment = pathToAlias[i];
        pointer = pointer[segment];
      }
      const aliasParent = pointer;

      keyParent[pathToKey[pathToKey.length - 1]] = aliasParent[pathToAlias[pathToAlias.length - 1]];
    });
    return translations;
  }
}
