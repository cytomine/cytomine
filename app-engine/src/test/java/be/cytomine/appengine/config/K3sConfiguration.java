package be.cytomine.appengine.config;

import io.fabric8.kubernetes.client.Config;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class K3sConfiguration {
    @Bean
    K3sContainer k3s() {
        K3sContainer k3sContainer =
            new K3sContainer(DockerImageName.parse("rancher/k3s:v1.21.3-k3s1"))
                .withCommand("server", "--disable", "metrics-server");

        // don't normally need to start the container in these @Bean methods but can't get the
        // config unless its started
        k3sContainer.start();

        String kubeConfigYaml = k3sContainer.getKubeConfigYaml();
        Config config = Config.fromKubeconfig(
            kubeConfigYaml);  // requires io.fabric8:kubernetes-client:5.11.0 or higher

        // in the absence of @ServiceConnection integration for this testcontainer, Jack the test
        // container URL into properties so it's picked up when I create a client in main app
        System.setProperty(Config.KUBERNETES_MASTER_SYSTEM_PROPERTY, config.getMasterUrl());
        System.setProperty(Config.KUBERNETES_CA_CERTIFICATE_DATA_SYSTEM_PROPERTY,
            config.getCaCertData());
        System.setProperty(Config.KUBERNETES_CLIENT_CERTIFICATE_DATA_SYSTEM_PROPERTY,
            config.getClientCertData());
        System.setProperty(Config.KUBERNETES_CLIENT_KEY_DATA_SYSTEM_PROPERTY,
            config.getClientKeyData());
        System.setProperty(Config.KUBERNETES_TRUST_CERT_SYSTEM_PROPERTY, "true");

        return k3sContainer;
    }

}
