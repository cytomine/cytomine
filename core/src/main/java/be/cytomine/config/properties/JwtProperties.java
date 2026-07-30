package be.cytomine.config.properties;

import lombok.Data;

@Data
public class JwtProperties {

    String secret;

    Long tokenValidityInSeconds;

    Long tokenValidityInSecondsForRememberMe;

    Long tokenValidityInSecondsForShortTerm;
}
