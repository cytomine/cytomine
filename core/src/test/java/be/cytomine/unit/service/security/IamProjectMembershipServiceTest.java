package be.cytomine.unit.service.security;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import be.cytomine.service.security.IamProjectMembershipService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class IamProjectMembershipServiceTest {

    @Mock
    private Keycloak keycloak;

    private IamProjectMembershipService iamProjectMembershipService;

    @BeforeEach
    public void setUp() {
        iamProjectMembershipService = new IamProjectMembershipService(keycloak);
        ReflectionTestUtils.setField(iamProjectMembershipService, "externalUsernameSuffixes",
            List.of("@lifescience-ri.eu"));
    }

    @Test
    public void isExternalShouldMatchDefaultSuffix() {
        assertTrue(iamProjectMembershipService.isExternal("flora@lifescience-ri.eu"));
        assertFalse(iamProjectMembershipService.isExternal("flora"));
        assertFalse(iamProjectMembershipService.isExternal(null));
    }

    @Test
    public void isExternalShouldMatchAnyConfiguredSuffix() {
        ReflectionTestUtils.setField(iamProjectMembershipService, "externalUsernameSuffixes",
            List.of("@provider-1.eu", " @provider-2.eu"));

        assertTrue(iamProjectMembershipService.isExternal("flora@provider-1.eu"));
        assertTrue(iamProjectMembershipService.isExternal("flora@provider-2.eu"));
        assertFalse(iamProjectMembershipService.isExternal("flora@lifescience-ri.eu"));
        assertFalse(iamProjectMembershipService.isExternal("flora"));
    }

    @Test
    public void isExternalShouldNotMatchAnythingWhenNoSuffixIsConfigured() {
        ReflectionTestUtils.setField(iamProjectMembershipService, "externalUsernameSuffixes", List.of(""));

        assertFalse(iamProjectMembershipService.isExternal("flora@lifescience-ri.eu"));
        assertFalse(iamProjectMembershipService.isExternal("flora"));
        assertFalse(iamProjectMembershipService.isExternal(null));
    }
}
