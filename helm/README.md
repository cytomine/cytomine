# Cytomine - Helm

This is the new official Helm Chart for cytomine.

It is based on [NBIS version](https://github.com/NBISweden/cytomine-bp-helm)

For now it is under heavy development so please allow us some time before using it.

## How to use

The faster way to start it locally is to stay at root project level, and run:
- `docker-compose -f ./helm/compose.yaml up`
- `helm upgrade --kubeconfig=./.kube/shared/config -f ./helm/example/values.yaml cytomine-platform ./helm/charts/cytomine/ -n cytomine-local --install`

You need to have your /etc/hosts with
`172.16.238.15 cytomine.local` (which is the IP in the `docker-compose.yaml`).

After that you should be able to navigate to http://cytomine.local.


## Debug

To see what is generated you can use:
`helm install charts/cytomine/ --debug --dry-run --generate-name -f example/values.yaml`
