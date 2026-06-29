package org.cytomine.repository.mapper;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BaseMapper {

    default <T> Optional<T> map(T t) {
        return Optional.ofNullable(t);
    }

    default Timestamp map(Instant value) {
        return Timestamp.from(value);
    }

    default <T> T map(Optional<T> t) {
        return t.orElse(null);
    }

    default Instant mapTimestamp(Timestamp value) {
        return value.toInstant();
    }

    default Optional<Instant> mapToInstant(Timestamp value) {
        return Optional.ofNullable(value).map(Timestamp::toInstant);
    }
}
