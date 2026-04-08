package be.cytomine.controller.repository;

import java.time.LocalDateTime;
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
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.ontology.Term;
import be.cytomine.utils.JsonObject;

import static org.mockito.ArgumentMatchers.any;
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
    private MockMvc restTermRelationControllerMockMvc;

    @MockitoBean
    private TermRelationHttpContract termRelationHttpContract;

    private TermRelationResponse buildResponse(long id, Term term1, Term term2) {
        return new TermRelationResponse(
            id,
            term1.getId(),
            term2.getId(),
            term1.getOntology().getId(),
            1L,
            LocalDateTime.now(),
            Optional.empty(),
            LocalDateTime.now(),
            "parent"
        );
    }

    @Test
    @Transactional
    public void getATermRelation() throws Exception {
        Term term1 = builder.given_a_term();
        Term term2 = builder.given_a_term(term1.getOntology());
        Long userId = builder.given_superadmin().getId();
        long relationId = 42L;
        TermRelationResponse response = buildResponse(relationId, term1, term2);

        when(termRelationHttpContract.findTermByID(eq(relationId), eq(userId))).thenReturn(Optional.of(response));

        restTermRelationControllerMockMvc.perform(get("/api/term_relation/{id}.json", relationId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(relationId))
            .andExpect(jsonPath("$.term1Id").value(term1.getId().intValue()))
            .andExpect(jsonPath("$.term2Id").value(term2.getId().intValue()))
            .andExpect(jsonPath("$.ontologyId").value(term1.getOntology().getId().intValue()));
    }

    @Test
    @Transactional
    public void getATermRelationWithNoAccessReturnsNotFound() throws Exception {
        Long userId = builder.given_superadmin().getId();
        long relationId = 99L;

        when(termRelationHttpContract.findTermByID(eq(relationId), eq(userId))).thenReturn(Optional.empty());

        restTermRelationControllerMockMvc.perform(get("/api/term_relation/{id}.json", relationId))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void addValidTermRelation() throws Exception {
        Term term1 = builder.given_a_term();
        Term term2 = builder.given_a_term(term1.getOntology());
        Long userId = builder.given_superadmin().getId();
        long relationId = 42L;
        UUID commandId = UUID.randomUUID();
        TermRelationResponse response = buildResponse(relationId, term1, term2);

        when(termRelationHttpContract.create(eq(userId), any())).thenReturn(
            Optional.of(new HttpCommandResponse(true, response, commandId, Commands.CREATE_TERM_RELATION)));

        String createJson = JsonObject.of("term1Id", term1.getId(), "term2Id", term2.getId(), "name", "parent")
                                .toJsonString();

        restTermRelationControllerMockMvc.perform(
                post("/api/term_relation.json").contentType(MediaType.APPLICATION_JSON).content(createJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.command").value(Commands.CREATE_TERM_RELATION))
            .andExpect(jsonPath("$.data.id").value(relationId))
            .andExpect(jsonPath("$.data.term1Id").value(term1.getId().intValue()));
    }

    @Test
    @Transactional
    public void addTermRelationWithNoWriteAccessReturnsEmpty() throws Exception {
        Term term1 = builder.given_a_term();
        Term term2 = builder.given_a_term(term1.getOntology());
        Long userId = builder.given_superadmin().getId();

        when(termRelationHttpContract.create(eq(userId), any())).thenReturn(Optional.empty());

        String createJson = JsonObject.of("term1Id", term1.getId(), "term2Id", term2.getId(), "name", "parent")
                                .toJsonString();

        restTermRelationControllerMockMvc.perform(
                post("/api/term_relation.json").contentType(MediaType.APPLICATION_JSON).content(createJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    @Transactional
    public void editValidTermRelation() throws Exception {
        Term term1 = builder.given_a_term();
        Term term2 = builder.given_a_term(term1.getOntology());
        Long userId = builder.given_superadmin().getId();
        long relationId = 42L;
        UUID commandId = UUID.randomUUID();
        TermRelationResponse response = buildResponse(relationId, term1, term2);

        when(termRelationHttpContract.update(eq(relationId), eq(userId), any())).thenReturn(
            Optional.of(new HttpCommandResponse(true, response, commandId, Commands.UPDATE_TERM_RELATION)));

        String updateJson = JsonObject.of("term1Id", term1.getId(), "term2Id", term2.getId()).toJsonString();

        restTermRelationControllerMockMvc.perform(
                put("/api/term_relation/{id}.json", relationId).contentType(MediaType.APPLICATION_JSON)
                    .content(updateJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.command").value(Commands.UPDATE_TERM_RELATION))
            .andExpect(jsonPath("$.data.id").value(relationId));
    }

    @Test
    @Transactional
    public void editTermRelationWithNoWriteAccessReturnsNotFound() throws Exception {
        Long userId = builder.given_superadmin().getId();
        long relationId = 99L;

        when(termRelationHttpContract.update(eq(relationId), eq(userId), any())).thenReturn(Optional.empty());

        String updateJson = JsonObject.of("term1Id", 1L, "term2Id", 2L).toJsonString();

        restTermRelationControllerMockMvc.perform(
                put("/api/term_relation/{id}.json", relationId).contentType(MediaType.APPLICATION_JSON)
                    .content(updateJson))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void deleteTermRelation() throws Exception {
        Term term1 = builder.given_a_term();
        Term term2 = builder.given_a_term(term1.getOntology());
        Long userId = builder.given_superadmin().getId();
        long relationId = 42L;
        UUID commandId = UUID.randomUUID();
        TermRelationResponse response = buildResponse(relationId, term1, term2);

        when(termRelationHttpContract.delete(eq(relationId), eq(userId))).thenReturn(
            Optional.of(new HttpCommandResponse(true, response, commandId, Commands.DELETE_TERM_RELATION)));

        restTermRelationControllerMockMvc.perform(delete("/api/term_relation/{id}.json", relationId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.command").value(Commands.DELETE_TERM_RELATION))
            .andExpect(jsonPath("$.data.id").value(relationId));
    }

    @Test
    @Transactional
    public void deleteTermRelationWithNoDeleteAccessReturnsNotFound() throws Exception {
        Long userId = builder.given_superadmin().getId();
        long relationId = 99L;

        when(termRelationHttpContract.delete(eq(relationId), eq(userId))).thenReturn(Optional.empty());

        restTermRelationControllerMockMvc.perform(delete("/api/term_relation/{id}.json", relationId))
            .andExpect(status().isNotFound());
    }
}
