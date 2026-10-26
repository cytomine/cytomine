package org.cytomine.repository.persistence;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.cytomine.repository.persistence.entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import be.cytomine.common.repository.model.list.filtered.BoundedFilter;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    String PROJECT_CLASS = "'be.cytomine.domain.project.Project'";

    String MEMBERS_COUNT_SUBQUERY = """
        (SELECT count(DISTINCT su.id)
           FROM acl_entry ae
           JOIN acl_object_identity aoi ON aoi.id = ae.acl_object_identity
           JOIN acl_sid asid ON asid.id = ae.sid
           JOIN sec_user su ON su.username = asid.sid
           WHERE aoi.object_id_identity = p.id
             AND aoi.object_id_class =
                 (SELECT ac.id FROM acl_class ac
                   WHERE ac.class = 'be.cytomine.domain.project.Project')
        )""";

    String IS_ADMIN_EXISTS = """
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
               )""";

    String READABLE_EXISTS = """
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
                   AND ae.mask >= 1
               )""";

    String PLATFORM_ADMIN_EXISTS = """
        EXISTS (SELECT 1
                  FROM sec_user_sec_role usr
                  JOIN sec_role sr ON sr.id = usr.sec_role_id
                 WHERE usr.sec_user_id = :userId
                   AND sr.authority IN ('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')
               )""";

    String FILTERED_CONDITIONS = """
        AND (:ontologyNames IS NULL OR o.name IN (:ontologyNames))
        AND (:roleNames IS NULL OR CASE WHEN (""" + IS_ADMIN_EXISTS + """
             ) THEN 'admin' ELSE 'contributor' END IN (:roleNames))
        AND (:tagLabels IS NULL OR EXISTS (SELECT 1
                                              FROM tag_domain_association tda
                                              JOIN tag t ON t.id = tda.tag_id AND t.deleted IS NULL
                                             WHERE tda.domain_id = p.id
                                               AND tda.domain_class_name = 'be.cytomine.domain.project.Project'
                                               AND tda.deleted IS NULL
                                               AND t.name IN (:tagLabels)))
        AND (:minMemberCount IS NULL OR """ + MEMBERS_COUNT_SUBQUERY + """
              >= :minMemberCount)
        AND (:maxMemberCount IS NULL OR """ + MEMBERS_COUNT_SUBQUERY + """
              <= :maxMemberCount)
        AND (:minImageCount IS NULL OR p.count_images >= :minImageCount)
        AND (:maxImageCount IS NULL OR p.count_images <= :maxImageCount)
        AND (:minManualAnnotationCount IS NULL OR p.count_annotations >= :minManualAnnotationCount)
        AND (:maxManualAnnotationCount IS NULL OR p.count_annotations <= :maxManualAnnotationCount)
        AND (:minReviewedAnnotationCount IS NULL OR p.count_reviewed_annotations >= :minReviewedAnnotationCount)
        AND (:maxReviewedAnnotationCount IS NULL OR p.count_reviewed_annotations <= :maxReviewedAnnotationCount)
        """;

    String FILTERED_WHERE = """
        WHERE p.deleted IS NULL
        AND (""" + PLATFORM_ADMIN_EXISTS + """
        OR\s""" + READABLE_EXISTS + """
        )
        """ + FILTERED_CONDITIONS;

    String ACL_MEMBER_JOIN = """
            JOIN acl_entry ae
            JOIN acl_object_identity aoi ON aoi.id = ae.acl_object_identity
            JOIN acl_class ac ON ac.id = aoi.object_id_class AND ac.class = 'be.cytomine.domain.project.Project'
            JOIN acl_sid asid ON asid.id = ae.sid
            JOIN sec_user su ON su.username = asid.sid
        """;

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

    Optional<ProjectEntity> findByIdAndDeletedNull(long id);

    default ProjectListAvailableFilters getAvailableFilters() {
        return new ProjectListAvailableFilters(Set.copyOf(findOntologyNames()), Set.copyOf(findRoleNames()),
            Set.copyOf(findTagNames()), findMemberCountBounds(), findImageCountBounds(),
            findManualAnnotationCountBounds(), findReviewedAnnotationCountBounds());
    }

    @Query(value = """
        SELECT DISTINCT o.name
        FROM project p
        JOIN ontology o ON o.id = p.ontology_id AND o.deleted IS NULL
        WHERE p.deleted IS NULL
        """, nativeQuery = true)
    List<String> findOntologyNames();

    @Query(value = """
        SELECT DISTINCT CASE WHEN EXISTS (
                  SELECT 1
                    FROM acl_entry ae_admin
                    JOIN acl_object_identity aoi_admin ON aoi_admin.id = ae_admin.acl_object_identity
                    JOIN acl_class ac_admin ON ac_admin.id = aoi_admin.object_id_class
                      AND ac_admin.class =""" + PROJECT_CLASS + """
                   WHERE aoi_admin.object_id_identity = p.id
                     AND asid.id = ae_admin.sid
                     AND ae_admin.granting = true
                     AND ae_admin.mask >= 16
               ) THEN 'admin' ELSE 'contributor' END
        FROM project p
        """ + ACL_MEMBER_JOIN, nativeQuery = true)
    List<String> findRoleNames();

    @Query(value = """
        SELECT DISTINCT t.name
        FROM project p
        JOIN tag_domain_association tda ON tda.domain_id = p.id
          AND tda.domain_class_name =""" + PROJECT_CLASS + """
          AND tda.deleted IS NULL
        JOIN tag t ON t.id = tda.tag_id AND t.deleted IS NULL
        WHERE p.deleted IS NULL
        """, nativeQuery = true)
    List<String> findTagNames();

    @Query(value = """
        WITH members AS (
            SELECT aoi.object_id_identity AS project_id, count(DISTINCT su.id) AS cnt
              FROM acl_entry ae
              JOIN acl_object_identity aoi ON aoi.id = ae.acl_object_identity
              JOIN acl_class ac ON ac.id = aoi.object_id_class AND ac.class =""" + PROJECT_CLASS + """
              JOIN acl_sid asid ON asid.id = ae.sid
              JOIN sec_user su ON su.username = asid.sid
             GROUP BY 1
        )
        SELECT min(m.cnt) AS min, max(m.cnt) AS max
        FROM members m
        """, nativeQuery = true)
    BoundedFilter findMemberCountBounds();

    @Query(value = """
        SELECT min(p.count_images) AS min, max(p.count_images) AS max
        FROM project p
        WHERE p.deleted IS NULL
        """, nativeQuery = true)
    BoundedFilter findImageCountBounds();

    @Query(value = """
        SELECT min(p.count_annotations) AS min, max(p.count_annotations) AS max
        FROM project p
        WHERE p.deleted IS NULL
        """, nativeQuery = true)
    BoundedFilter findManualAnnotationCountBounds();

    @Query(value = """
        SELECT min(p.count_reviewed_annotations) AS min, max(p.count_reviewed_annotations) AS max
        FROM project p
        WHERE p.deleted IS NULL
        """, nativeQuery = true)
    BoundedFilter findReviewedAnnotationCountBounds();

    @Query(value = ProjectRepository.PROJECT_ROW_PROJECTION + """
        FROM project p
        LEFT JOIN ontology o ON o.id = p.ontology_id
        """ + FILTERED_WHERE,
        countQuery = """
            SELECT count(p.id) FROM project p
            LEFT JOIN ontology o ON o.id = p.ontology_id
            """ + FILTERED_WHERE, nativeQuery = true)
    Page<ProjectListRow> findAllReadableByUser(long userId, Pageable pageable, Set<String> ontologyNames,
        Set<String> roleNames, Set<String> tagLabels, Long minMemberCount, Long maxMemberCount, Long minImageCount,
        Long maxImageCount, Long minManualAnnotationCount, Long maxManualAnnotationCount,
        Long minReviewedAnnotationCount, Long maxReviewedAnnotationCount);

}
