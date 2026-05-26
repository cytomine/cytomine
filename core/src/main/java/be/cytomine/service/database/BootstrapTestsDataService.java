package be.cytomine.service.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import be.cytomine.domain.security.SecRole;
import be.cytomine.domain.security.SecUserSecRole;
import be.cytomine.domain.security.User;
import be.cytomine.repository.security.SecRoleRepository;
import be.cytomine.repository.security.SecUserSecRoleRepository;
import be.cytomine.repository.security.UserRepository;

import static be.cytomine.repository.security.SecRoleRepository.ROLE_ADMIN;
import static be.cytomine.repository.security.SecRoleRepository.ROLE_GUEST;
import static be.cytomine.repository.security.SecRoleRepository.ROLE_SUPER_ADMIN;
import static be.cytomine.repository.security.SecRoleRepository.ROLE_USER;

@Slf4j
@RequiredArgsConstructor
@Service
public class BootstrapTestsDataService {

    public static final String SUPERADMIN = "SUPER_ADMIN_ACL";

    public static final String ADMIN = "ADMIN_ACL";

    public static final String GUEST = "GUEST_ACL";

    public static final String USER_ACL_READ = "USER_ACL_READ";

    public static final String USER_ACL_CREATE = "USER_ACL_CREATE";

    public static final String USER_ACL_WRITE = "USER_ACL_WRITE";

    public static final String USER_ACL_DELETE = "USER_ACL_DELETE";

    public static final String USER_ACL_ADMIN = "USER_ACL_ADMIN";

    public static final String USER_NO_ACL = "ACL_USER_NO_ACL";

    public static final String CREATOR = "CREATOR";

    private final UserRepository userRepository;

    private final SecRoleRepository secRoleRepository;

    private final SecUserSecRoleRepository secSecUserSecRoleRepository;

    public static final Map<String, List<String>> ROLES = new HashMap<>();

    static {
        ROLES.put(SUPERADMIN, List.of(ROLE_SUPER_ADMIN));
        ROLES.put(ADMIN, List.of(ROLE_ADMIN));
        ROLES.put(USER_NO_ACL, List.of(ROLE_USER));
        ROLES.put(USER_ACL_READ, List.of(ROLE_USER));
        ROLES.put(USER_ACL_WRITE, List.of(ROLE_USER));
        ROLES.put(USER_ACL_CREATE, List.of(ROLE_USER));
        ROLES.put(USER_ACL_DELETE, List.of(ROLE_USER));
        ROLES.put(USER_ACL_ADMIN, List.of(ROLE_USER));
        ROLES.put(CREATOR, List.of(ROLE_USER));
        ROLES.put(GUEST, List.of(ROLE_GUEST));
    }

    public User createUserForTests(String login) {
        Optional<User> alreadyExistingUser = userRepository.findByUsernameLikeIgnoreCase(login.toLowerCase());
        if (!ROLES.containsKey(login)) {
            throw new RuntimeException("Cannot execute test because user has not authority defined");
        }
        List<String> authoritiesConstants = ROLES.get(login);

        if (alreadyExistingUser.isPresent()) {
            Set<SecRole> allRoleByUser = secSecUserSecRoleRepository.findAllRoleByUser(alreadyExistingUser.get());
            for (String authoritiesConstant : authoritiesConstants) {
                if (!allRoleByUser.stream().anyMatch(x -> x.getAuthority().equals(authoritiesConstant))) {
                    throw new RuntimeException("Cannot execute test because already existing user "
                        + login
                        + "  has not same roles: not present - "
                        + authoritiesConstant);
                }
            }
            for (SecRole secRole : allRoleByUser) {
                if (!authoritiesConstants.stream().anyMatch(x -> x.equals(secRole.getAuthority()))) {
                    throw new RuntimeException("Cannot execute test because already existing user "
                        + login
                        + " has not same roles: should not be there - "
                        + secRole.getAuthority());
                }
            }
            return alreadyExistingUser.get();
        }

        User user = new User();
        user.setUsername(login);
        user.setName("firstname lastname");
        user.setReference(UUID.randomUUID().toString());
        user.generateKeys();
        user = userRepository.save(user);
        userRepository.findById(user.getId()); // flush

        for (String authority : authoritiesConstants) {
            SecRole secRole = secRoleRepository.getByAuthority(authority);
            SecUserSecRole secSecUserSecRole = new SecUserSecRole();
            secSecUserSecRole.setSecUser(user);
            secSecUserSecRole.setSecRole(secRole);
            secSecUserSecRoleRepository.save(secSecUserSecRole);
        }
        userRepository.findById(user.getId()); // flush
        return user;
    }
}
