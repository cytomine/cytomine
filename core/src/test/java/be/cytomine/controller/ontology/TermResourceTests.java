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

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.TermHttpContract;
import be.cytomine.common.repository.model.TermResponse;
import be.cytomine.common.repository.model.command.HttpCommandResponse;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.ontology.Term;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@DirtiesContext
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

    private HttpCommandResponse<TermResponse> toHttpTermResponse(Term term) {
        return new HttpCommandResponse<>("", null, false, toTermResponse(term), -1);
    }

    private TermResponse toTermResponse(Term term) {
        return new TermResponse(term.getId(), term.getName(), term.getColor(), term.getOntology().getId(),
            term.getCreated(), term.getUpdated(), term.getComment(), Set.of());
    }

    @Test
    @Transactional
    public void listAllTerms() throws Exception {
        Term term = builder.given_a_term();
        Page<TermResponse> page = new PageImpl<>(List.of(toTermResponse(term)));
        when(termHttpContract.findAll(any(Pageable.class))).thenReturn(page);

        restTermControllerMockMvc.perform(get("/api/term.json")).andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThan(0)))).andExpect(
                jsonPath("$.collection[?(@.name=='" + term.getName() + "')].ontologyId").value(
                    term.getOntology().getId().intValue()));
    }

    @Test
    @Transactional
    public void getATerm() throws Exception {
        Term term = builder.given_a_term();
        when(termHttpContract.findTermByID(term.getId())).thenReturn(Optional.of(toTermResponse(term)));

        restTermControllerMockMvc.perform(get("/api/term/{id}.json", term.getId())).andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(term.getId().intValue()))
            .andExpect(jsonPath("$.color").value(term.getColor()))
            .andExpect(jsonPath("$.ontologyId").value(term.getOntology().getId().intValue()));
    }

    @Test
    @Transactional
    public void listTermsByOntology() throws Exception {
        Term term = builder.given_a_term();
        Page<TermResponse> page = new PageImpl<>(List.of(toTermResponse(term)));
        when(termHttpContract.findTermsByOntology(eq(term.getOntology().getId()), any(Pageable.class))).thenReturn(
            page);

        restTermControllerMockMvc.perform(get("/api/ontology/{id}/term.json", term.getOntology().getId()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.collection", hasSize(greaterThan(0)))).andExpect(
                jsonPath("$.collection[?(@.name=='" + term.getName() + "')].ontologyId").value(
                    term.getOntology().getId().intValue()));
    }

    @Test
    @Transactional
    public void listTermsByProject() throws Exception {
        Term term = builder.given_a_term();
        Project project = builder.given_a_project_with_ontology(term.getOntology());
        Page<TermResponse> page = new PageImpl<>(List.of(toTermResponse(term)));
        when(termHttpContract.findTermsByProject(eq(project.getId()), any(Pageable.class))).thenReturn(page);

        restTermControllerMockMvc.perform(get("/api/project/{id}/term.json", project.getId()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.collection", hasSize(greaterThan(0)))).andExpect(
                jsonPath("$.collection[?(@.name=='" + term.getName() + "')].ontologyId").value(
                    term.getOntology().getId().intValue()));
    }

    @Test
    @Transactional
    public void addValidTerm() throws Exception {
        Term term = basicInstanceBuilder.given_a_not_persisted_term(builder.given_an_ontology());
        TermResponse response =
            new TermResponse(1L, term.getName(), term.getColor(), term.getOntology().getId(), term.getCreated(),
                term.getUpdated(), term.getComment(), Set.of());
        when(termHttpContract.update(any())).thenReturn(response);

        restTermControllerMockMvc.perform(post("/api/term.json").contentType(MediaType.APPLICATION_JSON).content(
                "{\"name\":\"" + term.getName() + "\",\"color\":\"" + term.getColor() + "\",\"ontology\":" +
                    term.getOntology().getId() + ",\"project\":0}")).andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists()).andExpect(jsonPath("$.name").value(term.getName()))
            .andExpect(jsonPath("$.ontologyId").value(term.getOntology().getId()));
    }

    @Test
    @Transactional
    public void editValidTerm() throws Exception {
        Term term = builder.given_a_term();
        TermResponse response = toTermResponse(term);
        when(termHttpContract.update(eq(term.getId()), any())).thenReturn(response);

        restTermControllerMockMvc.perform(
                put("/api/term/{id}.json", term.getId()).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"" + term.getName() + "\",\"color\":\"" + term.getColor() + "\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(term.getId().intValue()))
            .andExpect(jsonPath("$.name").value(term.getName()))
            .andExpect(jsonPath("$.ontologyId").value(term.getOntology().getId()));
    }

    @Test
    @Transactional
    public void deleteTerm() throws Exception {
        Term term = builder.given_a_term();
        User user = builder.given_a_user();
        when(termHttpContract.delete(term.getId(), user.getId())).thenReturn(Optional.of(toHttpTermResponse(term)));

        restTermControllerMockMvc.perform(delete("/api/term/{id}.json?user={}", term.getId(),user.getId())).andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(term.getId().intValue()))
            .andExpect(jsonPath("$.name").value(term.getName()))
            .andExpect(jsonPath("$.ontologyId").value(term.getOntology().getId()));
    }

    @Test
    @Transactional
    public void deleteTermNotExistReturnsEmpty() throws Exception {
        User user = builder.given_a_user();
        when(termHttpContract.delete(0L, user.getId())).thenReturn(Optional.empty());

        restTermControllerMockMvc.perform(delete("/api/term/{id}.json", 0)).andExpect(status().isOk())
            .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    @Transactional
    public void getTermNotExistReturnsEmpty() throws Exception {
        when(termHttpContract.findTermByID(0L)).thenReturn(Optional.empty());

        restTermControllerMockMvc.perform(get("/api/term/{id}.json", 0)).andExpect(status().isOk())
            .andExpect(jsonPath("$").doesNotExist());
    }
}
