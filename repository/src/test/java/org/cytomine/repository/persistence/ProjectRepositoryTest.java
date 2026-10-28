package org.cytomine.repository.persistence;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

import static org.assertj.core.api.Assertions.assertThat;
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
        LocalDateTime created, Timestamp updated, String username, boolean adminMask, boolean representative) {
        Long projectId = insertProject(name, countImages, countAnnotations, mode, created, updated);
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

    void insertCommandHistory(Long projectId, Long userId) {
        Long commandId = jdbcTemplate.queryForObject("""
            INSERT INTO command (id, version, action_message, data, print_message, refuse_undo,
                                 save_on_undo_redo_stack, user_id, class)
            VALUES (nextval('hibernate_sequence'), 0, 'CREATE', '{}', false, false, false, ?, 'AddProjectCommand')
            RETURNING id
            """, Long.class, userId);
        jdbcTemplate.execute("""
            INSERT INTO command_history (id, version, command_id, message, prefix_action, project_id, user_id, created)
            VALUES (nextval('hibernate_sequence'), 0, %d, 'test', 'CREATE', %d, %d, now())
            """.formatted(commandId, projectId, userId));
    }

    @Test
    void mapsRowWithoutSort() {
        Long userId = insertAdminUser("test-user1");
        Long id =
            insertProject("test1", 4, 3, "READ_ONLY", LocalDateTime.now(), Timestamp.valueOf(LocalDateTime.now()));
        insertCommandHistory(id, userId);

        Page<ProjectListRow> page = findAllUnfiltered(userId, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        ProjectListRow row = page.getContent().get(0);
        assertThat(row.id()).isEqualTo(id);
        assertThat(row.name()).isEqualTo("test1");
        assertThat(row.ontology()).isNull();
        assertThat(row.ontologyName()).isNull();
        assertThat(row.blindMode()).isFalse();
        assertThat(row.areImagesDownloadable()).isFalse();
        assertThat(row.numberOfSlides()).isZero();
        assertThat(row.numberOfImages()).isEqualTo(4);
        assertThat(row.numberOfAnnotations()).isEqualTo(3);
        assertThat(row.numberOfJobAnnotations()).isZero();
        assertThat(row.numberOfReviewedAnnotations()).isZero();
        assertThat(row.isClosed()).isFalse();
        assertThat(row.isReadOnly()).isTrue();
        assertThat(row.isRestricted()).isFalse();
        assertThat(row.hideUsersLayers()).isFalse();
        assertThat(row.hideAdminsLayers()).isFalse();
        assertThat(row.created()).isNotNull();
        assertThat(row.updated()).isNotNull();
        assertThat(row.membersCount()).isZero();
        assertThat(row.currentUserRoles()).isEmpty();
        assertThat(row.lastActivity()).isGreaterThan(1_000_000_000_000L);
    }

    @Test
    void filteredRows() {
        Long userId = insertAdminUser("test-user1");
        insertProjectWithMembership("searchable-project", 4, 3, "READ_ONLY", LocalDateTime.now(), null, "test-user1",
            true, false);
        insertProject("other-project", 1, 0, "CLASSIC", LocalDateTime.now(), null);

        Page<ProjectListRow> page = projectRepository.findAllReadableByUser(userId, PageRequest.of(0, 10), Set.of(),
            Set.of(), Set.of(), "SEARCHABLE", null, null, null, null, null, null, null, null);
        assertThat(page.getContent()).extracting(ProjectListRow::name).containsExactly("searchable-project");

        Page<ProjectListRow> adminRoleRows = projectRepository.findAllReadableByUser(userId, PageRequest.of(0, 10),
            Set.of(), Set.of("admin"), Set.of(), null, null, null, null, null, null, null, null, null);
        assertThat(adminRoleRows.getContent()).extracting(ProjectListRow::name).containsExactly("searchable-project");
    }

    @Test
    void nullNameIsIgnoredByFilters() {
        Long userId = insertAdminUser("test-user1");
        insertProjectWithMembership("searchable-project", 4, 3, "READ_ONLY", LocalDateTime.now(), null, "test-user1",
            true, false);
        insertProject("other-project", 1, 0, "CLASSIC", LocalDateTime.now(), null);

        Page<ProjectListRow> page = projectRepository.findAllReadableByUser(userId, PageRequest.of(0, 10), Set.of(),
            Set.of(), Set.of(), null, null, null, null, null, null, null, null, null);
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void mapsRowWithNullables() {
        Long userId = insertAdminUser("test-admin-sup");
        insertProject("test2", 0, 0, "CLASSIC", LocalDateTime.now(), null);

        Page<ProjectListRow> page = findAllUnfiltered(userId, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        ProjectListRow row = page.getContent().get(0);
        assertThat(row.updated()).isNull();
        assertThat(row.lastActivity()).isNull();
        assertThat(row.isReadOnly()).isFalse();
        assertThat(row.currentUserRoles()).isEmpty();
    }

    @Test
    void computesMembershipFields() {
        Long userId = insertAdminUser("test-admin");
        Long anotherId = insertUser("test-another");
        insertProjectWithMembership("test3", 1, 2, "CLASSIC", LocalDateTime.now(), null, "test-admin", true, true);
        insertProjectWithMembership("test4", 1, 2, "CLASSIC", LocalDateTime.now(), null, "test-another", false, false);

        Page<ProjectListRow> page = findAllUnfiltered(userId, PageRequest.of(0, 10));

        Map<String, ProjectListRow> rows =
            page.getContent().stream().collect(Collectors.toMap(ProjectListRow::name, r -> r));
        assertThat(rows).containsKeys("test3", "test4");
        assertThat(rows.get("test3").membersCount()).isEqualTo(1);
        assertThat(rows.get("test4").membersCount()).isEqualTo(1);
    }

    @Test
    void sortsOnRawColumnsAndAliases() {
        Long userId = insertAdminUser("test-user2");
        insertProjectWithMembership("test-a", 4, 3, "CLASSIC", LocalDateTime.now().minusHours(1), null, "test-user2",
            true, true);
        insertProject("test-b", 9, 1, "CLASSIC", LocalDateTime.now(), null);
        // command history only for test-a
        Long testAId = jdbcTemplate.queryForObject("SELECT id FROM project WHERE name = 'test-a'", Long.class);
        insertCommandHistory(testAId, userId);

        Page<ProjectListRow> descImages =
            findAllUnfiltered(userId, PageRequest.of(0, 10, Sort.by(DESC, "count_images")));
        assertThat(descImages.getContent()).extracting(ProjectListRow::name).containsExactly("test-b", "test-a");

        Page<ProjectListRow> ascAnnotations =
            findAllUnfiltered(userId, PageRequest.of(0, 10, Sort.by(ASC, "count_annotations")));
        assertThat(ascAnnotations.getContent()).extracting(ProjectListRow::name).containsExactly("test-b", "test-a");

        Page<ProjectListRow> ascCreated = findAllUnfiltered(userId, PageRequest.of(0, 10, Sort.by(ASC, "created")));
        assertThat(ascCreated.getContent()).extracting(ProjectListRow::name).containsExactly("test-a", "test-b");

        Page<ProjectListRow> ascMembers =
            findAllUnfiltered(userId, PageRequest.of(0, 10, Sort.by(ASC, "members_count")));
        assertThat(ascMembers.getContent()).extracting(ProjectListRow::name).containsExactly("test-b", "test-a");

        Page<ProjectListRow> descActivity =
            findAllUnfiltered(userId, PageRequest.of(0, 10, Sort.by(DESC, "last_activity")));
        assertThat(descActivity.getContent().get(0).lastActivity()).isNull();
        assertThat(descActivity.getContent().get(1).name()).isEqualTo("test-a");
        assertThat(descActivity.getContent().get(1).lastActivity()).isGreaterThan(1_000_000_000_000L);
    }
}
