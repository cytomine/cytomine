package be.cytomine.controller.ontology;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.TermRelationHttpContract;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.TermRelationResponse;
import be.cytomine.common.repository.model.termrelation.payload.CreateTermRelation;
import be.cytomine.common.repository.model.termrelation.payload.UpdateTermRelation;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.ontology.RelationTerm;
import be.cytomine.utils.JsonObject;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
public class TermRelationResourceTests {

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private MockMvc restTermControllerMockMvc;

    @MockitoBean
    private TermRelationHttpContract termRelationHttpContract;

    private TermRelationResponse buildResponse(RelationTerm relationTerm, long ontologyId) {
        return new TermRelationResponse(relationTerm.getId(), relationTerm.getTerm1().getId(),
            relationTerm.getTerm2().getId(), ontologyId, relationTerm.getRelation().getId(),
            LocalDateTime.ofInstant(relationTerm.getUpdated().toInstant(), ZoneId.systemDefault()),
            Optional.empty(),
            LocalDateTime.ofInstant(relationTerm.getCreated().toInstant(), ZoneId.systemDefault()),
            relationTerm.getRelation().getName());
    }

    @Test
    @Transactional
    public void getATermRelation() throws Exception {
        RelationTerm relationTerm = builder.given_a_relation_term();
        long ontologyId = relationTerm.getTerm1().getOntology().getId();
        long userId = builder.given_superadmin().getId();
        when(termRelationHttpContract.findTermByID(eq(relationTerm.getId()), eq(userId)))
            .thenReturn(Optional.of(buildResponse(relationTerm, ontologyId)));

        restTermControllerMockMvc.perform(get("/api/relation/term/{id}.json", relationTerm.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(relationTerm.getId()))
            .andExpect(jsonPath("$.term1Id").value(relationTerm.getTerm1().getId()))
            .andExpect(jsonPath("$.term2Id").value(relationTerm.getTerm2().getId()))
            .andExpect(jsonPath("$.ontologyId").value(ontologyId));
    }

    @Test
    @Transactional
    public void getATermRelationNotFoundReturns404() throws Exception {
        long userId = builder.given_superadmin().getId();
        when(termRelationHttpContract.findTermByID(eq(999L), eq(userId))).thenReturn(Optional.empty());

        restTermControllerMockMvc.perform(get("/api/relation/term/{id}.json", 999L))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void addTermRelation() throws Exception {
        RelationTerm relationTerm = builder.given_a_relation_term();
        long ontologyId = relationTerm.getTerm1().getOntology().getId();
        long userId = builder.given_superadmin().getId();
        UUID commandId = UUID.randomUUID();
        CreateTermRelation createTermRelation = new CreateTermRelation(relationTerm.getTerm1().getId(),
            relationTerm.getTerm2().getId(), RelationTerm.PARENT);
        when(termRelationHttpContract.create(eq(userId), eq(createTermRelation))).thenReturn(Optional.of(
            new HttpCommandResponse(true, buildResponse(relationTerm, ontologyId), commandId,
                Commands.CREATE_TERM_RELATION)));

        String body = JsonObject.of("term1", relationTerm.getTerm1().getId(), "term2",
            relationTerm.getTerm2().getId()).toJsonString();

        restTermControllerMockMvc.perform(
                post("/api/relation/term.json").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.command").value("be.cytomine.AddTermRelationCommand"))
            .andExpect(jsonPath("$.data.term1Id").value(relationTerm.getTerm1().getId()))
            .andExpect(jsonPath("$.data.term2Id").value(relationTerm.getTerm2().getId()));
    }

    @Test
    @Transactional
    public void addTermRelationWithNoWriteAccessReturnsEmpty() throws Exception {
        RelationTerm relationTerm = builder.given_a_relation_term();
        long userId = builder.given_superadmin().getId();
        CreateTermRelation createTermRelation = new CreateTermRelation(relationTerm.getTerm1().getId(),
            relationTerm.getTerm2().getId(), RelationTerm.PARENT);
        when(termRelationHttpContract.create(eq(userId), eq(createTermRelation))).thenReturn(Optional.empty());

        String body = JsonObject.of("term1", relationTerm.getTerm1().getId(), "term2",
            relationTerm.getTerm2().getId()).toJsonString();

        restTermControllerMockMvc.perform(
                post("/api/relation/term.json").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    @Transactional
    public void editTermRelation() throws Exception {
        RelationTerm relationTerm = builder.given_a_relation_term();
        long ontologyId = relationTerm.getTerm1().getOntology().getId();
        long userId = builder.given_superadmin().getId();
        UUID commandId = UUID.randomUUID();
        UpdateTermRelation updateTermRelation = new UpdateTermRelation(Optional.of(relationTerm.getTerm1().getId()),
            Optional.of(relationTerm.getTerm2().getId()), Optional.empty());
        when(termRelationHttpContract.update(eq(relationTerm.getId()), eq(userId), eq(updateTermRelation)))
            .thenReturn(Optional.of(
                new HttpCommandResponse(true, buildResponse(relationTerm, ontologyId), commandId,
                    Commands.UPDATE_TERM_RELATION)));

        String body = JsonObject.of("term1", relationTerm.getTerm1().getId(), "term2",
            relationTerm.getTerm2().getId()).toJsonString();

        restTermControllerMockMvc.perform(
                put("/api/relation/term/{id}.json", relationTerm.getId()).contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.command").value("be.cytomine.EditTermRelationCommand"))
            .andExpect(jsonPath("$.data.term1Id").value(relationTerm.getTerm1().getId()))
            .andExpect(jsonPath("$.data.term2Id").value(relationTerm.getTerm2().getId()));
    }

    @Test
    @Transactional
    public void editTermRelationWithNoWriteAccessReturnsNotFound() throws Exception {
        RelationTerm relationTerm = builder.given_a_relation_term();
        long userId = builder.given_superadmin().getId();
        UpdateTermRelation updateTermRelation = new UpdateTermRelation(Optional.of(relationTerm.getTerm1().getId()),
            Optional.of(relationTerm.getTerm2().getId()), Optional.empty());
        when(termRelationHttpContract.update(eq(relationTerm.getId()), eq(userId), eq(updateTermRelation)))
            .thenReturn(Optional.empty());

        String body = JsonObject.of("term1", relationTerm.getTerm1().getId(), "term2",
            relationTerm.getTerm2().getId()).toJsonString();

        restTermControllerMockMvc.perform(
                put("/api/relation/term/{id}.json", relationTerm.getId()).contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void deleteTermRelation() throws Exception {
        RelationTerm relationTerm = builder.given_a_relation_term();
        long ontologyId = relationTerm.getTerm1().getOntology().getId();
        long userId = builder.given_superadmin().getId();
        UUID commandId = UUID.randomUUID();
        when(termRelationHttpContract.delete(eq(relationTerm.getId()), eq(userId))).thenReturn(Optional.of(
            new HttpCommandResponse(true, buildResponse(relationTerm, ontologyId), commandId,
                Commands.DELETE_TERM_RELATION)));

        restTermControllerMockMvc.perform(delete("/api/relation/term/{id}.json", relationTerm.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.command").value("be.cytomine.DeleteTermRelationCommand"))
            .andExpect(jsonPath("$.data.term1Id").value(relationTerm.getTerm1().getId()))
            .andExpect(jsonPath("$.data.term2Id").value(relationTerm.getTerm2().getId()));
    }

    @Test
    @Transactional
    public void deleteTermRelationWithNoDeleteAccessReturnsNotFound() throws Exception {
        RelationTerm relationTerm = builder.given_a_relation_term();
        long userId = builder.given_superadmin().getId();
        when(termRelationHttpContract.delete(eq(relationTerm.getId()), eq(userId))).thenReturn(Optional.empty());

        restTermControllerMockMvc.perform(delete("/api/relation/term/{id}.json", relationTerm.getId()))
            .andExpect(status().isNotFound());
    }
}
