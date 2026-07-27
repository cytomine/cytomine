import moment from 'moment';

export function formatDate(date, locale) {
  if (!date) {
    return '';
  }

  return new Intl.DateTimeFormat(
    locale,
    {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }
  ).format(new Date(date));
}

export function formatMomentDate(value, format) {
  if (!value) {
    return value;
  }

  // A number with fewer than 12 digits is a Unix seconds timestamp, not milliseconds
  const date = typeof value === 'number' && String(value).length < 12
    ? moment.unix(value)
    : moment(value);

  return date.isValid() ? date.format(format) : value;
}

export function formatMomentDuration(value, method) {
  return moment.duration(value)[method]();
}
