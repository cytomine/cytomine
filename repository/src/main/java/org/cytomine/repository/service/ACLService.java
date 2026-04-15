package org.cytomine.repository.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ACLService {

    private static final int READ_MASK = 1;
    private static final int WRITE_MASK = 2;
    private static final int DELETE_MASK = 8;

    private static final String ONTOLOGY_CLASS = "be.cytomine.domain.ontology.Ontology";
    private static final String PROJECT_CLASS = "be.cytomine.domain.project.Project";

    private final JdbcTemplate jdbcTemplate;

    public boolean canWriteOntology(long userId, long ontologyId) {
        return isAdmin(userId) || hasPermission(userId, ontologyId, ONTOLOGY_CLASS, WRITE_MASK);
    }

    public boolean canReadOntology(long userId, long ontologyId) {
        return isAdmin(userId) || hasPermission(userId, ontologyId, ONTOLOGY_CLASS, READ_MASK);
    }

    public boolean canReadProject(long userId, long projectId) {
        return isAdmin(userId) || hasPermission(userId, projectId, PROJECT_CLASS, READ_MASK);
    }

    public boolean canDeleteOntology(long userId, long ontologyId) {
        return isAdmin(userId) || hasPermission(userId, ontologyId, ONTOLOGY_CLASS, DELETE_MASK);
    }

    /**
     * Mirrors core's checkFullOrRestrictedForOwner: in CLASSIC mode any project member can edit;
     * in RESTRICTED mode only the owner (or an admin) can edit.
     */
    public boolean canEditForOwner(long userId, long projectId, long ownerId) {
        if (isAdmin(userId)) {
            return true;
        }
        if (!hasPermission(userId, projectId, PROJECT_CLASS, READ_MASK)) {
            return false;
        }
        String mode = jdbcTemplate.queryForObject(
            "SELECT mode FROM project WHERE id = ?", String.class, projectId);
        return "CLASSIC".equals(mode) || userId == ownerId;
    }

    public boolean isAdmin(long userId) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM sec_user_sec_role usr
            JOIN sec_role sr ON sr.id = usr.sec_role_id
            WHERE usr.sec_user_id = ?
            AND sr.authority IN ('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')
            """;

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, userId));
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
