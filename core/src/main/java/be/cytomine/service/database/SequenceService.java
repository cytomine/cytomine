package be.cytomine.service.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Transactional
@Service
public class SequenceService {

    /**
     * Shared sequence for MongoDB document IDs.
     * This sequence is used by MongoDB documents (social domain) to generate unique IDs
     * that are consistent across the system. JPA entities use per-table sequences.
     */
    public final static String SEQ_NAME = "hibernate_sequence";

    @Autowired
    private EntityManager entityManager;

    /**
     * Get a new id number for MongoDB documents.
     * This is used by social domain entities stored in MongoDB.
     */
    public Long generateID()  {
        try {
            Query query = entityManager.createNativeQuery("select nextval('" + SEQ_NAME + "');");
            return (Long) query.getSingleResult();
        } catch (Exception e) {
            throw new RuntimeException("Cannot generate ID with sequence: " + e, e);
        }
    }
}
