package be.cytomine.appengine.config;

import java.time.Duration;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class K3sConfiguration {
    @Bean
    K3sContainer k3s() {
        K3sContainer k3sContainer = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.30.14-rc3-k3s3"))
            .withCommand("server", "--disable", "metrics-server")
            .withStartupTimeout(Duration.ofMinutes(3));

        // don't normally need to start the container in these @Bean methods but can't get the
        // config unless its started
        k3sContainer.start();

        String kubeConfigYaml = k3sContainer.getKubeConfigYaml();
        // requires io.fabric8:kubernetes-client:5.11.0 or higher
        Config config = Config.fromKubeconfig(kubeConfigYaml);

        // in the absence of @ServiceConnection integration for this testcontainer, Jack the test
        // container URL into properties so it's picked up when I create a client in main app
        System.setProperty(Config.KUBERNETES_MASTER_SYSTEM_PROPERTY, config.getMasterUrl());
        System.setProperty(Config.KUBERNETES_CA_CERTIFICATE_DATA_SYSTEM_PROPERTY, config.getCaCertData());
        System.setProperty(Config.KUBERNETES_CLIENT_CERTIFICATE_DATA_SYSTEM_PROPERTY, config.getClientCertData());
        System.setProperty(Config.KUBERNETES_CLIENT_KEY_DATA_SYSTEM_PROPERTY, config.getClientKeyData());
        System.setProperty(Config.KUBERNETES_TRUST_CERT_SYSTEM_PROPERTY, "true");

        awaitApiServerAuthorized(config);

        return k3sContainer;
    }

    private void awaitApiServerAuthorized(Config config) {
        Duration timeout = Duration.ofMinutes(1);
        Duration pollInterval = Duration.ofSeconds(1);
        long deadline = System.nanoTime() + timeout.toNanos();

        try (KubernetesClient client = new KubernetesClientBuilder().withConfig(config).build()) {
            while (true) {
                try {
                    client.namespaces().list();
                    return;
                } catch (RuntimeException e) {
                    if (System.nanoTime() >= deadline) {
                        throw new IllegalStateException("K3s API server did not become authorized in time", e);
                    }
                    try {
                        Thread.sleep(pollInterval.toMillis());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Interrupted while waiting for K3s API server", ie);
                    }
                }
            }
        }
    }
}
