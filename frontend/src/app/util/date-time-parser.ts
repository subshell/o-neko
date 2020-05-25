import isSameDay from "date-fns/isSameDay";
import format from "date-fns/format";
import isSameMonth from "date-fns/isSameMonth";

export function relativeDateString(date: Date): string {
  if (date === null) {
    return null;
  }

  const now = new Date();
  if (isSameDay(now, date)) {
    return format(date, 'HH:mm:ss');
  } else if (isSameMonth(now, date)) {
    return format(date, 'do MMM HH:mm:ss');
  }
  return format(date, 'dd/MM/yy HH:mm:ss');
}
