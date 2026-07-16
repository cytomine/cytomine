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

import be.cytomine.common.repository.http.UserHttpContract;
import be.cytomine.common.repository.model.command.payload.response.RoleResponse;
import be.cytomine.common.repository.model.command.payload.response.UserResponse;
import be.cytomine.domain.security.SecRole;
import be.cytomine.domain.security.SecUserSecRole;
import be.cytomine.domain.security.User;
import be.cytomine.mapper.UserMapper;
import be.cytomine.repository.security.SecRoleRepository;
import be.cytomine.repository.security.SecUserSecRoleRepository;
import be.cytomine.repository.security.UserRepository;

import static be.cytomine.common.repository.model.Role.ROLE_ADMIN;
import static be.cytomine.common.repository.model.Role.ROLE_GUEST;
import static be.cytomine.common.repository.model.Role.ROLE_SUPER_ADMIN;
import static be.cytomine.common.repository.model.Role.ROLE_USER;

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
    public static final Map<String, List<String>> ROLES = new HashMap<>();

    static {
        ROLES.put(SUPERADMIN, List.of(ROLE_SUPER_ADMIN.toString()));
        ROLES.put(ADMIN, List.of(ROLE_ADMIN.toString()));
        ROLES.put(USER_NO_ACL, List.of(ROLE_USER.toString()));
        ROLES.put(USER_ACL_READ, List.of(ROLE_USER.toString()));
        ROLES.put(USER_ACL_WRITE, List.of(ROLE_USER.toString()));
        ROLES.put(USER_ACL_CREATE, List.of(ROLE_USER.toString()));
        ROLES.put(USER_ACL_DELETE, List.of(ROLE_USER.toString()));
        ROLES.put(USER_ACL_ADMIN, List.of(ROLE_USER.toString()));
        ROLES.put(CREATOR, List.of(ROLE_USER.toString()));
        ROLES.put(GUEST, List.of(ROLE_GUEST.toString()));
    }

    private final UserRepository userRepository;
    private final UserHttpContract userHttpContract;
    private final SecRoleRepository secRoleRepository;
    private final SecUserSecRoleRepository secSecUserSecRoleRepository;
    private final UserMapper userMapper;

    public UserResponse createUserForTests(String login) {
        Optional<UserResponse> alreadyExistingUser = userHttpContract.search(login.toLowerCase());
        if (!ROLES.containsKey(login)) {
            throw new RuntimeException("Cannot execute test because user has not authority defined");
        }
        List<String> authoritiesConstants = ROLES.get(login);

        if (alreadyExistingUser.isPresent()) {
            Set<RoleResponse> allRoleByUser = alreadyExistingUser.get().roles();
            for (String authoritiesConstant : authoritiesConstants) {
                if (!allRoleByUser.stream().anyMatch(x -> x.authority().equals(authoritiesConstant))) {
                    throw new RuntimeException("Cannot execute test because already existing user " + login
                        + "  has not same roles: not present - " + authoritiesConstant);
                }
            }
            for (RoleResponse role : allRoleByUser) {
                if (!authoritiesConstants.stream().anyMatch(x -> x.equals(role.authority()))) {
                    throw new RuntimeException("Cannot execute test because already existing user " + login
                        + " has not same roles: should not be there - " + role.authority());
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
        return userMapper.map(user);
    }
}
