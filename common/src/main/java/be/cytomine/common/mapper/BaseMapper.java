package be.cytomine.common.mapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.mapstruct.Mapper;

/**
 * It's tempting to add a
 *  `T map(Optional<T> value)`
 * here, but then ObjectMapper does a lot of obscure chained calls.
 * Be careful.
 */
@Mapper(componentModel = "spring")
public interface BaseMapper {

    default <T> Optional<T> map(T t) {
        return Optional.ofNullable(t);
    }

    default Timestamp map(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return Timestamp.valueOf(value);
    }

    default Optional<LocalDateTime> map(Date maybeDate) {
        return Optional.ofNullable(maybeDate)
            .map(date -> LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
    }

    default Date mapMaybeDate(Optional<LocalDateTime> maybeDate) {
        return maybeDate.map(d -> Date.from(d.atZone(ZoneId.systemDefault()).toInstant())).orElse(null);
    }

    default LocalDateTime mapTimestamp(Timestamp value) {
        return value.toInstant().atZone(ZoneOffset.systemDefault()).toLocalDateTime();
    }

    default String mapMaybeString(Optional<String> value){
        return value.orElse(null);
    }

    default UUID mapMaybeUUID(Optional<UUID> value){
        return value.orElse(null);
    }

    default Optional<LocalDateTime> mapToLocalDateTime(Timestamp value) {
        return Optional.ofNullable(value).map(this::mapTimestamp);
    }
}
