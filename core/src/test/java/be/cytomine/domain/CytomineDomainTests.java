package be.cytomine.domain;

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

import java.util.Date;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.CustomIdentifierGenerator;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.ontology.Ontology;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_SUPER_ADMIN", username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class CytomineDomainTests {

    @Autowired
    BasicInstanceBuilder basicInstanceBuilder;

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    EntityManager em;

    @Test
    void assign_id_automatically() {
        Ontology ontology = basicInstanceBuilder.givenANotPersistedOntology();
        assertThat(ontology.getId()).isNull();
        ontology = builder.persistAndReturn(ontology);
        assertThat(ontology.getId()).isPositive();
    }

    @Test
    void assign_created_date() {
        Date beforeCreate = new Date();
        Ontology ontology = basicInstanceBuilder.givenANotPersistedOntology();
        assertThat(ontology.getCreated()).isNull();
        ontology = builder.persistAndReturn(ontology);
        Date afterCreate = new Date();
        assertThat(ontology.getCreated()).isBetween(beforeCreate, afterCreate, true, true);
    }

    @Test
    void assign_updated_date() {
        Date beforeCreate = new Date();
        Ontology ontology = basicInstanceBuilder.givenANotPersistedOntology();
        assertThat(ontology.getUpdated()).isNull();
        ontology = builder.persistAndReturn(ontology);
        Date afterCreate = new Date();
        assertThat(ontology.getUpdated()).isBetween(beforeCreate, afterCreate, true, true);

        Date beforeUpdate = new Date();
        ontology.setName(UUID.randomUUID().toString());
        ontology = builder.persistAndReturn(ontology);
        Date afterUpdate = new Date();

        assertThat(ontology.getUpdated()).isBetween(beforeUpdate, afterUpdate, true, true);
    }

    @Test
    void preserve_preassigned_id() {
        Long preassignedId = 999999999L;
        Ontology ontology = basicInstanceBuilder.givenANotPersistedOntology();
        ontology.setId(preassignedId);
        assertThat(ontology.getId()).isEqualTo(preassignedId);
        ontology = builder.persistAndReturn(ontology);
        assertThat(ontology.getId()).isEqualTo(preassignedId);

        // Verify it can be retrieved from the database with the same ID
        em.clear();
        Ontology retrieved = em.find(Ontology.class, preassignedId);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getId()).isEqualTo(preassignedId);
    }

    @Test
    void custom_identifier_generator_allows_assigned_identifiers() {
        CustomIdentifierGenerator generator = new CustomIdentifierGenerator();
        assertThat(generator.allowAssignedIdentifiers()).isTrue();
    }
}
