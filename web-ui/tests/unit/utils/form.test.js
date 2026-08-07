import { decimal, email, min, positive, required, rules } from '@/utils/form.js';

function check(rule, value, name = 'field') {
  return rule({ value, fieldApi: { name } });
}

describe('utils/form.js', () => {

  describe('required', () => {
    it.each([undefined, null, '', '   ', []])('should reject %p', value => {
      expect(check(required, value)).toBe('This field is required');
    });

    it.each(['a', ' a ', 0, false, ['x'], { id: 1 }])('should accept %p', value => {
      expect(check(required, value)).toBeUndefined();
    });
  });

  describe('email', () => {
    it.each(['user@example.com', 'first.last@sub.example.co.uk'])('should accept %p', value => {
      expect(check(email, value)).toBeUndefined();
    });

    it.each(['user', 'user@', '@example.com', 'user@example', 'a b@example.com'])(
      'should reject %p', value => {
        expect(check(email, value)).toBe('Must be a valid email address');
      });

    it('should leave an empty value to the required rule', () => {
      expect(check(email, '')).toBeUndefined();
    });
  });

  describe('decimal', () => {
    it.each(['1', '1.5', '-2.25', '+3', '.5', 42])('should accept %p', value => {
      expect(check(decimal, value)).toBeUndefined();
    });

    it.each(['abc', '1.2.3', '1,5', '5px'])('should reject %p', value => {
      expect(check(decimal, value)).toBe('Must be a number');
    });

    it('should leave an empty value to the required rule', () => {
      expect(check(decimal, '')).toBeUndefined();
    });
  });

  describe('positive', () => {
    it.each(['1', 0.001, '2.5'])('should accept %p', value => {
      expect(check(positive, value)).toBeUndefined();
    });

    it.each(['0', 0, '-1'])('should reject %p', value => {
      expect(check(positive, value)).toBe('Must be positive');
    });

    it('should leave an empty value to the required rule', () => {
      expect(check(positive, '')).toBeUndefined();
    });
  });

  describe('min', () => {
    it('should accept a value of exactly the minimum length', () => {
      expect(check(min(8), '12345678')).toBeUndefined();
    });

    it('should reject a shorter value, naming the minimum in the message', () => {
      expect(check(min(8), '1234567')).toBe('Must have at least 8 characters');
    });

    it('should leave an empty value to the required rule', () => {
      expect(check(min(8), '')).toBeUndefined();
    });
  });

  describe('rules', () => {
    it('should report nothing when every rule passes', () => {
      expect(check(rules(required, decimal, positive), '1.5')).toBeUndefined();
    });

    it('should report the first failure only', () => {
      expect(check(rules(required, decimal), '')).toBe('This field is required');
      expect(check(rules(required, positive), '-1')).toBe('Must be positive');
    });
  });
});
