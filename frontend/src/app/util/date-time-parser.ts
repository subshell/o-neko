import isSameDay from "date-fns/isSameDay";
import format from "date-fns/format";
import isSameMonth from "date-fns/isSameMonth";
import {de, enGB} from "date-fns/locale";
import {currentOnekoLocale, OnekoLocale} from "../store/i18n/i18n.state";

export function relativeDateString(date: Date, locale: OnekoLocale = currentOnekoLocale() || 'en'): string {
  if (date === null) {
    return null;
  }
  let dateLocale: Locale;

  switch (locale) {
    case "de":
      dateLocale = de;
      break;
    default:
      dateLocale = enGB;
  }

  const now = new Date();
  if (isSameDay(now, date)) {
    return format(date, 'pp', {locale: dateLocale});
  } else if (isSameMonth(now, date)) {
    return format(date, 'do MMM pp', {locale: dateLocale});
  }
  return format(date, 'Pp', {locale: dateLocale});
}
