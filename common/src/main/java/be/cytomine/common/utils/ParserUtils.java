package be.cytomine.common.utils;

import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public class ParserUtils {
    public Optional<Long> parseLong(String maybeLong) {
        try {
            return Optional.of(Long.parseLong(maybeLong));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
