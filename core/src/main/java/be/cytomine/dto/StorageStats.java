package be.cytomine.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorageStats {
    Long used;

    Long available;

    Double usedP;

    String hostname;

    String mount;

    String ip;
}
