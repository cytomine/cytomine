package be.cytomine.repository.meta;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import be.cytomine.domain.meta.Configuration;
import be.cytomine.domain.meta.ConfigurationReadingRole;

public interface ConfigurationRepository extends JpaRepository<Configuration, Long>, JpaSpecificationExecutor<Configuration>  {


    List<Configuration> findAllByReadingRole(ConfigurationReadingRole role);

    List<Configuration> findAllByReadingRoleIn(List<ConfigurationReadingRole> role);

    Optional<Configuration> findByKey(String key);
}
