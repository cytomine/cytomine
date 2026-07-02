export function formatDate(value) {
  if (!value) {
    return '';
  }

  const date = typeof value === 'number' ? new Date(value * 1000) : new Date(value);
  return new Intl.DateTimeFormat(
    undefined,
    {month: 'short', day: 'numeric', year: 'numeric', hour: 'numeric', minute: '2-digit'}
  ).format(date);
}
