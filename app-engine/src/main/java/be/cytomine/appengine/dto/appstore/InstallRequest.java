package be.cytomine.appengine.dto.appstore;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InstallRequest {
    private String appStorescheme;
    private String appStoreHost;
    private String appNamespace;
    private String appVersion;
}
