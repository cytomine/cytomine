package be.cytomine.common.repository.utils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BaseMapper {

    default <T> Optional<T> map(T t) {
        return Optional.ofNullable(t);
    }

    default Timestamp map(LocalDateTime value) {
        return Timestamp.valueOf(value);
    }

    default <T> T map(Optional<T> t) {
        return t.orElse(null);
    }

    default LocalDateTime mapTimestamp(Timestamp value) {
        return value.toInstant().atZone(ZoneOffset.systemDefault()).toLocalDateTime();
    }

    default Optional<LocalDateTime> mapToLocalDateTime(Timestamp value) {
        return Optional.ofNullable(value).map(this::mapTimestamp);
    }

    default Optional<LocalDateTime> map(Date maybeDate) {
        return Optional.ofNullable(maybeDate)
            .map(date -> LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
    }
}
