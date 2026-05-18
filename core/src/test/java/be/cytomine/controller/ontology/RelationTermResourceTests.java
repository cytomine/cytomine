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
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TermRelationHttpContract termRelationHttpContract;

    private TermRelationResponse buildResponse(RelationTerm relationTerm, long ontologyId) {
        return new TermRelationResponse(
            relationTerm.getId(), relationTerm.getTerm1().getId(),
            relationTerm.getTerm2().getId(), ontologyId, relationTerm.getRelation().getId(),
            LocalDateTime.ofInstant(relationTerm.getUpdated().toInstant(), ZoneId.systemDefault()),
            Optional.empty(),
            LocalDateTime.ofInstant(relationTerm.getCreated().toInstant(), ZoneId.systemDefault()),
            relationTerm.getRelation().getName()
        );
    }

    @Test
    @Transactional
    public void getATermRelation() throws Exception {
        RelationTerm relationTerm = builder.givenARelationTerm();
        long ontologyId = relationTerm.getTerm1().getOntology().getId();
        long userId = builder.givenSuperAdmin().getId();
        TermRelationResponse expected = buildResponse(relationTerm, ontologyId);
        when(termRelationHttpContract.findTermRelationByID(eq(relationTerm.getId()), eq(userId)))
            .thenReturn(Optional.of(expected));

        String body =
            restRelationTermControllerMockMvc.perform(get("/api/relation/term/{id}.json", relationTerm.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals(expected, objectMapper.readValue(body, TermRelationResponse.class));
    }

    @Test
    @Transactional
    public void getATermRelationNotFoundReturns404() throws Exception {
        long userId = builder.givenSuperAdmin().getId();
        when(termRelationHttpContract.findTermRelationByID(eq(999L), eq(userId))).thenReturn(Optional.empty());

        restRelationTermControllerMockMvc.perform(get("/api/relation/term/{id}.json", 999L))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void addTermRelation() throws Exception {
        RelationTerm relationTerm = builder.givenARelationTerm();
        long ontologyId = relationTerm.getTerm1().getOntology().getId();
        long userId = builder.givenSuperAdmin().getId();
        UUID commandId = UUID.randomUUID();
        CreateTermRelation createTermRelation = new CreateTermRelation(
            relationTerm.getTerm1().getId(),
            relationTerm.getTerm2().getId(), RelationTerm.PARENT
        );
        HttpCommandResponse expected = new HttpCommandResponse(
            true, buildResponse(relationTerm, ontologyId),
            commandId, Commands.CREATE_TERM_RELATION
        );
        when(termRelationHttpContract.create(eq(userId), eq(createTermRelation))).thenReturn(Optional.of(expected));

        String body = restRelationTermControllerMockMvc.perform(
                post("/api/relation/term.json").contentType(MediaType.APPLICATION_JSON)
                    .content(JsonObject.of(
                        "term1", relationTerm.getTerm1().getId(), "term2",
                        relationTerm.getTerm2().getId()
                    ).toJsonString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertEquals(expected, objectMapper.readValue(body, HttpCommandResponse.class));
    }

    @Test
    @Transactional
    public void addTermRelationWithNoWriteAccessReturnsEmpty() throws Exception {
        RelationTerm relationTerm = builder.givenARelationTerm();
        long userId = builder.givenSuperAdmin().getId();
        CreateTermRelation createTermRelation = new CreateTermRelation(
            relationTerm.getTerm1().getId(),
            relationTerm.getTerm2().getId(), RelationTerm.PARENT
        );
        when(termRelationHttpContract.create(eq(userId), eq(createTermRelation))).thenReturn(Optional.empty());

        String body = restRelationTermControllerMockMvc.perform(
                post("/api/relation/term.json").contentType(MediaType.APPLICATION_JSON)
                    .content(JsonObject.of(
                        "term1", relationTerm.getTerm1().getId(), "term2",
                        relationTerm.getTerm2().getId()
                    ).toJsonString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertNull(objectMapper.readValue(body, HttpCommandResponse.class));
    }

    @Test
    @Transactional
    public void editTermRelation() throws Exception {
        RelationTerm relationTerm = builder.givenARelationTerm();
        long ontologyId = relationTerm.getTerm1().getOntology().getId();
        long userId = builder.givenSuperAdmin().getId();
        UUID commandId = UUID.randomUUID();
        UpdateTermRelation updateTermRelation = new UpdateTermRelation(
            Optional.of(relationTerm.getTerm1().getId()),
            Optional.of(relationTerm.getTerm2().getId()), Optional.empty()
        );
        HttpCommandResponse expected = new HttpCommandResponse(
            true, buildResponse(relationTerm, ontologyId),
            commandId, Commands.UPDATE_TERM_RELATION
        );
        when(termRelationHttpContract.update(eq(relationTerm.getId()), eq(userId), eq(updateTermRelation)))
            .thenReturn(Optional.of(expected));

        String body = restRelationTermControllerMockMvc.perform(
                put("/api/relation/term/{id}.json", relationTerm.getId()).contentType(MediaType.APPLICATION_JSON)
                    .content(JsonObject.of(
                        "term1Id", relationTerm.getTerm1().getId(), "term2Id",
                        relationTerm.getTerm2().getId()
                    ).toJsonString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertEquals(expected, objectMapper.readValue(body, HttpCommandResponse.class));
    }

    @Test
    @Transactional
    public void editTermRelationWithNoWriteAccessReturnsNotFound() throws Exception {
        RelationTerm relationTerm = builder.givenARelationTerm();
        long userId = builder.givenSuperAdmin().getId();
        UpdateTermRelation updateTermRelation = new UpdateTermRelation(
            Optional.of(relationTerm.getTerm1().getId()),
            Optional.of(relationTerm.getTerm2().getId()), Optional.empty()
        );
        when(termRelationHttpContract.update(eq(relationTerm.getId()), eq(userId), eq(updateTermRelation)))
            .thenReturn(Optional.empty());

        restRelationTermControllerMockMvc.perform(
                put("/api/relation/term/{id}.json", relationTerm.getId()).contentType(MediaType.APPLICATION_JSON)
                    .content(JsonObject.of(
                        "term1Id", relationTerm.getTerm1().getId(), "term2Id",
                        relationTerm.getTerm2().getId()
                    ).toJsonString()))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void deleteTermRelation() throws Exception {
        RelationTerm relationTerm = builder.givenARelationTerm();
        long ontologyId = relationTerm.getTerm1().getOntology().getId();
        long userId = builder.givenSuperAdmin().getId();
        UUID commandId = UUID.randomUUID();
        HttpCommandResponse expected = new HttpCommandResponse(
            true, buildResponse(relationTerm, ontologyId),
            commandId, Commands.DELETE_TERM_RELATION
        );
        when(termRelationHttpContract.delete(eq(relationTerm.getId()), eq(userId))).thenReturn(Optional.of(expected));

        String body =
            restRelationTermControllerMockMvc.perform(delete("/api/relation/term/{id}.json", relationTerm.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals(expected, objectMapper.readValue(body, HttpCommandResponse.class));
    }

    @Test
    @Transactional
    public void deleteTermRelationWithNoDeleteAccessReturnsNotFound() throws Exception {
        RelationTerm relationTerm = builder.givenARelationTerm();
        long userId = builder.givenSuperAdmin().getId();
        when(termRelationHttpContract.delete(eq(relationTerm.getId()), eq(userId))).thenReturn(Optional.empty());

        restRelationTermControllerMockMvc.perform(delete("/api/relation/term/{id}.json", relationTerm.getId()))
            .andExpect(status().isNotFound());
    }
}
