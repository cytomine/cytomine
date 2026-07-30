package be.cytomine.repository.ontology;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.ontology.AnnotationGroup;
import be.cytomine.domain.ontology.AnnotationLink;

@Repository
public interface AnnotationLinkRepository
    extends JpaRepository<AnnotationLink, Long>, JpaSpecificationExecutor<AnnotationLink> {

    Optional<AnnotationLink> findByAnnotationIdent(Long id);

    Optional<AnnotationLink> findByAnnotationIdentAndGroup(Long id, AnnotationGroup group);

    List<AnnotationLink> findAllByGroup(AnnotationGroup group);

    void deleteAllByGroup(AnnotationGroup group);

    @Modifying
    @Query(value = "UPDATE AnnotationLink al SET al.group = :newGroup WHERE al.group = :mergedGroup")
    void setMergedAnnotationGroup(AnnotationGroup newGroup, AnnotationGroup mergedGroup);
}
