package be.cytomine.appengine.repositories.collection;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import be.cytomine.appengine.models.task.ParameterType;
import be.cytomine.appengine.models.task.collection.ReferencePersistence;

@Repository
public interface ReferencePersistenceRepository extends JpaRepository<ReferencePersistence, UUID> {
    ReferencePersistence findReferencePersistenceByParameterNameAndRunIdAndParameterType(
        String parameterName,
        UUID run,
        ParameterType parameterType
    );
}
