export function stripIDfromKey(key) {
  if (!key.startsWith('MSMDAD')) {
    return key;
  }

  const keywords = [
    'BiologicalBeing',
    'Block',
    'Case',
    'Dataset',
    'Image',
    'Observation',
    'Slide',
    'Specimen',
    'Study'
  ];
  const toRemove = [
    'blocks',
    'cases',
    'images',
    'observations',
    'slides',
    'specimens'
  ];

  let words = key.split('.').filter(word => !toRemove.includes(word));
  let striped = words.map(word => {
    if (keywords.some(keyword => word.startsWith(keyword))) {
      word = word.substr(0, word.lastIndexOf('_'));
    }

    return word;
  });

  return striped.join('.');
}

export function filterAutoCompletion(key, suggestions) {
  let subKeys = key.split('.');
  let subKey = subKeys[subKeys.length - 2];

  return suggestions.filter(suggestion => suggestion.startsWith(subKey));
}
