// return all terms from an ontology object
export function getAllTerms(ontology) {
  return processNodes(ontology.children.array);
}

function processNodes(nodes) {
  let result = [];
  nodes.forEach(node => {
    result.push(node);
    if (node.children) {
      result.push(...processNodes(node.children));
    }
  });
  return result;
}
