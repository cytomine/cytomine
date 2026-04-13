package be.cytomine.controller;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.config.properties.ApplicationProperties;
import be.cytomine.domain.project.Project;
import be.cytomine.repositorynosql.social.LastConnectionRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION;
import static org.springframework.security.acls.domain.BasePermission.READ;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
public class CustomUIControllerTests {

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private MockMvc restConfigurationControllerMockMvc;

    @Autowired
    private LastConnectionRepository lastConnectionRepository;

    @Autowired
    private ApplicationProperties applicationProperties;

    @BeforeEach
    public void before() {
        lastConnectionRepository.deleteAll();
    }

    @Test
    @Transactional
    @WithMockUser(username = "user")
    public void load_custom_ui_default_config() {
        assertThat(applicationProperties.getCustomUI()
            .getProject()
            .get("project-images-tab")
            .get("ADMIN_PROJECT")).isEqualTo(true);
        System.out.println(applicationProperties.getCustomUI().getProject().get("project-annotations-tab"));
        assertThat(applicationProperties.getCustomUI()
            .getProject()
            .get("project-annotations-tab")
            .get("ADMIN_PROJECT")).isEqualTo(true);
    }

    @Test
    @Transactional
    @WithMockUser(username = "superadmin")
    public void retrieve_global_custom_ui_as_superadmin() throws Exception {
        restConfigurationControllerMockMvc.perform(get("/api/custom-ui/config.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activity").value(true))
            .andExpect(jsonPath("$.admin").value(true))
            .andExpect(jsonPath("$.explore").value(true))
            .andExpect(jsonPath("$.feedback").value(true))
            .andExpect(jsonPath("$.feedback").value(true))
            .andExpect(jsonPath("$.help").value(true))
            .andExpect(jsonPath("$.ontology").value(true))
            .andExpect(jsonPath("$.project").value(true))
            .andExpect(jsonPath("$.search").value(true))
            .andExpect(jsonPath("$.storage").value(true));
    }

    @Test
    @Transactional
    @WithMockUser(username = "user")
    public void retrieve_global_custom_ui_as_user() throws Exception {
        restConfigurationControllerMockMvc.perform(get("/api/custom-ui/config.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activity").value(true))
            .andExpect(jsonPath("$.admin").value(false))
            .andExpect(jsonPath("$.explore").value(true))
            .andExpect(jsonPath("$.feedback").value(true))
            .andExpect(jsonPath("$.help").value(true))
            .andExpect(jsonPath("$.ontology").value(false))
            .andExpect(jsonPath("$.project").value(true))
            .andExpect(jsonPath("$.search").value(false))
            .andExpect(jsonPath("$.storage").value(true));
    }

    @Test
    @Transactional
    @WithMockUser(username = "superadmin")
    public void retrieve_project_custom_ui() throws Exception {
        Project project = builder.givenAProject();
        restConfigurationControllerMockMvc.perform(get("/api/custom-ui/config.json")
                .param("project", project.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.admin").value(true)) //1 global
            .andExpect(jsonPath("$.project-images-tab").value(true)); // 1project
    }

    @Test
    @Transactional
    @WithMockUser(username = "user")
    public void retrieve_project_custom_ui_as_contributor() throws Exception {
        Project project = builder.givenAProject();
        builder.addUserToProject(project, "user", READ);
        restConfigurationControllerMockMvc.perform(get("/api/custom-ui/config.json")
                .param("project", project.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.project-jobs-tab").value(false));
    }

    @Test
    @Transactional
    @WithMockUser(username = "user")
    public void retrieve_project_custom_ui_as_manager() throws Exception {
        Project project = builder.givenAProject();
        builder.addUserToProject(project, "user", ADMINISTRATION);
        restConfigurationControllerMockMvc.perform(get("/api/custom-ui/config.json")
                .param("project", project.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.project-jobs-tab").value(false));
    }


    @Test
    @Transactional
    @WithMockUser(username = "superadmin")
    public void retrieve_project_custom_ui_as_superadmin() throws Exception {
        Project project = builder.givenAProject();
        restConfigurationControllerMockMvc.perform(get("/api/custom-ui/project/{project}.json", project.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.project-images-tab.ADMIN_PROJECT").value(true))
            .andExpect(jsonPath("$.project-explore-hide-tools.ADMIN_PROJECT").value(true))
            .andExpect(jsonPath("$.project-jobs-tab.CONTRIBUTOR_PROJECT").value(false));
    }


    @Test
    @Transactional
    @WithMockUser(username = "superadmin")
    public void change_project_custom_ui() throws Exception {
        Project project = builder.givenAProject();

        String customUI = """
            {
               "project-images-tab":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-annotations-tab":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-jobs-tab":{
                  "ADMIN_PROJECT":false,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-activities-tab":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":false
               },
               "project-information-tab":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-configuration-tab":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":false
               },
               "project-explore-image-overview":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-image-status":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-image-description":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-image-tags":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-image-properties":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-image-attached-files":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-image-slide-preview":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-image-original-filename":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-image-format":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-image-vendor":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-image-size":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-image-resolution":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-image-magnification":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-hide-tools":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-overview":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-info":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-digital-zoom":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-link":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-color-manipulation":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-image-layers":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-ontology":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-review":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-job":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-property":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-follow":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-guided-tour":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-annotation-main":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-annotation-geometry-info":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-annotation-info":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-annotation-comments":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-annotation-preview":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-annotation-properties":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-annotation-description":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-annotation-panel":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-annotation-terms":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-annotation-tags":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-annotation-attached-files":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-explore-annotation-creation-info":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-tools-main":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-tools-select":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-tools-point":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-tools-line":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-tools-freehand-line":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-tools-arrow":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-tools-rectangle":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-tools-diamond":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-tools-circle":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-tools-polygon":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-tools-freehand-polygon":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-tools-magic-wand":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-tools-freehand":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-tools-union":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-tools-diff":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-tools-fill":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-tools-rule":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-tools-edit":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-tools-resize":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-tools-rotate":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-tools-move":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-tools-delete":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-tools-screenshot":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-tools-undo-redo":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-annotations-term-piegraph":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-annotations-term-bargraph":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-annotations-users-graph":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-annotated-slides-term-graph":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-annotated-slides-users-graph":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-annotation-graph":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-users-global-activities-graph":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":true
               },
               "project-users-heatmap-graph":{
                  "ADMIN_PROJECT":true,
                  "CONTRIBUTOR_PROJECT":false
               }
            }""";

        restConfigurationControllerMockMvc.perform(post("/api/custom-ui/project/{project}.json", project.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(customUI))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.project-users-heatmap-graph.ADMIN_PROJECT").value(true))
            .andExpect(jsonPath("$.project-users-heatmap-graph.CONTRIBUTOR_PROJECT").value(false));

        // re save
        restConfigurationControllerMockMvc.perform(post("/api/custom-ui/project/{project}.json", project.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(customUI))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.project-users-heatmap-graph.ADMIN_PROJECT").value(true))
            .andExpect(jsonPath("$.project-users-heatmap-graph.CONTRIBUTOR_PROJECT").value(false));
    }
}
