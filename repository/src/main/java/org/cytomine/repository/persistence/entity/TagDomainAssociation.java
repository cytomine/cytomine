package org.cytomine.repository.persistence.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import be.cytomine.common.repository.model.HasTimestampCUD;

@Entity(name = "tag_domain_association")
@Data
public class TagDomainAssociation implements HasTimestampCUD {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    @Version
    private long version = 0;

    @Column(name = "tag_id")
    private Long tagId;
    @Column
    @NotBlank
    private String domainClassName;
    @Column
    @NotNull
    private Long domainIdent;

    @Column
    private Timestamp created;
    @Column
    private Timestamp updated;
    @Column
    private Timestamp deleted;
}
