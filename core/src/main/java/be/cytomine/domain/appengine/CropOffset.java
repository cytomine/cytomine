package be.cytomine.domain.appengine;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.utils.JsonObject;

@ToString
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CropOffset extends CytomineDomain {
    private int x;
    private int y;

    @Column(name = "order_index")
    private int order;

    @Override
    public JsonObject toJsonObject() {
        return null;
    }
}
