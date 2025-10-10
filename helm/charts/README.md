Cytomine Helm Chart
===================

These helm charts will install a Cytomine system on a kubernetes host and it is built on the Cytomine Community Edition repo - https://github.com/cytomine/Cytomine-community-edition

- [Cytomine Helm Chart](#cytomine-helm-chart)
  - [Development environment](#development-environment)
    - [Setting up the environment and starting the minikube cluster on Ubuntu](#setting-up-the-environment-and-starting-the-minikube-cluster-on-ubuntu)
    - [Installing the cytomine helm chart](#installing-the-cytomine-helm-chart)
    - [Testing using Bigpicture DICOM File](#testing-using-bigpicture-dicom-file)
  - [Short Introduction to Kubernetes and Helm](#short-introduction-to-kubernetes-and-helm)
    - [Kubernetes](#kubernetes)
    - [Helm](#helm)
      - [Installing helm](#installing-helm)

## Development environment

This development environment has been tested in minikube. Instructions for
installing minikube can be found in the
[official instructions](https://minikube.sigs.k8s.io/docs/start/).

### Setting up the environment and starting the minikube cluster on Ubuntu

1. Start the minikube and we use [calico](https://docs.tigera.io/calico/latest/getting-started/kubernetes/minikube) for the networkpolicy

```
minikube start --network-plugin=cni --cni=calico
```
2. Enable the addons
```
minikube addons enable ingress
minikube addons enable ingress-dns
```
3. Add the `minikube-ip` as a DNS server
```
echo -e $"\nsearch local\nnameserver $(minikube ip)\ntimeout 5" \
| sudo tee -a /etc/resolv.conf
```
When you are done testing cytomine, you can cleanup `resolv.conf` or wait till this is done automatically when the dhcp lease is renewed.

4. Add minikibe 'cytomine domain name ' to host file
```
echo -e  $"\n$(minikube ip) cytomine.local\n$(minikube ip) ims.cytomine.local\n$(minikube ip) upload.cytomine.local" \
| sudo tee -a /etc/hosts
```
### Installing the cytomine helm chart

Once all the parts are in place, getting the system running is quite easy!

Run `helm install cytomine .` to install the system with the `cytomine` prefix.

You can use the `kubectl get pods` command to see the kubernetes pods come online.
It takes 5~6 min to deploy all the pods.If everything works, you should now be able to reach the system in your browser at `cytomine.local`.

Username to login is `admin` and the password can be retrived by running the command:
```
kubectl -n default get secret/cytomine-core-secret -o jsonpath='{.data.ADMIN_PASSWORD}' | base64 -d
```

### Testing using Bigpicture DICOM File

Test the new Cytomine version by uploading a DICOM files available [here](https://cytomine.com/collection/cmu-1/cmu-1-small-region-dicom) as per
Bigpicture project standard as produced by the [wsidicomizer tool](https://github.com/imi-bigpicture/wsidicomizer).
Currently, in order to be uploaded, DICOM files of WSI need to be placed within a folder, which should then be enclosed within a .zip archive.
The name of the folder will serve as the slide's name in the frontend.

## Short Introduction to Kubernetes and Helm
### Kubernetes

Kuberenetes is a system to keep systems running modularly in "pods". A pod is
basically one-or-more docker containers running together. Kubernetes can do all
kinds of fancy things that are outside the scope of this readme, but here are
some basics to get you started!

There are a lot of resource types in kubernetes for managing different parts of
an infrastructure, but we're only using a few in this project:

- A
  [Deployment](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/)
  defines how many pods of a certain type is desired, and tries to make sure
  that that number of pods is running and healthy. We run the cytomine docker
  containers as deployments.
- [Service](https://kubernetes.io/docs/concepts/services-networking/service/)s
  are are a way of defining network interfaces. We use services to map ports on
  the internal network to deployments.
- [ConfigMap](https://kubernetes.io/docs/concepts/configuration/configmap/)s
  are a way of passing configuration settings to other resources. We use them
  to set values and mount configuration files for containers.
- [Ingress](https://kubernetes.io/docs/concepts/services-networking/ingress/)
  is kubernetes system to handle communication between the internal kubernetes
  network and external networks. We use ingress to allow communication to the
  `nginx` pod.

You can interact with your kubernetes cluster using the
[kubectl](https://kubernetes.io/docs/reference/kubectl/overview/) tool. Here are
some examples:

- `kubectl get pods`: lists all pods.
- `kubectl get svc`: lists all services. This will list which ports are open on
  all pods.
- `kubectl get ing`: lists all ingress resources. Ingress resources are external
  communication interfaces. In this particular system there's currently only one
  ingress, getting to port 80 in the `nginx` container.

To get details from running pods you can use the `kubectl logs` command.
ex.
```
kubectl logs cytomine-core-6db5645df7-pcd9z
```
(use `kubectl logs -f` to get auto-updating logs). Note that you need to use the
full pod name including the id parts to get logs.

To get the state of a pod from kuberentes standpoint, you can use `describe`.
ex. `kubectl describe deployment cytomine-core` instead. That is particularly
useful to get information on why a pod isn't starting, as you can't get logs
from pods that aren't running.

### Helm

[Helm](https://helm.sh/docs/intro/install) is a package manager for kubernetes,
which allows us to use templates which will be automatically filled with values.
This makes management much more convenient.

#### Installing helm

Helm is a package manager for kubernetes. It allows us to configure, install and
uninstall the entire system much easier than using kubernetes to deploy all the
individual parts. Instructions for installing helm can be found
[here](https://helm.sh/docs/intro/install).


