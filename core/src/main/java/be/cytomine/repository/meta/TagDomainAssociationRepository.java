package be.cytomine.repository.meta;

import be.cytomine.domain.meta.Tag;
import be.cytomine.domain.meta.TagDomainAssociation;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

public interface TagDomainAssociationRepository extends JpaRepository<TagDomainAssociation, Long>, JpaSpecificationExecutor<TagDomainAssociation>  {

    long countByTag(Tag tag);

    List<TagDomainAssociation> findAllByTag(Tag tag);

    Optional<TagDomainAssociation> findByTagAndDomainClassNameAndDomainIdent(Tag tag, String domainClassName, Long domainIdent);


    @Override
    @EntityGraph(attributePaths = {"tag"})
    List<TagDomainAssociation> findAll(@Nullable Specification<TagDomainAssociation> spec, Sort sort);
}
