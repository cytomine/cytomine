package org.cytomine.repository.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ACLService {

    private static final int READ_MASK = 1;
    private static final int WRITE_MASK = 2;

    private static final String ONTOLOGY_CLASS = "be.cytomine.domain.ontology.Ontology";

    private final JdbcTemplate jdbcTemplate;

    public boolean canWriteOntology(long userId, long ontologyId) {
        return hasPermission(userId, ontologyId, ONTOLOGY_CLASS, WRITE_MASK);
    }

    public boolean canReadOntology(long userId, long ontologyId) {
        return hasPermission(userId, ontologyId, ONTOLOGY_CLASS, READ_MASK);
    }

    private boolean hasPermission(long userId, long domainId, String domainClass, int requiredMask) {
        String sql = """
            SELECT COALESCE(MAX(ae.mask), -1)
            FROM sec_user u
            JOIN acl_sid sid ON sid.sid = u.username
            JOIN acl_object_identity aoi ON aoi.object_id_identity = ?
            JOIN acl_class ac ON ac.id = aoi.object_id_class AND ac.class = ?
            JOIN acl_entry ae ON ae.acl_object_identity = aoi.id AND ae.sid = sid.id
            WHERE u.id = ?
            """;

        Integer maxMask = jdbcTemplate.queryForObject(sql, Integer.class, domainId, domainClass, userId);
        return maxMask != null && maxMask >= requiredMask;
    }
}
