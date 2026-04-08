package be.cytomine.controller.ontology;

/*
* Copyright (c) 2009-2022. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
public class RelationTermResourceTests {

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private MockMvc restRelationTermControllerMockMvc;

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
    public void list_by_term_position_1() throws Exception {
        Term term1 = builder.given_a_term();
        Term term2 = builder.given_a_term(term1.getOntology());
        Long userId = builder.given_superadmin().getId();
        long relationId = 42L;

        when(termRelationHttpContract.findTermByID(eq(relationId), eq(userId)))
            .thenReturn(Optional.of(buildResponse(relationId, term1, term2)));

        restRelationTermControllerMockMvc.perform(get("/api/relation/term/{id}.json", relationId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.term1Id").value(term1.getId().intValue()));
    }

    @Test
    @Transactional
    public void list_by_term_position_2() throws Exception {
        Term term1 = builder.given_a_term();
        Term term2 = builder.given_a_term(term1.getOntology());
        Long userId = builder.given_superadmin().getId();
        long relationId = 42L;

        when(termRelationHttpContract.findTermByID(eq(relationId), eq(userId)))
            .thenReturn(Optional.of(buildResponse(relationId, term1, term2)));

        restRelationTermControllerMockMvc.perform(get("/api/relation/term/{id}.json", relationId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.term2Id").value(term2.getId().intValue()));
    }

    @Test
    @Transactional
    public void list_by_term() throws Exception {
        Term term1 = builder.given_a_term();
        Term term2 = builder.given_a_term(term1.getOntology());
        Long userId = builder.given_superadmin().getId();
        long relationId = 42L;

        when(termRelationHttpContract.findTermByID(eq(relationId), eq(userId)))
            .thenReturn(Optional.of(buildResponse(relationId, term1, term2)));

        restRelationTermControllerMockMvc.perform(get("/api/relation/term/{id}.json", relationId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(relationId));
    }

    @Test
    @Transactional
    public void get_a_relation_term() throws Exception {
        Term term1 = builder.given_a_term();
        Term term2 = builder.given_a_term(term1.getOntology());
        Long userId = builder.given_superadmin().getId();
        long relationId = 42L;

        when(termRelationHttpContract.findTermByID(eq(relationId), eq(userId)))
            .thenReturn(Optional.of(buildResponse(relationId, term1, term2)));

        restRelationTermControllerMockMvc.perform(get("/api/relation/term/{id}.json", relationId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(relationId))
            .andExpect(jsonPath("$.term1Id").value(term1.getId().intValue()))
            .andExpect(jsonPath("$.term2Id").value(term2.getId().intValue()));
    }

    @Test
    @Transactional
    public void get_a_parent_relation_term() throws Exception {
        Term term1 = builder.given_a_term();
        Term term2 = builder.given_a_term(term1.getOntology());
        Long userId = builder.given_superadmin().getId();
        long relationId = 42L;

        when(termRelationHttpContract.findTermByID(eq(relationId), eq(userId)))
            .thenReturn(Optional.of(buildResponse(relationId, term1, term2)));

        restRelationTermControllerMockMvc.perform(get("/api/relation/term/{id}.json", relationId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(relationId))
            .andExpect(jsonPath("$.ontologyId").value(term1.getOntology().getId().intValue()));
    }

    @Test
    @Transactional
    public void add_valid_relation() throws Exception {
        Term term1 = builder.given_a_term();
        Term term2 = builder.given_a_term(term1.getOntology());
        Long userId = builder.given_superadmin().getId();
        long relationId = 42L;
        UUID commandId = UUID.randomUUID();

        when(termRelationHttpContract.create(eq(userId), any())).thenReturn(
            Optional.of(new HttpCommandResponse(true, buildResponse(relationId, term1, term2), commandId,
                Commands.CREATE_TERM_RELATION)));

        String createJson = JsonObject.of("term1Id", term1.getId(), "term2Id", term2.getId(), "name", "parent")
            .toJsonString();

        restRelationTermControllerMockMvc.perform(
                post("/api/relation/term.json").contentType(MediaType.APPLICATION_JSON).content(createJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.command").value(Commands.CREATE_TERM_RELATION))
            .andExpect(jsonPath("$.data.id").value(relationId));
    }

    @Test
    @Transactional
    public void delete_relation_term() throws Exception {
        Term term1 = builder.given_a_term();
        Term term2 = builder.given_a_term(term1.getOntology());
        Long userId = builder.given_superadmin().getId();
        long relationId = 42L;
        UUID commandId = UUID.randomUUID();

        when(termRelationHttpContract.delete(eq(relationId), eq(userId))).thenReturn(
            Optional.of(new HttpCommandResponse(true, buildResponse(relationId, term1, term2), commandId,
                Commands.DELETE_TERM_RELATION)));

        restRelationTermControllerMockMvc.perform(delete("/api/relation/term/{id}.json", relationId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.command").value(Commands.DELETE_TERM_RELATION))
            .andExpect(jsonPath("$.data.id").value(relationId));
    }

    @Test
    @Transactional
    public void delete_parent_relation_term() throws Exception {
        Term term1 = builder.given_a_term();
        Term term2 = builder.given_a_term(term1.getOntology());
        Long userId = builder.given_superadmin().getId();
        long relationId = 42L;
        UUID commandId = UUID.randomUUID();

        when(termRelationHttpContract.delete(eq(relationId), eq(userId))).thenReturn(
            Optional.of(new HttpCommandResponse(true, buildResponse(relationId, term1, term2), commandId,
                Commands.DELETE_TERM_RELATION)));

        restRelationTermControllerMockMvc.perform(delete("/api/relation/term/{id}.json", relationId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.command").value(Commands.DELETE_TERM_RELATION));
    }

    @Test
    @Transactional
    public void delete_unexisting_relation_term_fails() throws Exception {
        Long userId = builder.given_superadmin().getId();
        long relationId = 99L;

        when(termRelationHttpContract.delete(eq(relationId), eq(userId))).thenReturn(Optional.empty());

        restRelationTermControllerMockMvc.perform(delete("/api/relation/term/{id}.json", relationId))
            .andExpect(status().isNotFound());
    }
}
