package org.cytomine.keycloak;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

public class CytomineProtocolMapper extends AbstractOIDCProtocolMapper
    implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    public static final Map<String, List<String>> customClaims =
        Map.of("ADMIN", List.of("CAN_SEE_ALL_PROJECTS"), "USER", List.of("CAN_EDIT_PROJECT"), "GUEST",
            List.of("CAN_EDIT_PROJECT"));

    public static final String PROVIDER_ID = "cytomine-protocol-mapper";

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

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
        Set<String> appliedCustomClaims = clientSessionCtx.getRolesStream().flatMap(
                role ->
                    Optional.ofNullable(role.getName()).stream()
                        .flatMap(name -> Optional.ofNullable(customClaims.get(name)).stream())
                        .flatMap(List::stream))
            .collect(Collectors.toSet());
        token.getOtherClaims().put("cytomine_claims", appliedCustomClaims);
    }
}
