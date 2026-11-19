import { getKeycloak } from '../keycloak.js';

function decodeJwtPayload(token) {
  if (!token) {
    return null;
  }
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64.padEnd(base64.length + (4 - (base64.length % 4)) % 4, '=');
    return JSON.parse(atob(padded));
  } catch (e) {
    return null;
  }
}

export function tokenHasResourceRole(token, role, clientId = 'core') {
  const payload = decodeJwtPayload(token);
  if (!payload) {
    return false;
  }
  const roles = payload.resource_access?.[clientId]?.roles || [];
  return roles.includes(role);
}

export function appendShortTermToken(url, shortTermToken) {
  if (url == null || shortTermToken == null) {
    return url;
  }
  if (url.indexOf('?') === -1) {
    return url + '?authorization=Bearer ' + shortTermToken;
  } else {
    return url + '&authorization=' + encodeURI('Bearer ' + shortTermToken);
  }
}

export async function updateToken(minValidity = 70) {
  const keycloak = getKeycloak();
  await keycloak.updateToken(minValidity);
  return keycloak.token;
}
