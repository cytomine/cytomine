package be.cytomine.domain.ontology;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.image.SliceInstance;
import be.cytomine.domain.security.User;
import be.cytomine.exceptions.WrongArgumentException;
import be.cytomine.service.UrlApi;
import be.cytomine.utils.JsonObject;

@Entity
@Getter
@Setter
public class UserAnnotation extends AnnotationDomain implements Serializable {


    Integer countReviewedAnnotations = 0;
    @Column(name = "user_id")
    private Long userId;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "annotation_term",
        joinColumns = {@JoinColumn(name = "user_annotation_id")},
        inverseJoinColumns = {@JoinColumn(name = "term_id")}
    )
    private List<Term> terms = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "annotation_track",
        joinColumns = {@JoinColumn(name = "annotation_ident")},
        inverseJoinColumns = {@JoinColumn(name = "track_id")}
    )
    private List<Track> tracks = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "annotation_link",
        joinColumns = {@JoinColumn(name = "annotation_ident")},
        inverseJoinColumns = {@JoinColumn(name = "group_id")}
    )
    private List<AnnotationLink> links = new ArrayList<>();

    public static JsonObject getDataFromDomain(CytomineDomain domain, UrlApi urlApi) {
        JsonObject returnArray = AnnotationDomain.getDataFromDomain(domain);
        UserAnnotation annotation = (UserAnnotation) domain;
        returnArray.put("cropURL", urlApi.getUserAnnotationCropWithAnnotationId(annotation.getId(), "png"));
        returnArray.put(
            "smallCropURL",
            urlApi.getUserAnnotationCropWithAnnotationIdWithMaxSize(annotation.getId(), 256, "png")
        );
        returnArray.put("url", urlApi.getUserAnnotationCropWithAnnotationId(annotation.getId(), "png"));
        returnArray.put(
            "imageURL",
            urlApi.getAnnotationURL(
                annotation.getImage().getProject().getId(),
                annotation.getImage().getId(),
                annotation.getId()
            )
        );
        returnArray.put("reviewed", annotation.hasReviewedAnnotation());

        return returnArray;
    }

    @PrePersist
    public void beforeCreate() {
        super.beforeCreate();
    }

    @PreUpdate
    public void beforeUpdate() {
        super.beforeUpdate();
    }

    @Override
    public List<Term> terms() {
        return terms;
    }

    /**
     * Check if annotation is reviewed
     *
     * @return True if annotation is linked with at least one review annotation
     */
    boolean hasReviewedAnnotation() {
        return countReviewedAnnotations > 0;
    }

    public List<Track> tracks() {
        return tracks;
    }

    /**
     * Get all annotation terms id
     *
     * @return Terms id list
     */
    public List<Long> termsId() {
        return terms().stream().map(CytomineDomain::getId).distinct().collect(Collectors.toList());

    }

    @Override
    public boolean isUserAnnotation() {
        return true;
    }

    public List<Long> tracksId() {
        return tracks().stream().map(CytomineDomain::getId).distinct().collect(Collectors.toList());

    }

    public List<Long> annotationLinksId() {
        return links.stream().map(CytomineDomain::getId).distinct().collect(Collectors.toList());
    }

    public List<Long> linkedAnnotations() {
        return links.stream().map(AnnotationLink::getAnnotationIdent).distinct().collect(Collectors.toList());
    }

    /**
     * Get all terms for automatic review If review is done "for all" (without manual user control), we add these term
     * to the new review annotation
     */
    public List<Term> termsForReview() {
        return terms().stream().distinct().collect(Collectors.toList());
    }

    /**
     * Check if its a review annotation
     */
    public boolean isReviewedAnnotation() {
        return false;
    }

    public CytomineDomain buildDomainFromJson(JsonObject json, EntityManager entityManager) {
        UserAnnotation annotation = this;
        annotation.id = json.getJSONAttrLong("id", null);

        if (json.containsKey("sliceObject")) {
            annotation.slice = (SliceInstance) json.get("sliceObject");
        } else {
            annotation.slice = (SliceInstance) json.getJSONAttrDomain(
                entityManager,
                "slice",
                new SliceInstance(),
                true
            );
        }

        if (json.containsKey("imageObject")) {
            annotation.image = (ImageInstance) json.get("imageObject");
        } else {
            annotation.image = (ImageInstance) json.getJSONAttrDomain(
                entityManager,
                "image",
                new ImageInstance(),
                true
            );
        }

        if (json.containsKey("userObject")) {
            annotation.userId = ((User) json.get("userObject")).getId();
        } else {
            annotation.userId = ((User) json.getJSONAttrDomain(entityManager, "user", new User(), true)).getId();
        }

        annotation.project = image.getProject();

        annotation.geometryCompression = json.getJSONAttrDouble("geometryCompression", 0D);

        annotation.created = json.getJSONAttrDate("created");
        annotation.updated = json.getJSONAttrDate("updated");


        if (json.containsKey("location") && json.get("location") instanceof Geometry) {
            annotation.location = (Geometry) json.get("location");
        } else {
            try {
                annotation.location = new WKTReader().read(json.getJSONAttrStr("location"));
            } catch (ParseException ex) {
                throw new WrongArgumentException(ex.toString());
            }
        }

        if (annotation.location == null) {
            throw new WrongArgumentException("Geometry is null: 0 points");
        }

        if (annotation.location.getNumPoints() < 1) {
            throw new WrongArgumentException("Geometry is empty:" + annotation.location.getNumPoints() + " points");
        }

        return annotation;
    }

    @Override
    public JsonObject toJsonObject(UrlApi urlApi) {
        return getDataFromDomain(this, urlApi);
    }

    @Override
    public Long userDomainCreator() {
        return userId;
    }
}
