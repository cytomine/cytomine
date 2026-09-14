package org.cytomine.repository.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ACLService {

    private static final int READ_MASK = 1;
    private static final int WRITE_MASK = 2;
    private static final int DELETE_MASK = 8;
    private static final int ADMINISTRATION_MASK = 16;

    private static final String ONTOLOGY_CLASS = "be.cytomine.domain.ontology.Ontology";
    private static final String PROJECT_CLASS = "be.cytomine.domain.project.Project";
    private static final String STORAGE_CLASS = "be.cytomine.domain.image.server.Storage";

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

    public void grantOntologyOwnerPermission(long userId, long ontologyId) {
        grantOwnerPermission(userId, ontologyId, ONTOLOGY_CLASS);
    }

    public void grantStorageOwnerPermission(long userId, long storageId) {
        grantOwnerPermission(userId, storageId, STORAGE_CLASS);
    }

    public boolean canReadStorage(long userId, long storageId) {
        return isAdmin(userId) || hasPermission(userId, storageId, STORAGE_CLASS, READ_MASK);
    }

    public boolean canWriteStorage(long userId, long storageId) {
        return isAdmin(userId) || hasPermission(userId, storageId, STORAGE_CLASS, WRITE_MASK);
    }

    public boolean canDeleteStorage(long userId, long storageId) {
        return isAdmin(userId) || hasPermission(userId, storageId, STORAGE_CLASS, DELETE_MASK);
    }

    public boolean canWriteRole(long userId) {
        return isAdmin(userId);
    }

    public boolean canDeleteRole(long userId) {
        return isAdmin(userId);
    }

    public boolean canWriteUserRole(long userId) {
        return isAdmin(userId);
    }

    public boolean canDeleteUserRole(long userId) {
        return isAdmin(userId);
    }

    public List<Long> getAccessibleStorageIds(long userId) {
        if (isAdmin(userId)) {
            return null;
        }
        String sql = """
            SELECT aoi.object_id_identity
            FROM sec_user u
            JOIN acl_sid sid ON sid.sid = u.username
            JOIN acl_entry ae ON ae.sid = sid.id AND ae.mask >= ?
            JOIN acl_object_identity aoi ON aoi.id = ae.acl_object_identity
            JOIN acl_class ac ON ac.id = aoi.object_id_class AND ac.class = ?
            WHERE u.id = ?
            """;
        return jdbcTemplate.queryForList(sql, Long.class, READ_MASK, STORAGE_CLASS, userId);
    }

    public boolean canReadDomain(long userId, long domainId, String domainClassName) {
        return isAdmin(userId) || hasPermission(userId, domainId, domainClassName, READ_MASK);
    }

    public boolean canWriteDomain(long userId, long domainId, String domainClassName) {
        return isAdmin(userId) || hasPermission(userId, domainId, domainClassName, WRITE_MASK);
    }

    public boolean canDeleteDomain(long userId, long domainId, String domainClassName) {
        return isAdmin(userId) || hasPermission(userId, domainId, domainClassName, DELETE_MASK);
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

    private void grantOwnerPermission(long userId, long domainId, String domainClass) {
        String username = jdbcTemplate.queryForObject(
            "SELECT username FROM sec_user WHERE id = ?", String.class, userId);

        Long sid = getOrInsertAclSid(username);
        Long aclClassId = getOrInsertAclClass(domainClass);
        Long aclObjectIdentity = getOrInsertAclObjectIdentity(domainId, aclClassId, sid);

        List<Long> existingEntry = jdbcTemplate.queryForList(
            "SELECT id FROM acl_entry WHERE acl_object_identity = ? AND sid = ? AND mask = ?",
            Long.class,
            aclObjectIdentity,
            sid,
            ADMINISTRATION_MASK
        );
        if (existingEntry.isEmpty()) {
            int aceOrder = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(ace_order), -1) + 1 FROM acl_entry WHERE acl_object_identity = ?",
                Integer.class,
                aclObjectIdentity
            );
            jdbcTemplate.update(
                "INSERT INTO acl_entry(id, ace_order, acl_object_identity, audit_failure, audit_success, "
                    + "granting, mask, sid) "
                    + "VALUES (nextval('hibernate_sequence'), ?, ?, false, false, true, ?, ?)",
                aceOrder,
                aclObjectIdentity,
                ADMINISTRATION_MASK,
                sid
            );
        }
    }

    private Long getOrInsertAclSid(String username) {
        List<Long> ids = jdbcTemplate.queryForList("SELECT id FROM acl_sid WHERE sid = ?", Long.class, username);
        if (!ids.isEmpty()) {
            return ids.getFirst();
        }
        jdbcTemplate.update(
            "INSERT INTO acl_sid(id, principal, sid) VALUES (nextval('hibernate_sequence'), true, ?)", username);
        return jdbcTemplate.queryForObject("SELECT id FROM acl_sid WHERE sid = ?", Long.class, username);
    }

    private Long getOrInsertAclClass(String domainClass) {
        List<Long> ids = jdbcTemplate.queryForList("SELECT id FROM acl_class WHERE class = ?", Long.class,
            domainClass);
        if (!ids.isEmpty()) {
            return ids.getFirst();
        }
        jdbcTemplate.update(
            "INSERT INTO acl_class(id, class) VALUES (nextval('hibernate_sequence'), ?)", domainClass);
        return jdbcTemplate.queryForObject("SELECT id FROM acl_class WHERE class = ?", Long.class, domainClass);
    }

    private Long getOrInsertAclObjectIdentity(long domainId, long aclClassId, long ownerSid) {
        List<Long> ids = jdbcTemplate.queryForList(
            "SELECT id FROM acl_object_identity WHERE object_id_identity = ? AND object_id_class = ?",
            Long.class,
            domainId,
            aclClassId
        );
        if (!ids.isEmpty()) {
            return ids.getFirst();
        }
        jdbcTemplate.update(
            "INSERT INTO acl_object_identity(id, object_id_class, entries_inheriting, object_id_identity, "
                + "owner_sid, parent_object) "
                + "VALUES (nextval('hibernate_sequence'), ?, true, ?, ?, null)",
            aclClassId,
            domainId,
            ownerSid
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM acl_object_identity WHERE object_id_identity = ? AND object_id_class = ?",
            Long.class,
            domainId,
            aclClassId
        );
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
