package be.cytomine.appengine.repositories.collection;

import be.cytomine.appengine.models.task.ParameterType;
import be.cytomine.appengine.models.task.collection.ReferencePersistence;
import be.cytomine.appengine.models.task.string.StringPersistence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReferencePersistenceRepository extends JpaRepository<ReferencePersistence, UUID> {
    ReferencePersistence findReferencePersistenceByParameterNameAndRunIdAndParameterType(
        String parameterName,
        UUID run,
        ParameterType parameterType
    );
}
