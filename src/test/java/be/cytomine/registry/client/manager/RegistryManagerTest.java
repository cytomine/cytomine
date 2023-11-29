package io.github.ya_b.registry.client.manager;

import io.github.ya_b.registry.client.http.auth.Authenticator;
import io.github.ya_b.registry.client.http.auth.Scope;
import io.github.ya_b.registry.client.image.Context;
import io.github.ya_b.registry.client.name.Reference;
import kotlin.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RegistryManagerTest {

    private final RegistryManager REGISTRY_OPERATE = new RegistryManager();

    private final Authenticator AUTHENTICATOR = Authenticator.instance();

    @Test
    void load() throws Exception {
        Context context = new Context();
        Reference reference = Reference.parse("openjdk:17-alpine@sha256:a996cdcc040704ec6badaf5fecf1e144c096e00231a29188596c784bcf858d05");
        context.setToken(AUTHENTICATOR.getToken(new Pair<>(Scope.PULL, reference)));
        REGISTRY_OPERATE.load(context, reference);
        Assertions.assertEquals("sha256:264c9bdce361556ba6e685e401662648358980c01151c3d977f0fdf77f7c26ab", context.getConfig().getDigest());
        Assertions.assertEquals(3, context.getLayers().size());
    }
}