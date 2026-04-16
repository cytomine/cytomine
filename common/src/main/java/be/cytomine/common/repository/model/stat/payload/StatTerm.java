package be.cytomine.common.repository.model.stat.payload;

public record StatTerm(long id, String key,
                       // TODO rename value to count
                       long value, String color) {
}
