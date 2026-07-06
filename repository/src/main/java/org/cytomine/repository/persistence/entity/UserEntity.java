package org.cytomine.repository.persistence.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.Data;

import be.cytomine.common.repository.model.HasTimestampCUD;

@Entity(name = "sec_user")
@Data
public class UserEntity implements HasTimestampCUD {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @Version
    private long version = 0;

    @Column
    private String username;

    @Column
    private String name;

    @Column
    private String reference;

    @Column
    private String firstname;

    @Column
    private String lastname;

    @Column
    private String email;

    @Column
    private Integer language;

    @Column
    private boolean isDeveloper;

    @Column
    private String password;

    @Column
    private boolean enabled;

    @Column
    private String origin;

    @Column
    private Timestamp created;
    @Column
    private Timestamp updated;
    @Column
    private Timestamp deleted;
}
