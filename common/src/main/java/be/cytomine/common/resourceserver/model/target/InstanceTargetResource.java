package be.cytomine.common.resourceserver.model.target;

import be.cytomine.common.resourceserver.model.TargetResourceType;

public record InstanceTargetResource() implements TargetResource {
    @Override
    public TargetResourceType getResourceType() {
        return TargetResourceType.INSTANCE;
    }
}
