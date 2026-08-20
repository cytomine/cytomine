package be.cytomine.domain.image;

import java.util.Optional;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.service.UrlApi;
import be.cytomine.utils.JsonObject;

@Entity
@Getter
@Setter
@DiscriminatorValue("be.cytomine.domain.image.NestedImageInstance")
public class NestedImageInstance extends ImageInstance {

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    private ImageInstance parent;

    @NotNull
    private Integer x;

    @NotNull
    private Integer y;

    public CytomineDomain buildDomainFromJson(JsonObject json, EntityManager entityManager) {
        NestedImageInstance nestedImageInstance = (NestedImageInstance) super.buildDomainFromJson(
            this,
            json,
            entityManager
        );
        nestedImageInstance.parent = (ImageInstance) json.getJSONAttrDomain(
            entityManager,
            "parent",
            new ImageInstance(),
            true
        );
        nestedImageInstance.x = json.getJSONAttrInteger("x", 0);
        nestedImageInstance.y = json.getJSONAttrInteger("y", 0);
        return nestedImageInstance;
    }

    public static JsonObject getDataFromDomain(CytomineDomain domain, UrlApi urlApi) {
        JsonObject returnArray = ImageInstance.getDataFromDomain(domain, urlApi);
        NestedImageInstance nestedImageInstance = (NestedImageInstance) domain;
        returnArray.put("parent", nestedImageInstance.getParentId());
        returnArray.put("x", nestedImageInstance.getX());
        returnArray.put("y", nestedImageInstance.getY());
        return returnArray;
    }

    public Long getParentId() {
        return Optional.ofNullable(parent).map(CytomineDomain::getId).orElse(null);
    }

    @Override
    public JsonObject toJsonObject(UrlApi urlApi) {
        return getDataFromDomain(this, urlApi);
    }

}
