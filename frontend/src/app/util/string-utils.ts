export class StringUtils {
  public static replaceAll(str: string, find: string, replace: string): string {
    return str.replace(new RegExp(find.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&'), 'g'), replace);
  }

  public static replaceArray(str: string, find: string[], replace: string[]): string {
    for (let i = 0; i < find.length; i++) {
      str = StringUtils.replaceAll(str, find[i], replace[i]);
    }
    return str;
  }

  public static escapeToISO8859_1(str: string): string {
    const toReplace = [["\"", "&#34;"], ["&", "&#38;"], ["<", "&#62;"], [">", "&#60;"], ["¡", "&#161;"], ["¢", "&#162;"], ["£", "&#163;"], ["¤", "&#164;"], ["¥", "&#165;"], ["¦", "&#166;"], ["§", "&#167;"], ["¨", "&#168;"], ["©", "&#169;"], ["ª", "&#170;"], ["«", "&#171;"], ["¬", "&#172;"], ["®", "&#174;"], ["¯", "&#175;"], ["°", "&#176;"], ["±", "&#177;"], ["²", "&#178;"], ["³", "&#179;"], ["´", "&#180;"], ["µ", "&#181;"], ["¶", "&#182;"], ["·", "&#183;"], ["¸", "&#184;"], ["¹", "&#185;"], ["º", "&#186;"], ["»", "&#187;"], ["¼", "&#188;"], ["½", "&#189;"], ["¾", "&#190;"], ["¿", "&#191;"], ["À", "&#192;"], ["Á", "&#193;"], ["Â", "&#194;"], ["Ã", "&#195;"], ["Ä", "&#196;"], ["Å", "&#197;"], ["Æ", "&#198;"], ["Ç", "&#199;"], ["È", "&#200;"], ["É", "&#201;"], ["Ê", "&#202;"], ["Ë", "&#203;"], ["Ì", "&#204;"], ["Í", "&#205;"], ["Î", "&#206;"], ["Ï", "&#207;"], ["Ð", "&#208;"], ["Ñ", "&#209;"], ["Ò", "&#210;"], ["Ó", "&#211;"], ["Ô", "&#212;"], ["Õ", "&#213;"], ["Ö", "&#214;"], ["×", "&#215;"], ["Ø", "&#216;"], ["Ù", "&#217;"], ["Ú", "&#218;"], ["Û", "&#219;"], ["Ü", "&#220;"], ["Ý", "&#221;"], ["Þ", "&#222;"], ["ß", "&#223;"], ["à", "&#224;"], ["á", "&#225;"], ["â", "&#226;"], ["ã", "&#227;"], ["ä", "&#228;"], ["å", "&#229;"], ["æ", "&#230;"], ["ç", "&#231;"], ["è", "&#232;"], ["é", "&#233;"], ["ê", "&#234;"], ["ë", "&#235;"], ["ì", "&#236;"], ["í", "&#237;"], ["î", "&#238;"], ["ï", "&#239;"], ["ð", "&#240;"], ["ñ", "&#241;"], ["ò", "&#242;"], ["ó", "&#243;"], ["ô", "&#244;"], ["õ", "&#245;"], ["ö", "&#246;"], ["÷", "&#247;"], ["ø", "&#248;"], ["ù", "&#249;"], ["ú", "&#250;"], ["û", "&#251;"], ["ü", "&#252;"], ["ý", "&#253;"], ["þ", "&#254;"], ["ÿ", "&#255;"]];
    let find = [];
    let replace = [];
    for (let i = 0; i < toReplace.length; i++) {
      find[i] = toReplace[i][0];
      replace[i] = toReplace[i][1];
    }
    return StringUtils.replaceArray(str, find, replace);
  }

  public static startsWith(str: string, prefix: string): boolean {
    return str.slice(0, prefix.length) === prefix;
  }

  public static endsWith(str: string, suffix: string): boolean {
    return str.indexOf(suffix, str.length - suffix.length) !== -1;
  }

  public static removeEnd(str: string, remove: string): string {
    if (StringUtils.endsWith(str, remove)) {
      return str.substring(0, str.length - remove.length);
    }
    return str;
  }

  public static contains(str: string, search: string): boolean {
    return str.indexOf(search) !== -1;
  }

  public static containsByRegexp(str: string, search: RegExp): boolean {
    return search.test(str);
  }

  public static containsAny(str: string, search: Array<string>): boolean {
    for (let s of search) {
      if (this.contains(str, s)) {
        return true;
      }
    }
    return false;
  }

  public static equalsAny(str: string, search: Array<string>): boolean {
    for (let s of search) {
      if (str === s) {
        return true;
      }
    }
    return false;
  }

  public static trunc(str: string, n: number, html: boolean = true): string {
    const ellipsis = html ? '&hellip;' : '…';
    return str.length > n ? str.substr(0, n - 1) + ellipsis : str;
  }

  public static hashCode(str: string): number {
    let hash = 0, i, chr, len;
    if (str.length === 0) return hash;
    for (i = 0, len = str.length; i < len; i++) {
      chr = str.charCodeAt(i);
      hash = ((hash << 5) - hash) + chr;
      hash |= 0; // Convert to 32bit integer
    }
    return hash;
  }

  public static isBlank(str?: string): boolean {
    if (str) {
      return str.trim().length === 0;
    } else {
      return true;
    }
  }

  public static noneBlank(strings: Array<string>): boolean {
    return !this.anyBlank(strings);
  }

  public static anyBlank(strings: Array<string>): boolean {
    return strings.some(str => this.isBlank(str));
  }

  public static isEmpty(str?: string): boolean {
    if (str) {
      return str.length === 0;
    } else {
      return true;
    }
  }

  public static defaultString(str?: string, orElse?: string): string {
    if (str) {
      return str;
    } else if (orElse) {
      return orElse;
    } else {
      return '';
    }
  }

  public static substringAfter(str: string, search: string): string {
    let idx = str.indexOf(search) + search.length;
    return str.substring(idx);
  }

  public static capitalizeWords(str: string): string {
    return str.split(" ")
      .map(str => str.toLocaleLowerCase())
      .map(str => str.charAt(0).toUpperCase() + str.slice(1))
      .join(" ");
  }
}
