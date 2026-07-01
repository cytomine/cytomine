package org.cytomine.repository.http;

import java.time.LocalDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.cytomine.repository.mapper.OntologyMapper;
import org.cytomine.repository.persistence.OntologyRepository;
import org.cytomine.repository.persistence.UserRepository;
import org.cytomine.repository.service.ACLService;
import org.cytomine.repository.service.OntologyCommandService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.cytomine.common.repository.http.OntologyHttpContract;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.OntologyResponse;
import be.cytomine.common.repository.model.ontology.payload.CreateOntology;
import be.cytomine.common.repository.model.ontology.payload.OntologyLight;
import be.cytomine.common.repository.model.ontology.payload.UpdateOntology;

import static be.cytomine.common.repository.http.OntologyHttpContract.ROOT_PATH;
import static java.time.temporal.ChronoUnit.MICROS;

@RequiredArgsConstructor
@RestController
@RequestMapping(ROOT_PATH)
public class OntologyController implements OntologyHttpContract {
    private final OntologyCommandService service;
    private final OntologyRepository repository;
    private final UserRepository userRepository;
    private final ACLService aclService;
    private final OntologyMapper ontologyMapper;

    @Override
    public Optional<OntologyResponse> get(long id, long userId) {
        return repository.findByIdAndDeletedNull(id)
            .filter(ontologyEntity -> aclService.canReadOntology(userId, ontologyEntity.getId()))
            .map(ontologyMapper::mapToOntologyResponse);

    }

    @Override
    public Optional<OntologyLight> getLight(long id, long userId) {
        return repository.findByIdAndDeletedNull(id)
            .filter(ontologyEntity -> aclService.canReadOntology(userId, ontologyEntity.getId())).flatMap(
                ontologyEntity -> userRepository.findById(ontologyEntity.getUserId())
                    .map(user -> ontologyMapper.mapToOntologyLight(ontologyEntity)));
    }

    @Override
    public Optional<HttpCommandResponse> create(long userId, CreateOntology createPayload) {
        return service.create(userId, createPayload, LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<HttpCommandResponse> update(long id, long userId, UpdateOntology updateOntology) {
        return service.update(userId, id, updateOntology, LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Optional<HttpCommandResponse> delete(long id, long userId) {
        return service.delete(userId, id, LocalDateTime.now().truncatedTo(MICROS));
    }

    @Override
    public Page<OntologyLight> getAllLightForUser(long userId, Pageable pageable) {
        return repository.findAllByUserIdAndDeletedNull(userId, pageable).map(ontologyMapper::mapToOntologyLight);
    }

    @Override
    public Page<OntologyResponse> getAllForUser(long userId, Pageable pageable) {
        return repository.findAllByUserIdAndDeletedNull(userId, pageable).map(ontologyMapper::mapToOntologyResponse);
    }
}
