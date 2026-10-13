package org.cytomine.keycloak;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.cytomine.keycloak.config.CytominePermissions;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserInfoTokenMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

import be.cytomine.common.config.security.CytominePermission;

import static com.fasterxml.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS;
import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE;


public class CytomineProtocolMapper extends AbstractOIDCProtocolMapper
    implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    public static final ObjectMapper objectMapper =
        YAMLMapper.builder().enable(ACCEPT_CASE_INSENSITIVE_ENUMS).build().setPropertyNamingStrategy(SNAKE_CASE);
    public static final CytominePermissions customClaims;
    public static final String PROVIDER_ID = "cytomine-protocol-mapper";
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        try {
            customClaims = objectMapper.readValue(new File("/opt/keycloak/providers/cytomine_permissions.yml"),
                CytominePermissions.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, CytomineProtocolMapper.class);
    }

    @Override
    public String getDisplayCategory() {
        return "Token Mapper";
    }

    @Override
    public String getDisplayType() {
        return "Custom Token Mapper";
    }

    @Override
    public String getHelpText() {
        return "Adds a example text to the claim";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession,
        KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx) {
        Set<CytominePermission> appliedCustomClaims = clientSessionCtx.getRolesStream().flatMap(
            role -> Optional.ofNullable(role.getName()).stream()
                .flatMap(name -> Optional.ofNullable(customClaims.roles().get(name.toLowerCase())).stream())
                .flatMap(Set::stream)).collect(Collectors.toSet());
        token.getOtherClaims().put("cytomine_claims", appliedCustomClaims);
    }
}
