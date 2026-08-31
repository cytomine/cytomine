package be.cytomine.service.image;

import java.util.Optional;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.image.UploadedFile;
import be.cytomine.repository.image.UploadedFileRepository;
import be.cytomine.service.ModelService;
import be.cytomine.service.security.SecurityACLService;
import be.cytomine.utils.JsonObject;

import static org.springframework.security.acls.domain.BasePermission.READ;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class UploadedFileService extends ModelService {

    private final SecurityACLService securityACLService;

    private final UploadedFileRepository uploadedFileRepository;

    @Override
    public Class currentDomain() {
        return UploadedFile.class;
    }

    @Override
    public CytomineDomain createFromJSON(JsonObject json) {
        return new UploadedFile().buildDomainFromJson(json, getEntityManager());
    }

    public Optional<UploadedFile> find(Long id) {
        Optional<UploadedFile> uploadedFile = uploadedFileRepository.findById(id);
        uploadedFile.ifPresent(file -> securityACLService.check(file.container(), READ));
        return uploadedFile;
    }
}
