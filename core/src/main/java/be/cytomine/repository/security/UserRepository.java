package be.cytomine.repository.security;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.security.User;

/**
 * Spring Data JPA repository for the user entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    @EntityGraph(attributePaths = "roles")
    Optional<User> findById(Long id);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByUsernameLikeIgnoreCase(String username);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByUsername(String username);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByPublicKey(String publicKey);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByPublicKeyAndEnabled(String accessKey, boolean enabled);

    @Query("select distinct user " +
            "from AclObjectIdentity as aclObjectId, AclEntry as aclEntry, AclSid as aclSid, User as user "+
            "where aclObjectId.objectId = :projectId " +
            "and aclEntry.aclObjectIdentity = aclObjectId " +
            "and aclEntry.mask = 16 " +
            "and aclEntry.sid = aclSid " +
            "and aclSid.sid = user.username ")
    List<User> findAllAdminsByProjectId(Long projectId);


    default List<User> findAllUsersByProjectId(Long projectId) {
        return findAllUsersByContainer(projectId);
    }

    default List<User> findAllUsersByStorageId(Long storageId) {
        return findAllUsersByContainer(storageId);
    }

    @Query("select distinct user " +
            "from  User as user, AclObjectIdentity as aclObjectId, AclEntry as aclEntry, AclSid as aclSid "+
            "where aclObjectId.objectId = :containerId " +
            "and aclEntry.aclObjectIdentity = aclObjectId " +
            "and aclEntry.sid = aclSid " +
            "and aclSid.sid = user.username ")
    List<User> findAllUsersByContainer(Long containerId);


    @Query(value = "SELECT DISTINCT sec_user.id \n" +
            " FROM acl_object_identity, acl_entry,acl_sid, sec_user \n" +
            " WHERE acl_object_identity.object_id_identity = :domainId\n" +
            " AND acl_entry.acl_object_identity=acl_object_identity.id\n" +
            " AND acl_entry.sid = acl_sid.id " +
            " AND acl_sid.sid = sec_user.username", nativeQuery = true)
    List<Long> findAllAllowedUserIdList(Long domainId);

    List<User> findAllByIdIn(List<Long> ids);

    @Query(value = "select distinct user " +
            "from AclSid as aclSid, AclEntry as aclEntry, User as user "+
            "where aclEntry.aclObjectIdentity in (select  aclEntry.aclObjectIdentity from AclEntry as aclEntry where aclEntry.sid.id = :sidId) " +
            "and aclEntry.sid = aclSid and aclSid.sid = user.username and aclSid.id <> :sidId")
    List<User> findAllUsersSharingAccessToSameProject(Long sidId);

    @Query(value = "SELECT id FROM acl_sid WHERE sid = :username", nativeQuery = true)
    Long getAclSidFromUsername(String username);

    default List<User> findAllUsersSharingAccessToSameProject(String username) {
        Long aclId = getAclSidFromUsername(username);
        return findAllUsersSharingAccessToSameProject(aclId);
    }
    Optional<User> findByReference(String sub);

}
