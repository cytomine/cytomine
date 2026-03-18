package org.cytomine.repository.persistence.entity;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;

@Entity(name = "ontology")
@Getter
public class OntologyEntity {
    @Id
    private long id;

    @Column
    private String name;

    @Column
    private long userId;

    @Column
    private Long projectId;

    @OneToMany(mappedBy = "ontologyId")
    private Set<TermEntity> terms;
}
