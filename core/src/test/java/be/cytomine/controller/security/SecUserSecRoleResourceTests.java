package be.cytomine.controller.security;

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

import jakarta.persistence.EntityManager;
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
import be.cytomine.domain.security.SecUserSecRole;
import be.cytomine.domain.security.User;
import be.cytomine.repository.security.SecRoleRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
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
public class SecUserSecRoleResourceTests {

    @Autowired
    private EntityManager em;

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private MockMvc restSecUserSecRoleControllerMockMvc;

    @Autowired
    private SecRoleRepository secRoleRepository;

    @MockitoBean
    private TermRelationHttpContract termRelationHttpContract;

    @Test
    @Transactional
    public void list_roles() throws Exception {

        restSecUserSecRoleControllerMockMvc.perform(get(
                "/api/user/{user}/role.json",
                builder.given_superadmin().getId()
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$.collection[?(@.authority=='ROLE_SUPER_ADMIN')]").exists());
    }

    @Test
    @Transactional
    public void list_highest_roles() throws Exception {

        restSecUserSecRoleControllerMockMvc.perform(get(
                "/api/user/{user}/role.json",
                builder.given_superadmin().getId()
            )
                .param("highest", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(1)))
            .andExpect(jsonPath("$.collection[?(@.authority=='ROLE_SUPER_ADMIN')]").exists());
    }

    @Test
    @Transactional
    public void get_roles() throws Exception {
        restSecUserSecRoleControllerMockMvc.perform(get(
                "/api/user/{user}/role/{role}.json",
                builder.given_superadmin().getId(), secRoleRepository.getSuperAdmin().getId()
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.authority").value("ROLE_SUPER_ADMIN"));
    }

    @Test
    @Transactional
    public void get_role_with_unexisting_user() throws Exception {
        restSecUserSecRoleControllerMockMvc.perform(get(
                "/api/user/{user}/role/{role}.json",
                builder.given_superadmin().getId(), 0L
            ))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void get_role_with_unexisting_role() throws Exception {
        restSecUserSecRoleControllerMockMvc.perform(get(
                "/api/user/{user}/role/{role}.json",
                0L, secRoleRepository.getSuperAdmin().getId()
            ))
            .andExpect(status().isNotFound());
    }


    @Test
    @Transactional
    public void add_valid_role() throws Exception {
        User user = builder.given_a_user();
        SecUserSecRole secSecUserSecRole = builder.given_a_not_persisted_user_role(user, secRoleRepository.getAdmin());
        restSecUserSecRoleControllerMockMvc.perform(post("/api/user/{user}/role.json", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(secSecUserSecRole.toJSON()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.callback").exists())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.command").exists());
    }


    @Test
    @Transactional
    public void delete_user_role() throws Exception {
        User user = builder.given_a_user();
        SecUserSecRole secSecUserSecRole = builder.given_a_not_persisted_user_role(user, secRoleRepository.getAdmin());
        builder.persistAndReturn(secSecUserSecRole);
        restSecUserSecRoleControllerMockMvc.perform(delete(
                "/api/user/{user}/role/{role}.json",
                user.getId(),
                secSecUserSecRole.getSecRole().getId()
            )
                .contentType(MediaType.APPLICATION_JSON)
                .content(secSecUserSecRole.toJSON()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.callback").exists());
    }

    @Test
    @Transactional
    public void delete_parent_relation_term() throws Exception {
        Term term1 = builder.given_a_term();
        Term term2 = builder.given_a_term(term1.getOntology());
        Long userId = builder.given_superadmin().getId();
        long relationId = 42L;
        UUID commandId = UUID.randomUUID();
        TermRelationResponse response = new TermRelationResponse(
            relationId, term1.getId(), term2.getId(), term1.getOntology().getId(), 1L,
            LocalDateTime.now(), Optional.empty(), LocalDateTime.now(), "parent"
        );

        when(termRelationHttpContract.delete(eq(relationId), eq(userId))).thenReturn(
            Optional.of(new HttpCommandResponse(true, response, commandId, Commands.DELETE_TERM_RELATION)));

        restSecUserSecRoleControllerMockMvc.perform(delete("/api/relation/term/{id}.json", relationId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.command").value(Commands.DELETE_TERM_RELATION))
            .andExpect(jsonPath("$.data.id").value(relationId));
    }

    @Test
    @Transactional
    public void define() throws Exception {
        User user = builder.given_a_user();

        restSecUserSecRoleControllerMockMvc.perform(put(
                "/api/user/{user}/role/{role}/define.json",
                user.getId(),
                secRoleRepository.getAdmin().getId()
            )
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        em.refresh(user);
        assertThat(user.getRoles().stream().map(x -> x.getAuthority()))
            .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER", "ROLE_GUEST");
    }

}
