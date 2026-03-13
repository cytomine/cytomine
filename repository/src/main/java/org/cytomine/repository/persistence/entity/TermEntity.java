package org.cytomine.repository.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity(name = "term")
@Getter
public class TermEntity {
    @Id
    private long id;
    @Column
    private long version;
    @Column
    private long ontologyId;
    @Column
    private String name;
}
