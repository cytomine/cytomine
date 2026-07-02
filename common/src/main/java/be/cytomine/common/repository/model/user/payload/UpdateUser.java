package be.cytomine.common.repository.model.user.payload;

import javax.swing.text.html.Option;
import java.util.Optional;

public record UpdateUser(Optional<String> name) {
}
