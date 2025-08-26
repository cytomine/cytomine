from cytomine import Cytomine
from cytomine.models import *

# Replace with actual values or -better- use args.
host = 'https://mycytomine.com'
public_key = 'AAA'
private_key = 'ZZZ'
id_term = 0

# Connect to Cytomine
with Cytomine(host, public_key, private_key) as cytomine:
    # Get all the available projects you have access to
    projects = ProjectCollection().fetch()

    # Instantiate a new list
    projects_with_p = ProjectCollection()

    # Filter the projects
    for project in projects:
        if project.name.startswith("P"):
            projects_with_p.append(project)

    # Get the term
    term = Term().fetch(id_term)

    # Count annotations having that term in filtered projects
    count = 0
    for project in projects_with_p:
        annotations = AnnotationCollection(project=project.id, term=term.id).fetch()
        count += len(annotations)

    print("There are {} annotations with term {}".format(count, term.name))