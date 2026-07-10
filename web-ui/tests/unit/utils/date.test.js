import {formatDate} from '@/utils/date';

const OPTIONS = {
  day: '2-digit',
  month: 'short',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
};

describe('formatDate()', () => {
  it.each([
    [null],
    [undefined],
    [''],
    [0],
  ])('should return empty string for falsy input %p', (value) => {
    expect(formatDate(value)).toBe('');
  });

  it('should format an ISO date string using the given locale', () => {
    const date = '2026-04-28T10:30:00Z';
    const locale = 'en-US';

    const expected = new Intl.DateTimeFormat(locale, OPTIONS).format(new Date(date));

    expect(formatDate(date, locale)).toBe(expected);
  });

  it('should format a Date instance', () => {
    const date = new Date('2023-06-01T08:00:00Z');
    const locale = 'en-US';

    const expected = new Intl.DateTimeFormat(locale, OPTIONS).format(date);

    expect(formatDate(date, locale)).toBe(expected);
  });

  it('should fall back to the default locale when none is given', () => {
    const date = '2024-01-01T00:00:00Z';

    const expected = new Intl.DateTimeFormat(undefined, OPTIONS).format(new Date(date));

    expect(formatDate(date)).toBe(expected);
  });

  it('should produce locale-specific output', () => {
    const date = '2026-01-15T12:00:00Z';

    expect(formatDate(date, 'en-US')).not.toBe(formatDate(date, 'fr-FR'));
  });
});
