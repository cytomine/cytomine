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
import be.cytomine.domain.ontology.Track;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
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
public class TrackResourceTests {

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private MockMvc restTrackControllerMockMvc;

    @Test
    @Transactional
    public void listTracksByImageinstance() throws Exception {
        Track track = builder.givenATrack();
        restTrackControllerMockMvc.perform(get("/api/imageinstance/{id}/track.json", track.getImage().getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.collection[?(@.name=='" + track.getName() + "')]").exists());
    }

    @Test
    @Transactional
    public void listTracksByImageinstanceNotExists() throws Exception {
        restTrackControllerMockMvc.perform(get("/api/imageinstance/{id}/track.json", 0))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void listTracksByProject() throws Exception {
        Track track = builder.givenATrack();
        restTrackControllerMockMvc.perform(get("/api/project/{id}/track.json", track.getProject().getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.collection[?(@.name=='" + track.getName() + "')]").exists());
    }

    @Test
    @Transactional
    public void listTracksByProjectNotExists() throws Exception {
        restTrackControllerMockMvc.perform(get("/api/project/{id}/track.json", 0))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void shouldReturnTrack() throws Exception {
        Track track = builder.givenATrack();
        restTrackControllerMockMvc.perform(get("/api/track/{id}.json", track.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(track.getId().intValue()))
            .andExpect(jsonPath("$.class").value("be.cytomine.domain.ontology.Track"))
            .andExpect(jsonPath("$.color").value(track.getColor()))
            .andExpect(jsonPath("$.created").isNotEmpty());
    }

    @Test
    @Transactional
    public void getAnUnexistingTrack() throws Exception {
        restTrackControllerMockMvc.perform(get("/api/track/{id}.json", 0))
            .andExpect(status().isNotFound());
    }


    @Test
    @Transactional
    public void addValidTrack() throws Exception {
        Track track = builder.givenANotPersistedTrack();
        restTrackControllerMockMvc.perform(post("/api/track.json")
                .contentType(MediaType.APPLICATION_JSON)
                .content(track.toJSON()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.callback").exists())
            .andExpect(jsonPath("$.callback.trackID").exists())
            .andExpect(jsonPath("$.callback.method").value("be.cytomine.AddTrackCommand"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.command").exists())
            .andExpect(jsonPath("$.track.id").exists())
            .andExpect(jsonPath("$.track.name").value(track.getName()));
    }

    @Test
    @Transactional
    public void editValidTrack() throws Exception {
        Track track = builder.givenATrack();
        restTrackControllerMockMvc.perform(put("/api/track/{id}.json", track.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(track.toJSON()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.callback").exists())
            .andExpect(jsonPath("$.callback.trackID").exists())
            .andExpect(jsonPath("$.callback.method").value("be.cytomine.EditTrackCommand"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.command").exists())
            .andExpect(jsonPath("$.track.id").exists())
            .andExpect(jsonPath("$.track.name").value(track.getName()));

    }

    @Test
    @Transactional
    public void deleteTrack() throws Exception {
        Track track = builder.givenATrack();
        restTrackControllerMockMvc.perform(delete("/api/track/{id}.json", track.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(track.toJSON()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.callback").exists())
            .andExpect(jsonPath("$.callback.trackID").exists())
            .andExpect(jsonPath("$.callback.method").value("be.cytomine.DeleteTrackCommand"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.command").exists())
            .andExpect(jsonPath("$.track.id").exists())
            .andExpect(jsonPath("$.track.name").value(track.getName()));

    }
}
