import moment from 'moment';

import {formatDate, formatMomentDate, formatMomentDuration} from '@/utils/date';

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

describe('formatMomentDate()', () => {
  it.each([
    [null],
    [undefined],
    [''],
    [0],
  ])('should return the input unchanged for falsy input %p', (value) => {
    expect(formatMomentDate(value, 'll')).toBe(value);
  });

  it('should return the original value when the date is invalid', () => {
    moment.suppressDeprecationWarnings = true;
    expect(formatMomentDate('not-a-date', 'll')).toBe('not-a-date');
    moment.suppressDeprecationWarnings = false;
  });

  it('should treat a number with fewer than 12 digits as a Unix seconds timestamp', () => {
    const unixSeconds = 1700000000;

    const expected = moment.unix(unixSeconds).format('YYYY-MM-DD HH:mm:ss');

    expect(formatMomentDate(unixSeconds, 'YYYY-MM-DD HH:mm:ss')).toBe(expected);
  });

  it('should treat a number with 12 or more digits as a milliseconds timestamp', () => {
    const milliseconds = 1700000000000;

    const expected = moment(milliseconds).format('YYYY-MM-DD HH:mm:ss');

    expect(formatMomentDate(milliseconds, 'YYYY-MM-DD HH:mm:ss')).toBe(expected);
  });

  it('should format a non-number value (ISO string) via moment', () => {
    const date = '2026-04-28T10:30:00Z';

    const expected = moment(date).format('ll');

    expect(formatMomentDate(date, 'll')).toBe(expected);
  });

  it('should format a Date instance', () => {
    const date = new Date('2023-06-01T08:00:00Z');

    const expected = moment(date).format('ll LT');

    expect(formatMomentDate(date, 'll LT')).toBe(expected);
  });
});

describe('formatMomentDuration()', () => {
  it('should call the given method on the moment duration object', () => {
    const value = 3600000;

    expect(formatMomentDuration(value, 'asHours')).toBe(moment.duration(value).asHours());
    expect(formatMomentDuration(value, 'asMinutes')).toBe(moment.duration(value).asMinutes());
  });

  it('should humanize a duration', () => {
    const value = 90000;

    expect(formatMomentDuration(value, 'humanize')).toBe(moment.duration(value).humanize());
  });

  it('should convert the raw value to a duration before calling the method', () => {
    const value = 60000;

    expect(formatMomentDuration(value, 'asSeconds')).toBe(60);
  });
});
