import {isGeometry} from '@/utils/app';

describe('app.js', () => {
  describe('isGeometry', () => {
    it('should return true when parameter type is geometry', () => {
      const parameter = {
        type: {id: 'geometry'}
      };

      expect(isGeometry(parameter)).toBe(true);
    });

    it('should return true when parameter is an array of geometry', () => {
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
    ])('should return false when parameter type is "%s"', (typeId) => {
      const parameter = {
        type: {id: typeId}
      };

      expect(isGeometry(parameter)).toBe(false);
    });

    it('should return false when parameter is an array but subType is not geometry', () => {
      const parameter = {
        type: {
          id: 'array',
          subType: {id: 'number'}
        }
      };

      expect(isGeometry(parameter)).toBe(false);
    });
  });
});
