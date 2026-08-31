package be.cytomine.repository.processing;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.processing.ImageFilter;

@Repository
public interface ImageFilterRepository extends JpaRepository<ImageFilter, Long>, JpaSpecificationExecutor<ImageFilter> {

    Optional<ImageFilter> findByName(String name);
}
