package be.cytomine.domain.appengine;

import jakarta.persistence.Entity;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.utils.JsonObject;

@Entity
public class CropOffset extends CytomineDomain {
    private int x;
    private int y;

    @Override
    public JsonObject toJsonObject() {
        return null;
    }
}
