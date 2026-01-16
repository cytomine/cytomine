import {isGeometry} from '@/utils/app';

describe('app.js', () => {
  describe('isGeometry', () => {
    it('should return true when type is "geometry"', () => {
      const parameter = {
        type: {id: 'geometry'}
      };

      expect(isGeometry(parameter)).toBe(true);
    });

    it('should return true when type is "array" and subType is "geometry"', () => {
      const parameter = {
        type: {
          id: 'array',
          subType: {id: 'geometry'}
        }
      };

      expect(isGeometry(parameter)).toBe(true);
    });

    it.each([
      ['integer'],
      ['number'],
      ['string'],
      ['enumeration'],
      ['boolean'],
      ['image'],
      ['file'],
    ])('should return false when type is "%s"', (typeId) => {
      const parameter = {
        type: {id: typeId}
      };

      expect(isGeometry(parameter)).toBe(false);
    });

    it.each([
      ['integer'],
      ['number'],
      ['string'],
      ['enumeration'],
      ['boolean'],
      ['image'],
      ['file'],
    ])('should return false when type is "array" but subType is "%s"', (typeId) => {
      const parameter = {
        type: {
          id: 'array',
          subType: {id: typeId}
        }
      };

      expect(isGeometry(parameter)).toBe(false);
    });
  });
});
