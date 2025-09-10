# Configuration

## Add Cytomine registry to MicroK8s

After installing the Cytomine, you will have to set up a configuration file to allow MicroK8s to communicate with the private registry.

1. Get the IP address of the registry container, using

    ```bash
    sudo docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' cytomine-registry-1
    ```

2. Create the directory and configuration file:

   - `mkdir -p /var/snap/microk8s/current/args/certs.d/<registry-ip>:5000`
   - `touch /var/snap/microk8s/current/args/certs.d/<registry-ip>:5000/hosts.toml`

   Where `<registry-ip>` is the output of step 1.

   > Normally, sudo privileges are not required.

3. Add the following lines inside the `hosts.toml` file:

   ```toml
   server = "http://<registry-ip>:5000"

   [host."http://<registry-ip>:5000"]
   capabilities = ["pull", "resolve"]
   ```

4. Restart MicroK8s to have the new configuration loaded:

   ```bash
   microk8s stop
   ```

   ```bash
   microk8s start
   ```

> More information are available at the [_How to work with a private registry_](https://microk8s.io/docs/registry-private) section of the official documentation of MicroK8s.

MicroK8s is now ready for use by Cytomine, you can proceed with the [installation](/admin-guide/ce/installation.md#installation).
