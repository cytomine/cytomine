package org.cytomine.repository.http;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import lombok.Getter;
import org.cytomine.repository.RepositoryApp;
import org.cytomine.repository.mapper.ApplyCommandResponseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.TagDomainAssociationHttpContract;
import be.cytomine.common.repository.model.command.payload.response.TagDomainAssociationResponse;
import be.cytomine.common.repository.model.tagdomainassociation.payload.CreateTagDomainAssociation;
import be.cytomine.common.repository.model.tagdomainassociation.payload.UpdateTagDomainAssociation;

@SpringBootTest(classes = RepositoryApp.class)
@AutoConfigureMockMvc
@Import(PostGisTestConfiguration.class)
@Getter
class TagDomainAssociationControllerTest
    implements CRUDCommandTests<CreateTagDomainAssociation, TagDomainAssociationResponse, UpdateTagDomainAssociation> {

    String apiURL = TagDomainAssociationHttpContract.ROOT_PATH;
    CreateTagDomainAssociation createPayload;
    UpdateTagDomainAssociation updatePayload = new UpdateTagDomainAssociation(
        Optional.empty(),
        Optional.of("be.cytomine.domain.project.Project"),
        Optional.empty()
    );
    @Autowired
    ApplyCommandResponseMapper applyCommandResponseMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void beforeCreate(long userId) {
        Long tagId = jdbcTemplate.queryForObject("SELECT nextval('hibernate_sequence')", Long.class);
        jdbcTemplate.update(
            "INSERT INTO tag (id, version, name, user_id) VALUES (?, 0, ?, ?)",
            tagId, UUID.randomUUID().toString(), userId
        );
        createPayload = new CreateTagDomainAssociation(tagId, "be.cytomine.domain.ontology.Ontology", userId);
    }

    @Override
    public TagDomainAssociationResponse expectedUpdatedResponse(
        TagDomainAssociationResponse response,
        UpdateTagDomainAssociation updatePayload,
        Instant updatedTime
    ) {
        return new TagDomainAssociationResponse(
            response.id(),
            updatePayload.tagId().orElse(response.tagId()),
            updatePayload.domainClassName().orElse(response.domainClassName()),
            updatePayload.domainId().orElse(response.domainId()),
            response.created(),
            Optional.of(updatedTime),
            response.deleted()
        );
    }
}
