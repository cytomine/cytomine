package be.cytomine.repository.meta;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import be.cytomine.domain.meta.Description;

public interface DescriptionRepository extends JpaRepository<Description, Long>, JpaSpecificationExecutor<Description> {
    Optional<Description> findByDomainIdentAndDomainClassName(Long id, String className);
}
