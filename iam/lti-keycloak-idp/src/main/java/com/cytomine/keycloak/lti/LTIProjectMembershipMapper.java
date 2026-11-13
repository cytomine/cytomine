package com.cytomine.keycloak.lti;

import org.jboss.logging.Logger;
import org.keycloak.broker.provider.AbstractIdentityProviderMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Identity-provider mapper that merges Cytomine project join token(s) carried in the LTI launch's
 * {@code custom} claim into a multivalued user attribute (default {@code lti_join_tokens}).
 *
 * <p>The Web-UI picker (deep linking) writes {@code custom.cytomine_join_token} into the content item it returns;
 * Moodle echoes that same {@code custom} claim on every subsequent student launch of the item. This mapper turns
 * the token into a user attribute that a protocol mapper on the {@code profile} client scope exposes as the
 * {@code lti_join_tokens} JWT claim, which Cytomine Core then uses to grant additive, non-admin project
 * membership.</p>
 *
 * <p>Tokens are <strong>merged, never replaced</strong>, so memberships across multiple courses/projects
 * accumulate.</p>
 */
public class LTIProjectMembershipMapper extends AbstractIdentityProviderMapper {

    private static final Logger log = Logger.getLogger(LTIProjectMembershipMapper.class);

    public static final String PROVIDER_ID = "lti-project-membership-mapper";

    private static final String CONFIG_CUSTOM_TOKEN_KEY = "customTokenKey";
    private static final String CONFIG_TARGET_ATTRIBUTE = "targetAttribute";

    private static final String DEFAULT_CUSTOM_TOKEN_KEY = "cytomine_join_token";
    private static final String DEFAULT_TARGET_ATTRIBUTE = "lti_join_tokens";

    @Override
    public String[] getCompatibleProviders() {
        return new String[]{LTIIdentityProviderFactory.PROVIDER_ID};
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        ProviderConfigProperty tokenKey = new ProviderConfigProperty();
        tokenKey.setName(CONFIG_CUSTOM_TOKEN_KEY);
        tokenKey.setLabel("Custom token parameter");
        tokenKey.setHelpText(
            "Key inside the launch's `custom` claim that carries the Cytomine project join token. " +
                "Default: " + DEFAULT_CUSTOM_TOKEN_KEY + ".");
        tokenKey.setType(ProviderConfigProperty.STRING_TYPE);
        tokenKey.setDefaultValue(DEFAULT_CUSTOM_TOKEN_KEY);

        ProviderConfigProperty attribute = new ProviderConfigProperty();
        attribute.setName(CONFIG_TARGET_ATTRIBUTE);
        attribute.setLabel("Target user attribute");
        attribute.setHelpText(
            "Multivalued user attribute where detected join tokens are merged. Default: " +
                DEFAULT_TARGET_ATTRIBUTE + ". Expose it as a JWT claim with a protocol mapper on the " +
                "'profile' client scope ('multivalued' = true).");
        attribute.setType(ProviderConfigProperty.STRING_TYPE);
        attribute.setDefaultValue(DEFAULT_TARGET_ATTRIBUTE);

        return List.of(tokenKey, attribute);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayCategory() {
        return "Role Importer";
    }

    @Override
    public String getDisplayType() {
        return "LTI Project Membership";
    }

    @Override
    public String getHelpText() {
        return "Merges the Cytomine project join token(s) carried in the launch's `custom` claim into a " +
            "multivalued user attribute (default " + DEFAULT_TARGET_ATTRIBUTE + ") so downstream clients can " +
            "grant project membership additively. Tokens are merged, never replaced, so memberships across " +
            "multiple courses/projects accumulate.";
    }

    @Override
    public void importNewUser(KeycloakSession session, RealmModel realm, UserModel user,
                              IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        applyMembership(user, mapperModel, context);
    }

    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user,
                                   IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        applyMembership(user, mapperModel, context);
    }

    private void applyMembership(UserModel user, IdentityProviderMapperModel mapperModel,
                                 BrokeredIdentityContext context) {
        String tokenKey = configValue(mapperModel, CONFIG_CUSTOM_TOKEN_KEY, DEFAULT_CUSTOM_TOKEN_KEY);
        String targetAttribute = configValue(mapperModel, CONFIG_TARGET_ATTRIBUTE, DEFAULT_TARGET_ATTRIBUTE);

        Object custom = context.getContextData().get(LTIIdentityProvider.CTX_LTI_CUSTOM);
        if (!(custom instanceof Map<?, ?> customMap)) {
            return;
        }
        Object rawToken = customMap.get(tokenKey);
        if (rawToken == null) {
            return;
        }

        Set<String> newTokens = new LinkedHashSet<>();
        if (rawToken instanceof Collection<?> collection) {
            for (Object o : collection) {
                if (o != null && !o.toString().isBlank()) {
                    newTokens.add(o.toString());
                }
            }
        } else if (!rawToken.toString().isBlank()) {
            newTokens.add(rawToken.toString());
        }
        if (newTokens.isEmpty()) {
            return;
        }

        List<String> existing = user.getAttributeStream(targetAttribute).toList();
        Set<String> merged = new LinkedHashSet<>();
        if (existing != null) {
            merged.addAll(existing);
        }
        boolean changed = false;
        for (String token : newTokens) {
            changed |= merged.add(token);
        }

        if (changed) {
            user.setAttribute(targetAttribute, new ArrayList<>(merged));
            log.infof("LTIProjectMembershipMapper: merged %d join token(s) into '%s' for user %s",
                newTokens.size(), targetAttribute, user.getUsername());
        }
    }

    private String configValue(IdentityProviderMapperModel mapperModel, String key, String defaultValue) {
        String v = mapperModel.getConfig() == null ? null : mapperModel.getConfig().get(key);
        if (v == null || v.isBlank()) {
            v = defaultValue;
        }
        return v;
    }
}