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

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.TermHttpContract;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.ontology.Term;
import be.cytomine.domain.project.Project;
import be.cytomine.utils.JsonObject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
public class TermResourceTests {

    @Autowired
    private EntityManager em;

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private MockMvc restTermControllerMockMvc;

    @MockitoBean
    private TermHttpContract termHttpContract;

    @Test
    @Transactional
    public void list_all_terms() throws Exception {
        Term term = builder.given_a_term();
        when(termHttpContract.findAll()).thenReturn(Set.of());
        restTermControllerMockMvc.perform(get("/api/term.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.collection[?(@.name=='" + term.getName() + "')].ontology").value(term.getOntology().getId().intValue()));
    }


    @Test
    @Transactional
    public void get_a_term() throws Exception {
        Term term = builder.given_a_term();
        Term parent = builder.given_a_term(term.getOntology());
        builder.given_a_relation_term(parent, term);
        em.refresh(term);
        when(termHttpContract.findTermByID(anyLong())).thenReturn(Optional.empty());
        restTermControllerMockMvc.perform(get("/api/term/{id}.json", term.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(term.getId().intValue()))
                .andExpect(jsonPath("$.class").value("be.cytomine.domain.ontology.Term"))
                .andExpect(jsonPath("$.color").value(term.getColor()))
                .andExpect(jsonPath("$.created").isNotEmpty())
                .andExpect(jsonPath("$.ontology").value(term.getOntology().getId().intValue()))
                .andExpect(jsonPath("$.parent").value(parent.getId().intValue()))
        ;
    }

    @Test
    @Transactional
    public void list_terms_by_ontology() throws Exception {
        Term term = builder.given_a_term();
        when(termHttpContract.findTermsByOntology(anyLong())).thenReturn(Set.of());
        restTermControllerMockMvc.perform(get("/api/ontology/{id}/term.json", term.getOntology().getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.collection[?(@.name=='" + term.getName() + "')].ontology").value(term.getOntology().getId().intValue()));
    }

    @Test
    @Transactional
    public void list_terms_by_project() throws Exception {
        Term term = builder.given_a_term();
        Project project = builder.given_a_project_with_ontology(term.getOntology());
        when(termHttpContract.findTermsByProject(anyLong())).thenReturn(Set.of());
        restTermControllerMockMvc.perform(get("/api/project/{id}/term.json", project.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.collection[?(@.name=='" + term.getName() + "')].ontology").value(term.getOntology().getId().intValue()));
    }

    @Test
    @Transactional
    public void add_valid_term() throws Exception {
        Term term = BasicInstanceBuilder.given_a_not_persisted_term(builder.given_an_ontology());
        when(termHttpContract.update(any(be.cytomine.common.repository.model.CreateTerm.class))).thenReturn(null);
        restTermControllerMockMvc.perform(post("/api/term.json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(term.toJSON()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.printMessage").value(true))
                .andExpect(jsonPath("$.callback").exists())
                .andExpect(jsonPath("$.callback.termID").exists())
                .andExpect(jsonPath("$.callback.method").value("be.cytomine.AddTermCommand"))
                .andExpect(jsonPath("$.callback.ontologyID").value(String.valueOf(term.getOntology().getId())))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.command").exists())
                .andExpect(jsonPath("$.term.id").exists())
                .andExpect(jsonPath("$.term.name").value(term.getName()))
                .andExpect(jsonPath("$.term.ontology").value(term.getOntology().getId()));
    }

    @Test
    @Transactional
    public void add_valid_term_by_group() throws Exception {
        Term term1 = BasicInstanceBuilder.given_a_not_persisted_term(builder.given_an_ontology());
        Term term2 = BasicInstanceBuilder.given_a_not_persisted_term(term1.getOntology());
        when(termHttpContract.update(any(be.cytomine.common.repository.model.CreateTerm.class))).thenReturn(null);
        restTermControllerMockMvc.perform(post("/api/term.json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonObject.toJsonString(List.of(term1.toJsonObject(), term2.toJsonObject()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @Transactional
    public void add_term_refused_if_already_exists() throws Exception {
        Term term = BasicInstanceBuilder.given_a_not_persisted_term(builder.given_an_ontology());
        builder.persistAndReturn(term);
        when(termHttpContract.update(any(be.cytomine.common.repository.model.CreateTerm.class))).thenReturn(null);
        restTermControllerMockMvc.perform(post("/api/term.json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(term.toJSON()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").value("Term " + term.getName() + " already exist in this ontology!"));
    }

    @Test
    @Transactional
    public void add_term_refused_if_ontology_not_set() throws Exception {
        Term term = BasicInstanceBuilder.given_a_not_persisted_term(null);
        when(termHttpContract.update(any(be.cytomine.common.repository.model.CreateTerm.class))).thenReturn(null);
        restTermControllerMockMvc.perform(post("/api/term.json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(term.toJSON()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").value("Ontology is mandatory for term creation"));
    }

    @Test
    @Transactional
    public void add_term_refused_if_name_not_set() throws Exception {
        Term term = BasicInstanceBuilder.given_a_not_persisted_term(builder.given_an_ontology());
        term.setName(null);
        when(termHttpContract.update(any(be.cytomine.common.repository.model.CreateTerm.class))).thenReturn(null);
        restTermControllerMockMvc.perform(post("/api/term.json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(term.toJSON()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Transactional
    public void edit_valid_term() throws Exception {
        Term term = builder.given_a_term();
        when(termHttpContract.update(anyLong(), any(be.cytomine.common.repository.model.UpdateTerm.class))).thenReturn(null);
        restTermControllerMockMvc.perform(put("/api/term/{id}.json", term.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(term.toJSON()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.printMessage").value(true))
                .andExpect(jsonPath("$.callback").exists())
                .andExpect(jsonPath("$.callback.termID").exists())
                .andExpect(jsonPath("$.callback.method").value("be.cytomine.EditTermCommand"))
                .andExpect(jsonPath("$.callback.ontologyID").value(String.valueOf(term.getOntology().getId())))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.command").exists())
                .andExpect(jsonPath("$.term.id").exists())
                .andExpect(jsonPath("$.term.name").value(term.getName()))
                .andExpect(jsonPath("$.term.ontology").value(term.getOntology().getId()));

    }


    @Test
    @Transactional
    public void edit_term_not_exists_fails() throws Exception {
        Term term = builder.given_a_term();
        em.remove(term);
        when(termHttpContract.update(anyLong(), any(be.cytomine.common.repository.model.UpdateTerm.class))).thenReturn(null);
        restTermControllerMockMvc.perform(put("/api/term/{id}.json", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(term.toJSON()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").exists());

    }

    @Test
    @Transactional
    public void delete_term() throws Exception {
        Term term = builder.given_a_term();
        when(termHttpContract.delete(anyLong())).thenReturn(Optional.empty());
        restTermControllerMockMvc.perform(delete("/api/term/{id}.json", term.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(term.toJSON()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.printMessage").value(true))
                .andExpect(jsonPath("$.callback").exists())
                .andExpect(jsonPath("$.callback.termID").exists())
                .andExpect(jsonPath("$.callback.method").value("be.cytomine.DeleteTermCommand"))
                .andExpect(jsonPath("$.callback.ontologyID").value(String.valueOf(term.getOntology().getId())))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.command").exists())
                .andExpect(jsonPath("$.term.id").exists())
                .andExpect(jsonPath("$.term.name").value(term.getName()))
                .andExpect(jsonPath("$.term.ontology").value(term.getOntology().getId()));

    }

    @Test
    @Transactional
    public void delete_term_not_exist_fails() throws Exception {
        Term term = builder.given_a_term();
        when(termHttpContract.delete(anyLong())).thenReturn(Optional.empty());
        restTermControllerMockMvc.perform(delete("/api/term/{id}.json", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(term.toJSON()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").exists());

    }
}
