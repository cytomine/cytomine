package org.cytomine.repository.persistence.entity;

import java.sql.Timestamp;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;

import be.cytomine.common.repository.model.HasTimestampCUD;

@Entity(name = "ontology")
@Data
public class OntologyEntity implements HasTimestampCUD {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private long version = 0;
    @Column
    private String name;

    @Column
    private Long userId;

    @Column
    private Timestamp created;
    @Column
    private Timestamp updated;
    @Column
    private Timestamp deleted;

    @OneToMany(mappedBy = "ontologyId")
    @SQLRestriction("deleted IS NULL")
    private Set<TermEntity> terms;
}
