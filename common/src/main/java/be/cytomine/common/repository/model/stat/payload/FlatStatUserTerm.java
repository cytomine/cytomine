package be.cytomine.common.repository.model.stat.payload;

public record FlatStatUserTerm(long userId, String username, StatTerm term) {
}
