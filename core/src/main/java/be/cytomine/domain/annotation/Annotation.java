package be.cytomine.domain.annotation;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.utils.JsonObject;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
public class Annotation extends CytomineDomain {
    @Id
    @GeneratedValue
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "layer_id")
    private AnnotationLayer annotationLayer;

    @Lob
    @JdbcTypeCode(Types.BINARY)
    private byte[] location;

    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        Annotation annotation = (Annotation) domain;
        JsonObject domainData = CytomineDomain.getDataFromDomain(domain);
        domainData.put("annotationLayer", annotation.getAnnotationLayer().getId());
        domainData.put("location", annotation.getLocation());

        return domainData;
    }

    @Override
    public CytomineDomain buildDomainFromJson(JsonObject json, EntityManager entityManager) {
        Annotation annotation = this;
        annotation.id = json.getJSONAttrLong("id", null);
        annotation.annotationLayer = (AnnotationLayer) json.getJSONAttrDomain(entityManager, "annotationLayer", new AnnotationLayer(), true);
        annotation.created = json.getJSONAttrDate("created");
        annotation.updated = json.getJSONAttrDate("updated");

        return annotation;
    }

    @Override
    public CytomineDomain container() {
        return this;
    }

    @Override
    public JsonObject toJsonObject() {
        return getDataFromDomain(this);
    }
}
