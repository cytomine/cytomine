package be.cytomine.repository.ontology;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import be.cytomine.domain.image.SliceInstance;
import be.cytomine.domain.ontology.AnnotationIndex;
import be.cytomine.domain.security.User;
import be.cytomine.dto.annotation.AnnotationIndexLightDTO;

public interface AnnotationIndexRepository
    extends JpaRepository<AnnotationIndex, Long>, JpaSpecificationExecutor<AnnotationIndex> {

    @Query(
        value = "SELECT"
            + " user_id AS user,"
            + " slice_id AS slice,"
            + " count_annotation AS countAnnotation,"
            + " count_reviewed_annotation AS countReviewedAnnotation"
            + " FROM annotation_index"
            + " WHERE slice_id = :slice",
        nativeQuery = true)
    List<AnnotationIndexLightDTO> findAllLightBySliceInstance(long slice);

    Optional<AnnotationIndexLightDTO> findOneBySliceAndUser(SliceInstance slice, User user);

    List<AnnotationIndexLightDTO> findAllBySlice(SliceInstance slice);

    void deleteAllBySlice(SliceInstance sliceInstance);

    void deleteAllByUser(User user);

    Optional<AnnotationIndexLightDTO> findOneBySliceInAndUser(List<SliceInstance> slices, User user);

    List<AnnotationIndexLightDTO> findAllBySliceIn(List<SliceInstance> slices);
}
