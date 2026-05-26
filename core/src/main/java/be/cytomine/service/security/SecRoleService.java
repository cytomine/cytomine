package be.cytomine.service.security;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.domain.security.SecRole;
import be.cytomine.repository.security.SecRoleRepository;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class SecRoleService {

    private final SecRoleRepository secRoleRepository;

    private final SecurityACLService securityACLService;

    public Optional<SecRole> find(Long id) {
        securityACLService.checkGuest();
        return secRoleRepository.findById(id);
    }

    public Optional<SecRole> findByAuthority(String authority) {
        securityACLService.checkGuest();
        return secRoleRepository.findByAuthority(authority);
    }

    public List<SecRole> list() {
        securityACLService.checkGuest();
        return secRoleRepository.findAll();
    }
}
