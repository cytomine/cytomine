import { UserRole } from '@/constants/UserRole.js';

export const rolesMapping = Object.freeze({
  [UserRole.GUEST]: { label: 'guest', index: 0, class: 'is-light' },
  [UserRole.USER]: { label: 'user', index: 1, class: 'is-link' },
  [UserRole.ADMIN]: { label: 'admin', index: 2, class: 'is-success' },
});
