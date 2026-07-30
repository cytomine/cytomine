import { readFile, writeFile, existsSync, mkdirSync } from 'fs';
import { join } from 'path';


function CSVToArray(strData, strDelim = ',') {
  // https://stackoverflow.com/questions/1293147/example-javascript-code-to-parse-csv-data
  // Create a regular expression to parse the CSV values.
  let objPattern = new RegExp((
    // Delimiters.
    '(\\' + strDelim + '|\\r?\\n|\\r|^)' +
        // Quoted fields.
        '(?:"([^"]*(?:""[^"]*)*)"|' +
        // Standard fields.
        '([^"\\' + strDelim + '\\r\\n]*))'
  ), 'gi');
  let arrData = [[]];
  let arrMatches = null;

  while ((arrMatches = objPattern.exec(strData)) !== null) {
    // Get the delimiter that was found.
    let strMatchedDelimiter = arrMatches[ 1 ];

    // Check to see if the given delimiter has a length (is not the start of string) and if it matches
    // field delimiter. If id does not, then we know that this delimiter is a row delimiter.
    if (strMatchedDelimiter.length && strMatchedDelimiter !== strDelim) {
      // Since we have reached a new row of data, add an empty row to our data array.
      arrData.push([]);
    }

    let strMatchedValue;

    // Now that we have our delimiter out of the way, let's check to see which kind of value we
    // captured (quoted or unquoted).
    if (arrMatches[ 2 ]) {
      // We found a quoted value. When we capture this value, unescape any double quotes.
      strMatchedValue = arrMatches[2].replace(new RegExp('""', 'g'),'"');
    } else {
        
      strMatchedValue = arrMatches[3]; // We found a non-quoted value.
    }
    // Now that we have our value string, let's add // it to the data array.
    arrData[arrData.length - 1].push(strMatchedValue);
  }

  // Return the parsed data.
  return arrData;
}

function createDeepField(obj, keys, value, overwrite = false) {
  if (keys.length < 1) {
    throw Error('key should contain at least one element');
  } else if (keys.length === 1) {
    let key = keys[0];
    if (!overwrite && key in obj) {
      throw Error(`key ${key} already contained`);
    }
    obj[key] = value;
    return;
  } 
  let currKey = keys[0];
  if (!(currKey in obj)) {
    obj[currKey] = {};
  }
  createDeepField(obj[currKey], keys.slice(1), value);
}

export function makeI18nJsons(csvPath, destPath) {
  readFile(csvPath, 'utf-8', (err, content) => {
    if (err) {
      throw err;
    }
    
    let csvContent = CSVToArray(content, ',');
    let headers = csvContent[0];
    let data = csvContent.slice(1);
    let languages = headers.slice(1);
    let i18n = {};
    for (let langIndex in languages) {
      i18n[languages[langIndex]] = {};
    }

    console.log('Found ' + data.length + ' translations in ' + languages.length + ' language(s): ' + JSON.stringify(languages));

    for (let rowIndex in data) {
      let row = data[rowIndex];
      let keys = row[0].split('.');
      for (let langIndex = 1; langIndex <= languages.length; ++langIndex) {
        createDeepField(i18n[languages[langIndex - 1]], keys, row[langIndex]);
      }
    }

    if (!existsSync(destPath)) {
      mkdirSync(destPath, { recursive: true });
    }

    for (let langIndex in languages) {
      let lang = languages[langIndex];
      writeFile(join(destPath, lang + '.i18n.json'), JSON.stringify(i18n[lang]), (err) => {
        if (err) {
          throw err;
        }
      });
    }
  });
}


if (process.argv.length === 4) {
  console.log('generate translations files for ' + JSON.stringify(process.argv));
  makeI18nJsons(process.argv[2], process.argv[3]);
}
