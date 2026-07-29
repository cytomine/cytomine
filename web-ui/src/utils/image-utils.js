function isWebPSupported() {
  let elem = document.createElement('canvas');
  if (elem.getContext && elem.getContext('2d')) {
    return elem.toDataURL('image/webp').indexOf('data:image/webp') === 0;
  }

  // very old browser like IE 8, canvas not supported
  return false;
}

export const SUPPORT_WEBP = isWebPSupported();
export const IMAGE_FORMAT = (SUPPORT_WEBP) ? 'webp' : 'jpg';

export function splitImageUrl(rawUrl) {
  let url = new URL(rawUrl);
  let pathname = url.pathname.split('.')[0];
  let params = url.searchParams;

  return {
    host: `${url.protocol}//${url.host}`,
    pathname: pathname,
    params: params
  };
}

export function combineImageUrl({host, pathname, format, params}) {
  if (!(params instanceof URLSearchParams)) {
    params = new URLSearchParams(params);
  }
  let formattedParams = params.toString();
  let sep = (formattedParams.length > 0) ? '?' : '';
  return `${host}${pathname}.${format}${sep}${formattedParams}`;
}


export function changeImageUrlFormat(url, newFormat = IMAGE_FORMAT) {
  return combineImageUrl({format: newFormat, ...splitImageUrl(url)});
}
