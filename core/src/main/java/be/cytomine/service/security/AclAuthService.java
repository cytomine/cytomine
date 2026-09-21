package be.cytomine.service.security;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import be.cytomine.domain.security.User;
import be.cytomine.repository.security.AclRepository;

@RequiredArgsConstructor
@Service
public class AclAuthService {

    private final AclRepository aclRepository;

    public List<Integer> get(Long domainId, String domainClassName, User user) {
        return aclRepository.listMaskForUsers(domainId, aclRepository.getAclClassId(domainClassName),
            user.getUsername());
    }
}
