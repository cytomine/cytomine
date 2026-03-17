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

import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.TermHttpContract;
import be.cytomine.common.repository.model.CreateTerm;
import be.cytomine.common.repository.model.TermResponse;
import be.cytomine.common.repository.model.UpdateTerm;
import be.cytomine.config.MongoTestConfiguration;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private MockMvc restTermControllerMockMvc;

    @MockitoBean
    private TermHttpContract termHttpContract;

    @Test
    public void list_all_terms() throws Exception {
        TermResponse term1 = new TermResponse(1L, "Cell", "#FF0000", 100L, 200L, Set.of());
        TermResponse term2 = new TermResponse(2L, "Nucleus", "#00FF00", 100L, 200L, Set.of());
        Page<TermResponse> page = new PageImpl<>(List.of(term1, term2));
        when(termHttpContract.findAll(any(Pageable.class))).thenReturn(page);

        restTermControllerMockMvc.perform(get("/api/term.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection", hasSize(2)))
                .andExpect(jsonPath("$.collection[?(@.name=='Cell')].id").value(1))
                .andExpect(jsonPath("$.collection[?(@.name=='Nucleus')].id").value(2))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    public void list_all_terms_empty() throws Exception {
        Page<TermResponse> emptyPage = Page.empty();
        when(termHttpContract.findAll(any(Pageable.class))).thenReturn(emptyPage);

        restTermControllerMockMvc.perform(get("/api/term.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection", hasSize(0)))
                .andExpect(jsonPath("$.size").value(0));
    }

    @Test
    public void get_a_term() throws Exception {
        TermResponse child = new TermResponse(2L, "Mitochondria", "#0000FF", 100L, 200L, Set.of());
        TermResponse term = new TermResponse(1L, "Cell", "#FF0000", 100L, 200L, Set.of(child));
        when(termHttpContract.findTermByID(1L)).thenReturn(Optional.of(term));

        restTermControllerMockMvc.perform(get("/api/term/{id}.json", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Cell"))
                .andExpect(jsonPath("$.color").value("#FF0000"))
                .andExpect(jsonPath("$.ontologyId").value(100))
                .andExpect(jsonPath("$.children", hasSize(1)))
                .andExpect(jsonPath("$.children[0].name").value("Mitochondria"));
    }

    @Test
    public void get_a_term_not_found() throws Exception {
        when(termHttpContract.findTermByID(999L)).thenReturn(Optional.empty());

        restTermControllerMockMvc.perform(get("/api/term/{id}.json", 999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    public void list_terms_by_ontology() throws Exception {
        TermResponse term = new TermResponse(1L, "Cell", "#FF0000", 100L, 200L, Set.of());
        Page<TermResponse> page = new PageImpl<>(List.of(term));
        when(termHttpContract.findTermsByOntology(eq(100L), any(Pageable.class))).thenReturn(page);

        restTermControllerMockMvc.perform(get("/api/ontology/{id}/term.json", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection", hasSize(1)))
                .andExpect(jsonPath("$.collection[0].name").value("Cell"))
                .andExpect(jsonPath("$.collection[0].ontologyId").value(100))
                .andExpect(jsonPath("$.size").value(1));
    }

    @Test
    public void list_terms_by_project() throws Exception {
        TermResponse term = new TermResponse(1L, "Cell", "#FF0000", 100L, 200L, Set.of());
        Page<TermResponse> page = new PageImpl<>(List.of(term));
        when(termHttpContract.findTermsByProject(eq(200L), any(Pageable.class))).thenReturn(page);

        restTermControllerMockMvc.perform(get("/api/project/{id}/term.json", 200L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection", hasSize(1)))
                .andExpect(jsonPath("$.collection[0].name").value("Cell"))
                .andExpect(jsonPath("$.collection[0].projectId").value(200))
                .andExpect(jsonPath("$.size").value(1));
    }

    @Test
    public void create_term() throws Exception {
        TermResponse createdTerm = new TermResponse(1L, "NewTerm", "#123456", 100L, 200L, Set.of());
        when(termHttpContract.update(any(CreateTerm.class))).thenReturn(createdTerm);

        String json = """
                {"name": "NewTerm", "color": "#123456", "ontology": 100, "project": 200}
                """;

        restTermControllerMockMvc.perform(post("/api/term.json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("NewTerm"))
                .andExpect(jsonPath("$.color").value("#123456"));
    }

    @Test
    public void update_term() throws Exception {
        TermResponse updatedTerm = new TermResponse(1L, "UpdatedName", "#ABCDEF", 100L, 200L, Set.of());
        when(termHttpContract.update(eq(1L), any(UpdateTerm.class))).thenReturn(updatedTerm);

        String json = """
                {"name": "UpdatedName", "color": "#ABCDEF"}
                """;

        restTermControllerMockMvc.perform(put("/api/term/{id}.json", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("UpdatedName"))
                .andExpect(jsonPath("$.color").value("#ABCDEF"));
    }

    @Test
    public void delete_term() throws Exception {
        TermResponse deletedTerm = new TermResponse(1L, "DeletedTerm", "#FF0000", 100L, 200L, Set.of());
        when(termHttpContract.delete(1L)).thenReturn(Optional.of(deletedTerm));

        restTermControllerMockMvc.perform(delete("/api/term/{id}.json", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("DeletedTerm"));
    }

    @Test
    public void delete_term_not_found() throws Exception {
        when(termHttpContract.delete(999L)).thenReturn(Optional.empty());

        restTermControllerMockMvc.perform(delete("/api/term/{id}.json", 999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }
}
