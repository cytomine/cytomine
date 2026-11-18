package be.cytomine.repository.security;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.security.ConnectAsEvent;

@Repository
public interface ConnectAsEventRepository extends JpaRepository<ConnectAsEvent, Long> {

    List<ConnectAsEvent> findByActingAdminIdAndEndedAtIsNull(Long actingAdminId);

    Optional<ConnectAsEvent> findFirstByActingAdminIdAndTargetUserIdAndEndedAtIsNullOrderByStartedAtDesc(
        Long actingAdminId, Long targetUserId);

    Optional<ConnectAsEvent> findFirstByTargetUserIdAndEndedAtIsNullOrderByStartedAtDesc(Long targetUserId);
}
