package org.cytomine.repository.persistence;

import java.util.Optional;

import org.cytomine.repository.persistence.entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    Optional<ProjectEntity> findByIdAndDeletedNull(long id);

    @Query(
        value = "SELECT p.* FROM project p WHERE p.deleted IS NULL",
        countQuery = "SELECT count(p.id) FROM project p WHERE p.deleted IS NULL",
        nativeQuery = true
    )
    Page<ProjectEntity> findAllByDeletedNull(Pageable pageable);

    @Query(
        value = """
            SELECT DISTINCT p.* FROM project p
            JOIN acl_object_identity aoi ON aoi.object_id_identity = p.id
            JOIN acl_class ac ON ac.id = aoi.object_id_class AND ac.class = 'be.cytomine.domain.project.Project'
            JOIN acl_entry ae ON ae.acl_object_identity = aoi.id
            JOIN acl_sid sid ON sid.id = ae.sid
            JOIN sec_user u ON u.username = sid.sid
            WHERE p.deleted IS NULL AND u.id = :userId
            """,
        countQuery = """
            SELECT count(DISTINCT p.id) FROM project p
            JOIN acl_object_identity aoi ON aoi.object_id_identity = p.id
            JOIN acl_class ac ON ac.id = aoi.object_id_class AND ac.class = 'be.cytomine.domain.project.Project'
            JOIN acl_entry ae ON ae.acl_object_identity = aoi.id
            JOIN acl_sid sid ON sid.id = ae.sid
            JOIN sec_user u ON u.username = sid.sid
            WHERE p.deleted IS NULL AND u.id = :userId
            """,
        nativeQuery = true
    )
    Page<ProjectEntity> findAllReadableByUser(long userId, Pageable pageable);
}
