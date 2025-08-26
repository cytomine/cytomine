---
title: Troubleshooting
---

# {{ $frontmatter.title }}

This section outlines the most common issues encountered when using MicroK8s and provides solutions for each.

## Invalid Certificates

When the network location of the cluster changes, the certificates become invalid because they are tied to a specific IP address.

```bash
error: error upgrading connection: error dialing backend: tls: failed to verify certificate: x509: certificate is valid for 192.168.1.100, 172.17.0.10, 172.19.0.20, 172.22.0.30, 172.18.0.40, 2a02:a03f:a1b6:e800:abcd:1234:5678:abcd, 2a02:a03f:a1b6:e800:9876:5432:1abc:6789, fd12:3456:789a:1::2, not 192.168.1.52
```

To solve this issue, you can run the following commands:

```bash
sudo microk8s refresh-certs --cert server.crt
```

```bash
sudo microk8s refresh-certs --cert front-proxy-client.crt
```

```bash
sudo microk8s refresh-certs --cert ca.crt
```

These commands will update the certificates to reflect the correct IP address.

## Unable to connect to the registry

After running a task in Cytomine, you may encounter an issue in the logs similar to the following:

```bash
failed to pull and unpack image "192.168.1.100:5000/com/cytomine/dummy/identity/boolean:1.0.0": failed to resolve reference "192.168.1.100:5000/com/cytomine/dummy/identity/boolean:1.0.0": failed to do request: Head "https://192.168.1.100:5000/v2/com/cytomine/dummy/identity/boolean/manifests/1.0.0": http: server gave HTTP response to HTTPS client
```

The issue occurs because there is no configuration file for the private registry to communicate with MicroK8s.

To solve it, follow the [Add registry](/admin-guide/clusters/microk8s/installation#add-registry) procedure to should resolve this issue.
