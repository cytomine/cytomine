package org.cytomine.repository.persistence.entity;

import java.sql.Timestamp;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import be.cytomine.common.repository.model.HasDeleted;
import be.cytomine.common.repository.model.HasUpdated;

@Entity(name = "ontology")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OntologyEntity implements HasDeleted, HasUpdated {
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
    private Set<TermEntity> terms;
}
