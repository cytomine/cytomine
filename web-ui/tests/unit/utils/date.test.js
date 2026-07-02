import {formatDate} from '@/utils/date';

describe('date.js', () => {
  describe('formatDate', () => {
    it.each([
      [null],
      [undefined],
      [0],
      [''],
    ])('should return empty string for falsy value %s', (value) => {
      expect(formatDate(value)).toBe('');
    });

    it('should treat numeric input as epoch seconds, not milliseconds', () => {
      const result = formatDate(1782721095);

      expect(result).toContain('2026');
      expect(result).not.toContain('1970');
    });

    it('should handle a decimal epoch-seconds float from the backend', () => {
      const result = formatDate(1782721095.497208);
      expect(result).toContain('2026');
    });

    it('should format an ISO string value correctly', () => {
      const result = formatDate('2026-06-29T10:30:00Z');
      expect(result).toContain('2026');
    });

    it('should not multiply an ISO string by 1000', () => {
      const result = formatDate('2026-06-29T10:30:00Z');

      expect(result).not.toBe('Invalid Date');
      expect(result).not.toContain('NaN');
    });
  });
});
