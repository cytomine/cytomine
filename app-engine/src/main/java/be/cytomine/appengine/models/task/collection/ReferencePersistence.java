package be.cytomine.appengine.models.task.collection;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import be.cytomine.appengine.models.task.TypePersistence;

@Entity
@Table(name = "reference_type_persistence")
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ReferencePersistence extends TypePersistence {
    private String value;
}
