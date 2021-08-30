import {flatten} from "../util/functions";
import {AliasingTranslateCompiler} from "../util/aliasing-translate-compiler";

interface Translation {
  original: object;
  flattened: object;
}

describe('Translations', () => {

  const supportedLanguageKeys = ['de', 'en'];
  const translations: Map<string, Translation> = new Map<string, Translation>();
  const DUPLICATE_IGNORE_KEYS = [
  ];

  beforeAll(() => {
    supportedLanguageKeys.forEach(key => {
      const file = require(`../../assets/i18n/${key}.json`);
      translations.set(key, {
        original: file,
        flattened: flatten(file)
      });
    });
  });

  it('should have identical translation keys', () => {
    const referenceTranslationKey = supportedLanguageKeys[0];

    // could be any, because if this matches with all, all others match with the rest, too:
    const referenceTranslation = translations.get(referenceTranslationKey);
    const referenceKeysFlattened = Object.keys(referenceTranslation.flattened);
    translations.forEach((translation, key) => {
      const flattenedKeys = Object.keys(translation.flattened);
      const inReferenceButNotInOtherLanguage = referenceKeysFlattened.filter(k => !flattenedKeys.includes(k));
      const inOtherLanguateButNotInReference = flattenedKeys.filter(k => !referenceKeysFlattened.includes(k));

      expect(inReferenceButNotInOtherLanguage.length).toBe(0, `The key(s) [${inReferenceButNotInOtherLanguage}] are missing in language ${key}`);
      expect(inOtherLanguateButNotInReference.length).toBe(0, `The key(s) [${inOtherLanguateButNotInReference}] are missing in language ${referenceTranslationKey}`);
    });
  });

  it('should not have empty translation texts', () => {
    translations.forEach((translation, languageKey) => {
      Object.keys(translation.flattened).forEach(translationKey => {
        expect(translation.flattened[translationKey])
          .not
          .toEqual('', `Translation key ${translationKey} in ${languageKey}.json should not be empty.`);
      });
    });
  });

});
