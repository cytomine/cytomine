package be.cytomine.domain.ontology;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.image.SliceInstance;
import be.cytomine.service.UrlApi;
import be.cytomine.utils.JsonObject;

@Entity
@Getter
@Setter
public class AnnotationTrack extends CytomineDomain implements Serializable {


    @NotNull
    private String annotationClassName;

    @NotNull
    @Column(name = "annotation_ident")
    private Long annotationIdent;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "slice_id", nullable = false)
    private SliceInstance slice;

    public void setAnnotation(AnnotationDomain annotation) {
        annotationClassName = annotation.getClass().getName();
        annotationIdent = annotation.getId();
    }


    public CytomineDomain buildDomainFromJson(JsonObject json, EntityManager entityManager) {
        AnnotationTrack annotationTrack = this;
        annotationTrack.id = json.getJSONAttrLong("id", null);
        annotationTrack.created = json.getJSONAttrDate("created");
        annotationTrack.updated = json.getJSONAttrDate("updated");

        Long annotationId = json.getJSONAttrLong("annotationIdent", -1L);
        String annotationClassName = json.getJSONAttrStr("annotationClassName");
        AnnotationDomain annotation = AnnotationDomain.getAnnotationDomain(
            entityManager,
            annotationId,
            annotationClassName
        );
        annotationTrack.setAnnotation(annotation);

        annotationTrack.slice = (SliceInstance) json.getJSONAttrDomain(
            entityManager,
            "slice",
            new SliceInstance(),
            false
        );
        annotationTrack.track = (Track) json.getJSONAttrDomain(entityManager, "track", new Track(), false);

        return annotationTrack;
    }

    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        JsonObject returnArray = CytomineDomain.getDataFromDomain(domain);
        AnnotationTrack annotationTrack = (AnnotationTrack) domain;
        returnArray.put("annotationIdent", annotationTrack.getAnnotationIdent());
        returnArray.put("annotationClassName", annotationTrack.getAnnotationClassName());

        returnArray.put("track", (annotationTrack.getTrack() != null ? annotationTrack.getTrack().getId() : null));
        returnArray.put("slice", (annotationTrack.getSlice() != null ? annotationTrack.getSlice().getId() : null));

        return returnArray;
    }

    @Override
    public String toJSON(UrlApi urlApi) {
        return getDataFromDomain(this).toJsonString();
    }

    @Override
    public JsonObject toJsonObject(UrlApi urlApi) {
        return getDataFromDomain(this);
    }

    public CytomineDomain container() {
        return track.container();
    }

}
