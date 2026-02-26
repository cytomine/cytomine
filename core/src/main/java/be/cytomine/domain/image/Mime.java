package be.cytomine.domain.image;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.utils.JsonObject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class Mime extends CytomineDomain {
    @Id
    @GeneratedValue
    Long id;

    @Size(min = 1, max = 5)
    private String extension;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String mimeType;

    @Override
    public String toJSON() {
        return null;
    }

    @Override
    public JsonObject toJsonObject() {
        return null;
    }
}
