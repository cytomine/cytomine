package org.cytomine.repository.persistence.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cytomine.repository.persistence.entity.key.UserRoleEntityKey;

import be.cytomine.common.repository.model.HasTimestampCUD;

@Entity(name = "sec_user_sec_role")
@IdClass(UserRoleEntityKey.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleEntity implements HasTimestampCUD {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private long version;
    @Column(name = "sec_role_id")
    private long secRoleId;
    @Column(name = "sec_user_id")
    private long secUserId;
    @Column
    private Timestamp created;
    @Column
    private Timestamp updated;
    @Column
    private Timestamp deleted;
}
