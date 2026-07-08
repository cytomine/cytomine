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
