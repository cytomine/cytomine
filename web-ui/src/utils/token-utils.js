import Vue from 'vue';

export function appendShortTermToken(url, shortTermToken) {
  if (url === null || shortTermToken === null) {
    return url;
  }
  if (url.indexOf('?') === -1) {
    return url + '?authorization=Bearer ' + shortTermToken;
  } else {
    return url + '&authorization=' + encodeURI('Bearer ' + shortTermToken);
  }
}

export async function updateToken(minValidity = 70) {
  await Vue.$keycloak.updateToken(minValidity);
  return Vue.$keycloak.token;
}
