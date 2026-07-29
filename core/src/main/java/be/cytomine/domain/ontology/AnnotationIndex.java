package be.cytomine.domain.ontology;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import be.cytomine.domain.image.SliceInstance;
import be.cytomine.domain.security.User;
import be.cytomine.utils.JsonObject;

@Entity
@Getter
@Setter
public class AnnotationIndex {

    @Id
    @GeneratedValue(generator = "myGenerator")
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slice_id", nullable = true)
    private SliceInstance slice;

    @Version
    protected Integer version = 0;

    Long countAnnotation;

    Long countReviewedAnnotation;

    public static JsonObject getDataFromDomain(AnnotationIndex index) {
        JsonObject returnArray = new JsonObject();
        returnArray.put("user", index.getUser() != null ? index.getUser().getId() : null);
        returnArray.put("slice", index.getSlice() != null ? index.getSlice().getId() : null);
        returnArray.put("countAnnotation", index.getCountAnnotation());
        returnArray.put("countReviewedAnnotation", index.getCountReviewedAnnotation());
        return returnArray;
    }
}
