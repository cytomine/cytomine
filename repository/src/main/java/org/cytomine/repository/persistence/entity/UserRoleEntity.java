package org.cytomine.repository.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cytomine.repository.persistence.entity.key.UserRoleEntityKey;

@Entity(name = "sec_user_sec_role")
@IdClass(UserRoleEntityKey.class)
@Data
@NoArgsConstructor
public class UserRoleEntity {
    @Id
    @Column(name = "sec_user_id")
    private long secUserId;

    @Id
    @Column(name = "sec_role_id")
    private long secRoleId;
}
