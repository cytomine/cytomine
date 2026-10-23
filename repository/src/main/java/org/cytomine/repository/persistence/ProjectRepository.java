package org.cytomine.repository.persistence;

import java.util.Optional;

import org.cytomine.repository.persistence.entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    Optional<ProjectEntity> findByIdAndDeletedNull(long id);

    String PROJECT_ROW_PROJECTION = """
        SELECT p.id,
               p.name,
               p.ontology_id,
               o.name,
               p.blind_mode,
               p.are_images_downloadable,
               0::bigint,
               p.count_images,
               p.count_annotations,
               p.count_job_annotations,
               p.count_reviewed_annotations,
               p.is_closed,
               (p.mode = 'READ_ONLY'),
               (p.mode = 'RESTRICTED'),
               p.hide_users_layers,
               p.hide_admins_layers,
               p.created,
               p.updated,
               (SELECT count(DISTINCT su.id)
                  FROM acl_entry ae
                  JOIN acl_object_identity aoi ON aoi.id = ae.acl_object_identity
                  JOIN acl_sid asid ON asid.id = ae.sid
                  JOIN sec_user su ON su.username = asid.sid
                  WHERE aoi.object_id_identity = p.id
                    AND aoi.object_id_class =
                        (SELECT ac.id FROM acl_class ac
                          WHERE ac.class = 'be.cytomine.domain.project.Project')
               ) AS members_count,
               EXISTS (SELECT 1
                         FROM acl_entry ae
                         JOIN acl_object_identity aoi ON aoi.id = ae.acl_object_identity
                         JOIN acl_sid asid ON asid.id = ae.sid
                         JOIN sec_user su ON su.username = asid.sid
                         WHERE aoi.object_id_identity = p.id
                           AND aoi.object_id_class =
                               (SELECT ac.id FROM acl_class ac
                                 WHERE ac.class = 'be.cytomine.domain.project.Project')
                           AND su.id = :userId
                          AND ae.granting = true
                          AND ae.mask >= 16
                      ) AS is_admin,
               EXISTS (SELECT 1
                         FROM project_representative_user pru
                        WHERE pru.project_id = p.id
                          AND pru.user_id = :userId
                          AND pru.deleted IS NULL
                      ) AS is_representative,
               (SELECT (extract(epoch from max(ch.created)) * 1000)::bigint
                  FROM command_history ch
                 WHERE ch.project_id = p.id
                   AND ch.deleted IS NULL
               ) AS last_activity
        """;

    @Query(
        value = ProjectRepository.PROJECT_ROW_PROJECTION + """
            FROM project p
            LEFT JOIN ontology o ON o.id = p.ontology_id
            WHERE p.deleted IS NULL
            """,
        countQuery = """
            SELECT count(p.id) FROM project p WHERE p.deleted IS NULL
            """,
        nativeQuery = true
    )
    Page<ProjectListRow> findAllByDeletedNull(long userId, Pageable pageable);

    @Query(
        value = ProjectRepository.PROJECT_ROW_PROJECTION + """
            FROM project p
            LEFT JOIN ontology o ON o.id = p.ontology_id
            JOIN acl_object_identity aoi ON aoi.object_id_identity = p.id
            JOIN acl_class ac ON ac.id = aoi.object_id_class AND ac.class = 'be.cytomine.domain.project.Project'
            JOIN acl_entry ae ON ae.acl_object_identity = aoi.id
            JOIN acl_sid sid ON sid.id = ae.sid
            JOIN sec_user su ON su.username = sid.sid
            WHERE p.deleted IS NULL AND su.id = :userId
            """,
        countQuery = """
            SELECT count(DISTINCT p.id) FROM project p
            JOIN acl_object_identity aoi ON aoi.object_id_identity = p.id
            JOIN acl_class ac ON ac.id = aoi.object_id_class AND ac.class = 'be.cytomine.domain.project.Project'
            JOIN acl_entry ae ON ae.acl_object_identity = aoi.id
            JOIN acl_sid sid ON sid.id = ae.sid
            JOIN sec_user su ON su.username = sid.sid
            WHERE p.deleted IS NULL AND su.id = :userId
            """,
        nativeQuery = true
    )
    Page<ProjectListRow> findAllReadableByUser(long userId, Pageable pageable);
}
