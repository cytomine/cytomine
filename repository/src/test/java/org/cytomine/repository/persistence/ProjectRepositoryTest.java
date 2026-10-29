package org.cytomine.repository.persistence;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import org.cytomine.repository.RepositoryApp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.common.PostGisTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@SpringBootTest(classes = RepositoryApp.class)
@Import(PostGisTestConfiguration.class)
@Transactional
class ProjectRepositoryTest {
    private static final String PROJECT_CLASS = "be.cytomine.domain.project.Project";

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private Long aclClassId() {
        List<Long> ids =
            jdbcTemplate.queryForList("SELECT id FROM acl_class WHERE class = ?", Long.class, PROJECT_CLASS);
        if (!ids.isEmpty()) {
            return ids.get(0);
        }
        return jdbcTemplate.queryForObject(
            "INSERT INTO acl_class (id, class) VALUES (nextval('hibernate_sequence'), ?) RETURNING id", Long.class,
            PROJECT_CLASS);
    }

    Long insertProject(String name, long countImages, long countAnnotations, String mode, LocalDateTime created,
        Timestamp updated) {
        return jdbcTemplate.queryForObject("""
            INSERT INTO project (version, are_images_downloadable, blind_mode, count_annotations, count_images,
                                 count_job_annotations, count_reviewed_annotations, is_closed, mode, name,
                                 hide_users_layers, hide_admins_layers, created, updated)
            VALUES (0, false, false, ?, ?, 0, 0, false, ?, ?, false, false, ?, ?)
            RETURNING id
            """, Long.class, countAnnotations, countImages, mode, name, Timestamp.valueOf(created), updated);
    }

    /**
     * Inserts a project plus one ACL entry / representative membership for the given seeded username.
     * A granting mask of 16 makes the user admin of the project.
     */
    Long insertProjectWithMembership(String name, long countImages, long countAnnotations, String mode,
        LocalDateTime created, String username, boolean adminMask, boolean representative) {
        Long projectId = insertProject(name, countImages, countAnnotations, mode, created, null);
        Long aclClassId = aclClassId();
        Long aoiId = jdbcTemplate.queryForObject("""
            INSERT INTO acl_object_identity (id, object_id_class, entries_inheriting, object_id_identity)
            VALUES (nextval('hibernate_sequence'), ?, false, ?)
            RETURNING id
            """, Long.class, aclClassId, projectId);
        Long aclSidId =
            jdbcTemplate.queryForObject("SELECT id FROM acl_sid WHERE sid = ? AND principal = true", Long.class,
                username);
        Long memberUserId =
            jdbcTemplate.queryForObject("SELECT id FROM sec_user WHERE username = ?", Long.class, username);
        jdbcTemplate.execute("""
            INSERT INTO acl_entry (id, ace_order, acl_object_identity, audit_failure, audit_success, granting, mask,
                                   sid)
            VALUES (nextval('hibernate_sequence'), 0, %d, false, false, true, %d, %d)
            """.formatted(aoiId, adminMask ? 16 : 4, aclSidId));
        if (representative) {
            jdbcTemplate.execute("""
                INSERT INTO project_representative_user (id, version, project_id, user_id)
                VALUES (nextval('hibernate_sequence'), 0, %d, %d)
                """.formatted(projectId, memberUserId));
        }
        return projectId;
    }

    Long insertUser(String username) {
        Long userId = jdbcTemplate.queryForObject("""
            INSERT INTO sec_user (id, version, account_expired, account_locked, enabled, password, password_expired,
                                  username, class)
            VALUES (nextval('hibernate_sequence'), 0, false, false, true, 'password', false, ?,
                    'be.cytomine.domain.user.User')
            RETURNING id
            """, Long.class, username);
        jdbcTemplate.execute(
            "INSERT INTO acl_sid (id, principal, sid) VALUES (%d, true, '%s')".formatted(userId, username));
        return userId;
    }

    Long insertAdminUser(String username) {
        Long userId = insertUser(username);
        jdbcTemplate.update(
            "INSERT INTO sec_role (version, authority, created) SELECT 0, 'ROLE_SUPER_ADMIN', NOW() "
                + "WHERE NOT EXISTS (SELECT 1 FROM sec_role WHERE authority = 'ROLE_SUPER_ADMIN')");
        jdbcTemplate.update(
            "INSERT INTO sec_user_sec_role (version, sec_user_id, created, sec_role_id) SELECT 0, ?, NOW(), "
                + "(SELECT id FROM sec_role WHERE authority = 'ROLE_SUPER_ADMIN')", userId);
        return userId;
    }

    private Page<ProjectListRow> findAllUnfiltered(long userId, Pageable pageable) {
        return projectRepository.findAllReadableByUser(userId, pageable, Set.of(), Set.of(), Set.of(), null,
            null, null, null, null, null, null, null, null);
    }

    void insertCommandHistory(Long projectId, Long userId, LocalDateTime created) {
        Long commandId = jdbcTemplate.queryForObject("""
            INSERT INTO command (id, version, action_message, data, print_message, refuse_undo,
                                 save_on_undo_redo_stack, user_id, class)
            VALUES (nextval('hibernate_sequence'), 0, 'CREATE', '{}', false, false, false, ?, 'AddProjectCommand')
            RETURNING id
            """, Long.class, userId);
        jdbcTemplate.update("""
            INSERT INTO command_history (id, version, command_id, message, prefix_action, project_id, user_id, created)
            VALUES (nextval('hibernate_sequence'), 0, %d, 'test', 'CREATE', %d, %d, ?)
            """.formatted(commandId, projectId, userId), Timestamp.valueOf(created));
    }

    Long lastActivity(Long projectId) {
        return jdbcTemplate.queryForObject("""
            SELECT (extract(epoch from max(ch.created)) * 1000)::bigint
              FROM command_history ch
             WHERE ch.project_id = ?
               AND ch.deleted IS NULL
            """, Long.class, projectId);
    }

    @Test
    void mapsRowWithoutSort() {
        Long userId = insertAdminUser("test-user1");
        LocalDateTime created = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        Timestamp updated = Timestamp.valueOf(created);
        Long id =
            insertProject("test1", 4, 3, "READ_ONLY", created, updated);
        insertCommandHistory(id, userId, created.plusHours(1));

        Page<ProjectListRow> page = findAllUnfiltered(userId, PageRequest.of(0, 10));

        assertEquals(List.of(new ProjectListRow(id, "test1", null, null, false, false, 0, 4, 3, 0, false, true,
            false, false, false, created, updated.toLocalDateTime(), 0, lastActivity(id))), page.getContent());
    }

    @Test
    void filteredRows() {
        Long userId = insertAdminUser("test-user1");
        LocalDateTime createdSearchable = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        Long searchableId = insertProjectWithMembership("searchable-project", 4, 3, "READ_ONLY", createdSearchable,
            "test-user1", true, false);
        LocalDateTime createdOther = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        insertProject("other-project", 1, 0, "CLASSIC", createdOther, null);
        ProjectListRow searchableRow = new ProjectListRow(searchableId, "searchable-project", null, null, false,
            false, 0, 4, 3, 0, false, true, false, false, false, createdSearchable, null, 1, null);

        Page<ProjectListRow> page = projectRepository.findAllReadableByUser(userId, PageRequest.of(0, 10), Set.of(),
            Set.of(), Set.of(), "SEARCHABLE", null, null, null, null, null, null, null, null);
        assertEquals(List.of(searchableRow), page.getContent());

        Page<ProjectListRow> adminRoleRows = projectRepository.findAllReadableByUser(userId, PageRequest.of(0, 10),
            Set.of(), Set.of("admin"), Set.of(), null, null, null, null, null, null, null, null, null);
        assertEquals(List.of(searchableRow), adminRoleRows.getContent());
    }

    @Test
    void nullNameIsIgnoredByFilters() {
        Long userId = insertAdminUser("test-user1");
        LocalDateTime createdSearchable = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        Long searchableId = insertProjectWithMembership("searchable-project", 4, 3, "READ_ONLY", createdSearchable,
            "test-user1", true, false);
        LocalDateTime createdOther = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        Long otherId = insertProject("other-project", 1, 0, "CLASSIC", createdOther, null);

        Page<ProjectListRow> page = projectRepository.findAllReadableByUser(userId, PageRequest.of(0, 10), Set.of(),
            Set.of(), Set.of(), null, null, null, null, null, null, null, null, null);
        assertEquals(List.of(
            new ProjectListRow(searchableId, "searchable-project", null, null, false, false, 0, 4, 3, 0, false,
                true, false, false, false, createdSearchable, null, 1, null),
            new ProjectListRow(otherId, "other-project", null, null, false, false, 0, 1, 0, 0, false, false,
                false, false, false, createdOther, null, 0, null)), page.getContent());
    }

    @Test
    void mapsRowWithNullables() {
        Long userId = insertAdminUser("test-admin-sup");
        LocalDateTime created = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        Long id = insertProject("test2", 0, 0, "CLASSIC", created, null);

        Page<ProjectListRow> page = findAllUnfiltered(userId, PageRequest.of(0, 10));

        assertEquals(List.of(new ProjectListRow(id, "test2", null, null, false, false, 0, 0, 0, 0, false, false,
            false, false, false, created, null, 0, null)), page.getContent());
    }

    @Test
    void computesMembershipFields() {
        Long userId = insertAdminUser("test-admin");
        LocalDateTime created3 = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        Long test3Id =
            insertProjectWithMembership("test3", 1, 2, "CLASSIC", created3, "test-admin", true, true);
        Page<ProjectListRow> page = findAllUnfiltered(userId, PageRequest.of(0, 10));
        assertEquals(List.of(
            new ProjectListRow(test3Id, "test3", null, null, false, false, 0, 1, 2, 0, false, false, false, false,
                false, created3, null, 1, null)), page.getContent());
    }

    @Test
    void sortsOnRawColumnsAndAliases() {
        Long userId = insertAdminUser("test-user2");
        LocalDateTime createdA = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS).minusHours(1);
        Long testAId =
            insertProjectWithMembership("test-a", 4, 3, "CLASSIC", createdA, "test-user2", true, true);
        LocalDateTime createdB = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        Long testBId = insertProject("test-b", 9, 1, "CLASSIC", createdB, null);
        insertCommandHistory(testAId, userId, LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));

        ProjectListRow rowA = new ProjectListRow(testAId, "test-a", null, null, false, false, 0, 4, 3, 0, false,
            false, false, false, false, createdA, null, 1, lastActivity(testAId));
        ProjectListRow rowB = new ProjectListRow(testBId, "test-b", null, null, false, false, 0, 9, 1, 0, false,
            false, false, false, false, createdB, null, 0, null);

        Page<ProjectListRow> descImages =
            findAllUnfiltered(userId, PageRequest.of(0, 10, Sort.by(DESC, "count_images")));
        assertEquals(List.of(rowB, rowA), descImages.getContent());

        Page<ProjectListRow> ascAnnotations =
            findAllUnfiltered(userId, PageRequest.of(0, 10, Sort.by(ASC, "count_annotations")));
        assertEquals(List.of(rowB, rowA), ascAnnotations.getContent());

        Page<ProjectListRow> ascCreated = findAllUnfiltered(userId, PageRequest.of(0, 10, Sort.by(ASC, "created")));
        assertEquals(List.of(rowA, rowB), ascCreated.getContent());

        Page<ProjectListRow> ascMembers =
            findAllUnfiltered(userId, PageRequest.of(0, 10, Sort.by(ASC, "members_count")));
        assertEquals(List.of(rowB, rowA), ascMembers.getContent());

        Page<ProjectListRow> descActivity =
            findAllUnfiltered(userId, PageRequest.of(0, 10, Sort.by(DESC, "last_activity")));
        assertEquals(List.of(rowB, rowA), descActivity.getContent());
    }
}
