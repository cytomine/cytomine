package com.cytomine.registry.client.manager;

import java.io.IOException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.cytomine.registry.client.RegistryClient;
import com.cytomine.registry.client.image.Context;
import com.cytomine.registry.client.name.Reference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class RegistryManagerTest {

    private final RegistryManager REGISTRY_OPERATE = new RegistryManager();

    @BeforeAll
    static void init() throws IOException {
        // environment variables
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = loggerContext.getLogger("ROOT");
        logger.setLevel(Level.DEBUG);
        RegistryClient.config("http://registry:5000");

    }

    @Test
    void load() throws Exception {
        Context context = new Context();
        Reference reference = Reference.prepareReference("postomine:1.3");
        REGISTRY_OPERATE.load(context, reference);
        Assertions.assertEquals("sha256" +
            ":96b4c4806b2878c9e51a8036106b374834f28067a61331c47924a083054a0059",
            context.getConfig().getDigest());
        Assertions.assertEquals(2, context.getLayers().size());
    }
}
