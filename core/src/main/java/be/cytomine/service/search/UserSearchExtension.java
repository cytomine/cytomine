package be.cytomine.service.search;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserSearchExtension {
    private boolean withLastImage;

    private boolean withLastConnection;

    private boolean withNumberConnections;

    public boolean noExtension() {
        return !withLastImage && !withLastConnection && !withNumberConnections;
    }
}
