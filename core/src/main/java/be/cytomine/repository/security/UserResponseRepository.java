package be.cytomine.repository.security;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import be.cytomine.common.repository.model.command.payload.response.UserResponse;

@Repository
public interface UserResponseRepository
    extends JpaRepository<UserResponse, Long>, JpaSpecificationExecutor<UserResponse> {
    Optional<UserResponse> findByUsernameLikeIgnoreCase(String username);
}
