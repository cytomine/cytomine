package be.cytomine.domain.annotation;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import be.cytomine.domain.CytomineDomain;


@Setter
@Getter
@Entity
public class AnnotationLayer extends CytomineDomain {

    @NotBlank
    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "annotationLayer", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Annotation> annotations = new HashSet<>();

    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        AnnotationLayer annotationLayer = (AnnotationLayer) domain;
        JsonObject domainData = CytomineDomain.getDataFromDomain(domain);
        domainData.put("name", annotationLayer.getName());

        return domainData;
    }

    @Override
    public CytomineDomain buildDomainFromJson(JsonObject json, EntityManager entityManager) {
        AnnotationLayer annotationLayer = this;
        annotationLayer.id = json.getJSONAttrLong("id", null);
        annotationLayer.name = json.getJSONAttrStr("name", true);
        annotationLayer.created = json.getJSONAttrDate("created");
        annotationLayer.updated = json.getJSONAttrDate("updated");

        return annotationLayer;
    }

    @Override
    public CytomineDomain container() {
        return this;
    }

    @Override
    public JsonObject toJsonObject() {
        return getDataFromDomain(this);
    }

    @Override
    public String toString() {
        return String.format("AnnotationLayer{id=%d, name='%s'}", id, name);
    }
}
