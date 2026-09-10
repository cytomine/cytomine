package org.cytomine.repository.persistence.entity;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Version;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import be.cytomine.common.repository.model.HasTimestampCUD;
import be.cytomine.common.repository.utils.Language;

@Entity(name = "sec_user")
@Data
@DynamicInsert // if fields are null here, they will fallback to default in postgres (useful for public/private keys)
public class UserEntity implements HasTimestampCUD {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @Version
    private long version = 0;

    @Column(unique = true)
    private String username;

    @Column
    private String firstname;

    @Column
    private String lastname;

    @Column
    private String name;

    @Column
    @Enumerated(EnumType.STRING)
    private Language language;

    @Column
    private String email;

    @Column
    private boolean isDeveloper;

    @Column
    // we should make this column a `NOT NULL DEFAULT true`.
    // `enabled = null` has no business meaning.
    private Boolean enabled = true;

    @Column
    private String origin;

    @Column
    private String privateKey;

    @Column
    private String publicKey;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "sec_user_sec_role",
        joinColumns = @JoinColumn(name = "sec_user_id"),
        inverseJoinColumns = @JoinColumn(name = "sec_role_id")
    )
    private Set<RoleEntity> roles = new HashSet<>();

    @Column
    private Timestamp created;
    @Column
    private Timestamp updated;
    @Column
    private Timestamp deleted;
}
