package org.cytomine.repository.persistence.entity;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Entity(name = "term")
@Data
public class TermEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private Long version;
    @Column
    private Long ontologyId;
    @Column
    private String name;
    @Column
    private String color;
    @OneToMany
    @JoinTable(name = "relation_term", joinColumns = @JoinColumn(name = "term1_id"),
        inverseJoinColumns = @JoinColumn(name = "term2_id"))
    private Set<TermEntity> children;
}
