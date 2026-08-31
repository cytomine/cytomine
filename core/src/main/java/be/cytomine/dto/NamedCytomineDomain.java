package be.cytomine.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class NamedCytomineDomain {
    private Long id;
    private String name;

    public NamedCytomineDomain() {
    }

    public NamedCytomineDomain(Long id) {
        this.id = id;
    }

    public NamedCytomineDomain(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
