import {removeUsername} from '@/utils/string-utils';

describe('string-utils.js', () => {
  it.each([
    ['Admin User (admin)', 'Admin User'],
    ['Random Name (53bdeb64-59cc-4da9-8c3b-927a43b9d0ff)', 'Random Name'],
    ['Another Name (e455b971-fe80-4a28-b22d-6bc86ec15221)', 'Another Name'],
    ['StarDist (0.2.1) - 2026-29-01 15:15:15', 'StarDist (0.2.1) - 2026-29-01 15:15:15'],
  ])('should remove the username from users', (testName, expectedName) => {
    const displayName = removeUsername(testName);

    expect(displayName).toBe(expectedName);
  });
});
