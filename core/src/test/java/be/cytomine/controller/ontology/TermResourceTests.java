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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.TermHttpContract;
import be.cytomine.common.repository.model.TermResponse;
import be.cytomine.common.repository.model.CreateTerm;
import be.cytomine.common.repository.model.UpdateTerm;
import be.cytomine.common.repository.model.command.Callback;
import be.cytomine.common.repository.model.command.HttpCommandResponse;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.ontology.Term;
import be.cytomine.domain.project.Project;
import be.cytomine.utils.JsonObject;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
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
public class TermResourceTests {

    @Autowired
    private BasicInstanceBuilder basicInstanceBuilder;

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private MockMvc restTermControllerMockMvc;

    @MockitoBean
    private TermHttpContract termHttpContract;

    private TermResponse toTermResponse(Term term) {
        return new TermResponse(term.getId(), term.getName(), term.getColor(),
            term.getOntology().getId(), term.getOntology().getId(),
            term.getCreated(), term.getUpdated(), term.getComment(), Set.of());
    }

    @Test
    @Transactional
    public void list_all_terms() throws Exception {
        Term term = builder.given_a_term();
        when(termHttpContract.findAll(any(Pageable.class))).thenReturn(
            new PageImpl<>(List.of(toTermResponse(term))));

        restTermControllerMockMvc.perform(get("/api/term.json")).andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThan(0)))).andExpect(
                jsonPath("$.collection[?(@.name=='" + term.getName() + "')].ontology").value(
                    term.getOntology().getId().intValue()));
    }


    @Test
    @Transactional
    public void get_a_term() throws Exception {
        Term term = builder.given_a_term();
        when(termHttpContract.findTermByID(term.getId())).thenReturn(
            Optional.of(toTermResponse(term)));

        restTermControllerMockMvc.perform(get("/api/term/{id}.json", term.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(term.getId().intValue()))
            .andExpect(jsonPath("$.class").value("be.cytomine.domain.ontology.Term"))
            .andExpect(jsonPath("$.color").value(term.getColor()))
            .andExpect(jsonPath("$.created").isNotEmpty())
            .andExpect(jsonPath("$.ontology").value(term.getOntology().getId().intValue()));
    }

    @Test
    @Transactional
    public void list_terms_by_ontology() throws Exception {
        Term term = builder.given_a_term();
        when(termHttpContract.findTermsByOntology(eq(term.getOntology().getId()),
            any(Pageable.class))).thenReturn(
            new PageImpl<>(List.of(toTermResponse(term))));

        restTermControllerMockMvc.perform(
                get("/api/ontology/{id}/term.json", term.getOntology().getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
            .andExpect(
                jsonPath("$.collection[?(@.name=='" + term.getName() + "')].ontology").value(
                    term.getOntology().getId().intValue()));
    }

    @Test
    @Transactional
    public void list_terms_by_project() throws Exception {
        Term term = builder.given_a_term();
        Project project = builder.given_a_project_with_ontology(term.getOntology());
        when(termHttpContract.findTermsByProject(eq(project.getId()),
            any(Pageable.class))).thenReturn(
            new PageImpl<>(List.of(toTermResponse(term))));

        restTermControllerMockMvc.perform(get("/api/project/{id}/term.json", project.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
            .andExpect(
                jsonPath("$.collection[?(@.name=='" + term.getName() + "')].ontology").value(
                    term.getOntology().getId().intValue()));
    }

    @Test
    @Transactional
    public void add_valid_term() throws Exception {
        Term term = basicInstanceBuilder.given_a_not_persisted_term(builder.given_an_ontology());
        long userId = builder.given_superadmin().getId();
        CreateTerm createTerm = new CreateTerm(term.getName(), term.getColor(),
            term.getOntology().getId(), term.getCreated(), term.getUpdated(), term.getComment());

        HttpCommandResponse<TermResponse> response =
            new HttpCommandResponse<>("",
                new Callback("be.cytomine.AddTermCommand", 1,
                    term.getOntology().getId()), true,
                new TermResponse(1, term.getName(), term.getColor(),
                    term.getOntology().getId(), term.getOntology().getId(),
                    term.getCreated(), term.getUpdated(), term.getComment(), Set.of()), -1);
        when(termHttpContract.create(eq(userId), eq(createTerm))).thenReturn(Optional.of(response));

        restTermControllerMockMvc.perform(
                post("/api/term.json").contentType(MediaType.APPLICATION_JSON).content(term.toJSON()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.callback").exists())
            .andExpect(jsonPath("$.callback.termID").exists())
            .andExpect(jsonPath("$.callback.method").value("be.cytomine.AddTermCommand"))
            .andExpect(
                jsonPath("$.callback.ontologyID").value(String.valueOf(term.getOntology().getId())))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.command").exists())
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.name").value(term.getName()))
            .andExpect(jsonPath("$.data.ontology").value(term.getOntology().getId()));
    }

    @Test
    @Transactional
    public void add_valid_term_by_group() throws Exception {
        Term term1 = basicInstanceBuilder.given_a_not_persisted_term(builder.given_an_ontology());
        Term term2 = basicInstanceBuilder.given_a_not_persisted_term(term1.getOntology());
        long userId = builder.given_superadmin().getId();
        CreateTerm createTerm1 = new CreateTerm(term1.getName(), term1.getColor(),
            term1.getOntology().getId(), term1.getCreated(), term1.getUpdated(), term1.getComment());
        CreateTerm createTerm2 = new CreateTerm(term2.getName(), term2.getColor(),
            term2.getOntology().getId(), term2.getCreated(), term2.getUpdated(), term2.getComment());

        HttpCommandResponse<TermResponse> response =
            new HttpCommandResponse<>("", new Callback("be.cytomine.DeleteTermCommand",
                term1.getId(), term1.getOntology().getId()), true,
                toTermResponse(term1), -1);

        when(termHttpContract.create(eq(userId), eq(createTerm1))).thenReturn(Optional.of(response));
        when(termHttpContract.create(eq(userId), eq(createTerm2))).thenReturn(Optional.of(response));

        restTermControllerMockMvc.perform(
                post("/api/term.json").contentType(MediaType.APPLICATION_JSON)
                    .content(
                        JsonObject.toJsonString(List.of(term1.toJsonObject(),
                            term2.toJsonObject()))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @Transactional
    public void add_term_refused_if_already_exists() throws Exception {
        Term term = basicInstanceBuilder.given_a_not_persisted_term(builder.given_an_ontology());
        builder.persistAndReturn(term);
        long userId = builder.given_superadmin().getId();
        CreateTerm createTerm = new CreateTerm(term.getName(), term.getColor(),
            term.getOntology().getId(), term.getCreated(), term.getUpdated(), term.getComment());
        when(termHttpContract.create(eq(userId), eq(createTerm))).thenReturn(Optional.empty());

        restTermControllerMockMvc.perform(
                post("/api/term.json").contentType(MediaType.APPLICATION_JSON).content(term.toJSON()))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errors").value(
                "Term " + term.getName() + " already exist in this ontology!"));
    }

    @Test
    @Transactional
    public void add_term_refused_if_ontology_not_set() throws Exception {
        Term term = basicInstanceBuilder.given_a_not_persisted_term(null);
        restTermControllerMockMvc.perform(
                post("/api/term.json").contentType(MediaType.APPLICATION_JSON).content(term.toJSON()))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errors").value("Ontology is mandatory for term creation"));
    }

    @Test
    @Transactional
    public void add_term_refused_if_name_not_set() throws Exception {
        CreateTerm term =
            basicInstanceBuilder.given_a_not_persisted_create_term(builder.given_an_ontology(),
                null);
        long userId = builder.given_superadmin().getId();
        when(termHttpContract.create(eq(userId), eq(term))).thenThrow(new ResponseStatusException(
            BAD_REQUEST));
        restTermControllerMockMvc.perform(
                post("/api/term.json").contentType(MediaType.APPLICATION_JSON).content(
                    String.valueOf(term)))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Transactional
    public void edit_valid_term() throws Exception {
        Term term = builder.given_a_term();
        long userId = builder.given_superadmin().getId();
        UpdateTerm updateTerm = new UpdateTerm(Optional.of(term.getName()), Optional.of(term.getColor()));
        HttpCommandResponse<TermResponse> response =
            new HttpCommandResponse<>("",
                new Callback("be.cytomine.DeleteTermCommand", term.getId(),
                    term.getOntology().getId()), true,
                toTermResponse(term), -1);
        when(termHttpContract.update(eq(term.getId()), eq(userId), eq(updateTerm))).thenReturn(Optional.of(response));

        restTermControllerMockMvc.perform(
                put("/api/term/{id}.json", term.getId()).contentType(MediaType.APPLICATION_JSON)
                    .content(term.toJSON()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.callback").exists())
            .andExpect(jsonPath("$.callback.termID").exists())
            .andExpect(jsonPath("$.callback.method").value("be.cytomine.EditTermCommand"))
            .andExpect(
                jsonPath("$.callback.ontologyID").value(String.valueOf(term.getOntology().getId())))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.command").exists())
            .andExpect(jsonPath("$.term.id").exists())
            .andExpect(jsonPath("$.term.name").value(term.getName()))
            .andExpect(jsonPath("$.term.ontology").value(term.getOntology().getId()));

    }


    @Test
    @Transactional
    public void edit_term_not_exists_fails() throws Exception {
        long userId = builder.given_superadmin().getId();
        UpdateTerm updateTerm = new UpdateTerm(Optional.empty(), Optional.empty());
        when(termHttpContract.update(eq(0L), eq(userId), eq(updateTerm))).thenReturn(null);

        restTermControllerMockMvc.perform(
                put("/api/term/{id}.json", 0).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errors").exists());

    }

    @Test
    @Transactional
    public void delete_term() throws Exception {
        Term term = builder.given_a_term();
        long userId = builder.given_superadmin().getId();
        HttpCommandResponse<TermResponse> response =
            new HttpCommandResponse<>("",
                new Callback("be.cytomine.DeleteTermCommand", term.getId(),
                    term.getOntology().getId()), true,
                toTermResponse(term), -1);
        when(termHttpContract.delete(eq(term.getId()), eq(userId))).thenReturn(
            Optional.of(response));

        restTermControllerMockMvc.perform(
                delete("/api/term/{id}.json", term.getId()).contentType(MediaType.APPLICATION_JSON)
                    .content(term.toJSON()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.callback").exists())
            .andExpect(jsonPath("$.callback.termID").exists())
            .andExpect(jsonPath("$.callback.method").value("be.cytomine.DeleteTermCommand"))
            .andExpect(
                jsonPath("$.callback.ontologyId").value(String.valueOf(term.getOntology().getId())))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.command").exists())
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.name").value(term.getName()))
            .andExpect(jsonPath("$.data.ontologyId").value(term.getOntology().getId()));

    }

    @Test
    @Transactional
    public void delete_term_not_exist_fails() throws Exception {
        long userId = builder.given_superadmin().getId();
        when(termHttpContract.delete(eq(0L), eq(userId))).thenReturn(Optional.empty());

        restTermControllerMockMvc.perform(
                delete("/api/term/{id}.json", 0).contentType(MediaType.APPLICATION_JSON).content(
                    "{}"))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errors").exists());

    }
}
