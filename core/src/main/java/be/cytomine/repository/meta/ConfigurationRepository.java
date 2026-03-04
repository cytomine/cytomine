package be.cytomine.repository.meta;

import be.cytomine.domain.meta.Configuration;
import be.cytomine.domain.meta.ConfigurationReadingRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ConfigurationRepository extends JpaRepository<Configuration, Long>, JpaSpecificationExecutor<Configuration>  {


    List<Configuration> findAllByReadingRole(ConfigurationReadingRole role);

    List<Configuration> findAllByReadingRoleIn(List<ConfigurationReadingRole> role);

    Optional<Configuration> findByKey(String key);
}
