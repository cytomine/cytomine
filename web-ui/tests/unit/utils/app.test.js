import {formatTaskName, isGeometry} from '@/utils/app';

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

  describe('formatTaskName', () => {
    it('should format task name with version and date', () => {
      const taskRun = {
        createdAt: '2026-04-28T10:30:00Z',
        task: {name: 'Nuclei Segmentation', version: '1.2.3'},
      };

      const result = formatTaskName(taskRun);

      expect(result).toContain(`${taskRun.task.name} (${taskRun.task.version})`);
      expect(result).toContain(new Date(taskRun.createdAt).toLocaleString());
    });

    it('should handle different dates correctly', () => {
      const taskRun = {
        createdAt: '2023-06-01T08:00:00Z',
        task: {name: 'Object Detection', version: '0.9.0'},
      };

      const result = formatTaskName(taskRun);

      const formattedDate = (new Date(taskRun.createdAt)).toLocaleString();
      const expectedName = `${taskRun.task.name} (${taskRun.task.version}) - ${formattedDate}`;
      expect(result).toBe(expectedName);
    });
  });
});
