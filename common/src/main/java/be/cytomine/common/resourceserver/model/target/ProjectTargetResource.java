package be.cytomine.common.resourceserver.model.target;

import be.cytomine.common.resourceserver.model.TargetResourceType;

public record ProjectTargetResource(long id) implements TargetResource {
    @Override
    public TargetResourceType getResourceType() {
        return TargetResourceType.PROJECT;

    }
}
