package be.cytomine.appengine.dto.misc;

import java.util.UUID;

public record TaskIdentifiers(UUID localTaskIdentifier, String storageIdentifier, String imageRegistryCompliantName) {}
