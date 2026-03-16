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
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "term")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TermEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    @Version
    private long version = 0;
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
