package org.cytomine.repository.persistence.entity;

import java.sql.Timestamp;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "ontology")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OntologyEntity {
    @Id
    private long id;
    @Column
    @Version
    private long version = 0;
    @Column
    private String name;

    @Column
    private long userId;

    @Column
    private Timestamp created;
    @Column
    private Timestamp updated;
    @Column
    private Timestamp deleted;

    @OneToMany(mappedBy = "ontologyId")
    private Set<TermEntity> terms;
}
