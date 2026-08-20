import _ from 'lodash';

/**
 * Return a Regexp based on the provided search string, and using * as a wildcard character.
 * @param {String} str The string entered by the user
 * @return {RegExp}
 */
export function getWildcardRegexp(str) {
  let escapedString = _.escapeRegExp(str); // escape all RegExp special characters (only * should have a special behaviour)
  return new RegExp('(' + escapedString.split('\\*').join('.*') + ')', 'i');
}

/**
 * Convert the string into a date time
 * @param {String} str The string value
 * @return {Date} The converted date time
 */
export function convertToDate(str) {
  let dateString = '';

  if (str.length === 8) {
    dateString = str.replace(/(\d{4})(\d{2})(\d{2})/, '$1-$2-$3');
  } else {
    dateString = str.replace(/(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})/, '$1-$2-$3 $4:$5:$6');
  }

  return new Date(dateString);
}

/**
 * Check if the string is a valid date
 * @param {String} str The string value
 * @return {boolean}
 */
export function isDate(str) {
  return (new Date(str)).toString() !== 'Invalid Date';
}

/**
 * Check if a string is a valid number.
 * @param {String} str The string entered by the user
 * @return {boolean}
 */
export function isNumeric(str) {
  return !isNaN(str);
}
