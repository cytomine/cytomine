package be.cytomine.repository.meta;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import be.cytomine.domain.meta.Property;

public interface PropertyRepository extends JpaRepository<Property, Long>, JpaSpecificationExecutor<Property> {

    Optional<Property> findByKey(String key);

    Optional<Property> findByDomainIdentAndKey(Long domainIdent, String key);

    List<Property> findAllByDomainIdentAndKeyIn(Long domainIdent, Collection<String> keys);

    void deleteAllByDomainIdentAndKeyIn(Long domainIdent, Collection<String> keys);

    List<Property> findAllByDomainIdent(Long id);

    @Query(
        value = "SELECT * "
            + "FROM property p "
            + "WHERE p.domain_ident = :domainIdent "
            + "AND NOT EXISTS ( "
            + "    SELECT 1 "
            + "    FROM unnest(STRING_TO_ARRAY(:excludedKeys, ';')) AS substr "
            + "    WHERE p.key LIKE (substr || '%')"
            + ")",
        nativeQuery = true
    )
    List<Property> findByDomainIdentAndExcludedKeys(
        @Param("domainIdent") Long domainIdent,
        @Param("excludedKeys") String excludedKeys
    );

    void deleteAllByDomainIdent(Long id);
}
