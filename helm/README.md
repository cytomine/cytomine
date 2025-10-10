# Cytomine - Helm

This is the new official Helm Chart for cytomine.

It is based on [NBIS version](https://github.com/NBISweden/cytomine-bp-helm)

For now it is under heavy development so please allow us some time before using it.


## Debug

To see what is generated you can use:
`helm install charts/cytomine/ --debug --dry-run --generate-name -f example/values.yaml`
