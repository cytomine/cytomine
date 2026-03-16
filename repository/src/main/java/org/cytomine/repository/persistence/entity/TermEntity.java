package org.cytomine.repository.persistence.entity;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
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
    @Column
    private String color;
    @OneToMany
    @JoinTable(name = "relation_term", joinColumns = @JoinColumn(name = "term1_id"), inverseJoinColumns = @JoinColumn(name = "term2_id"))
    private Set<TermEntity> children;
}
