package be.cytomine.repository.meta;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import be.cytomine.domain.meta.AttachedFile;

public interface AttachedFileRepository
    extends JpaRepository<AttachedFile, Long>, JpaSpecificationExecutor<AttachedFile> {
    List<AttachedFile> findAllByDomainClassNameAndDomainIdent(String domainClassName, Long domainIdent);
}
