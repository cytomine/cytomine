package org.cytomine.repository.http;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.Getter;
import org.cytomine.repository.RepositoryApp;
import org.cytomine.repository.mapper.BaseMapper;
import org.cytomine.repository.mapper.OntologyMapper;
import org.cytomine.repository.mapper.TermMapper;
import org.cytomine.repository.persistence.TermRepository;
import org.cytomine.repository.persistence.entity.TermEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.OntologyHttpContract;
import be.cytomine.common.repository.model.command.payload.response.ApplyCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.OntologyResponse;
import be.cytomine.common.repository.model.command.payload.response.TermResponse;
import be.cytomine.common.repository.model.ontology.payload.CreateOntology;
import be.cytomine.common.repository.model.ontology.payload.UpdateOntology;

@SpringBootTest(classes = RepositoryApp.class)
@AutoConfigureMockMvc
@Import(PostGisTestConfiguration.class)
@Getter
public class OntologyControllerTest implements CRUDCommandTests<CreateOntology, OntologyResponse, UpdateOntology> {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    OntologyMapper ontologyController;

    @Autowired
    TermRepository termRepository;

    @Autowired
    TermMapper termMapper;

    @Autowired
    BaseMapper baseMapper;

    String apiURL = OntologyHttpContract.ROOT_PATH;
    CreateOntology createPayload = new CreateOntology(UUID.randomUUID().toString());
    UpdateOntology updatePayload = new UpdateOntology(Optional.of(UUID.randomUUID().toString()));
    Set<TermResponse> subEntities;

    @Override
    public void createSubEntities(long userId, long currentId) {
        TermEntity subEntity = termRepository.save(
            new TermEntity(null, 0, currentId, UUID.randomUUID().toString(), UUID.randomUUID().toString(),
             baseMapper.map(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS)), null, null, "", Set.of()));
        subEntities = Set.of(termMapper.mapToTermResponse(subEntity));
    }

    @Override
    public Set<ApplyCommandResponse> expectDeletedSubEntities(LocalDateTime deletionTime) {
        return subEntities.stream().map(e -> termMapper.updateDeleteTime(e, Optional.of(deletionTime)))
            .collect(Collectors.toSet());
    }

    @Override
    public OntologyResponse expectedUpdatedResponse(OntologyResponse response, UpdateOntology updatePayload,
        LocalDateTime updated) {
        return new OntologyResponse(updatePayload.name().orElse(response.name()), response.id(), response.terms(),
            response.created(), Optional.of(updated), response.deleted(), response.user());
    }

    @Override
    public OntologyResponse expectedDeletedResponse(OntologyResponse response, LocalDateTime deletedTime) {
        return new OntologyResponse(response.name(), response.id(),
            response.terms().stream().map(e -> termMapper.updateDeleteTime(e, Optional.of(deletedTime)))
                .collect(Collectors.toSet()), response.created(), response.updated(), Optional.of(deletedTime),
            response.user());
    }

    @Override
    public OntologyResponse expectChangedUpdatedTime(OntologyResponse response, LocalDateTime updatedTime) {
        return new OntologyResponse(response.name(), response.id(), subEntities, response.created(),
            Optional.of(updatedTime), response.deleted(), response.user());
    }
}
