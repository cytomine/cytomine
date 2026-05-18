package be.cytomine.service.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Transactional
@Service
public class SequenceService {

    public static final String SEQ_NAME = "hibernate_sequence";

    private final EntityManager entityManager;

    /**
     * Get a new id number
     */
    public Long generateID() {
        try {
            Query query = entityManager.createNativeQuery("select nextval('" + SEQ_NAME + "');");
            return (Long) query.getSingleResult();
        } catch (Exception e) {
            throw new RuntimeException("Cannot generate ID with sequence: " + e, e);
        }
    }
}
