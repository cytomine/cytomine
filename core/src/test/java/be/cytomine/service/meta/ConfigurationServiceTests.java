package be.cytomine.service.meta;

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

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.meta.Configuration;
import be.cytomine.exceptions.AlreadyExistException;
import be.cytomine.repository.meta.ConfigurationRepository;
import be.cytomine.service.CommandService;
import be.cytomine.service.PermissionService;
import be.cytomine.service.command.TransactionService;
import be.cytomine.service.security.SecurityACLService;
import be.cytomine.utils.CommandResponse;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_SUPER_ADMIN", username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class ConfigurationServiceTests {

    @Autowired
    ConfigurationService configurationService;

    @Autowired
    ConfigurationRepository configurationRepository;

    @Autowired
    BasicInstanceBuilder builder;

    @Autowired
    CommandService commandService;

    @Autowired
    TransactionService transactionService;

    @Autowired
    PermissionService permissionService;

    @Autowired
    SecurityACLService securityACLService;

    @Test
    void listAllConfigurationWithSuccess() {
        Configuration configuration = builder.givenAConfiguration("xxx");
        assertThat(configuration).isIn(configurationService.list());
    }

    @Test
    void findConfigurationWithSuccess() {
        Configuration configuration = builder.givenAConfiguration("xxx");
        assertThat(configurationService.findByKey("xxx")).contains(configuration);
    }

    @Test
    void findUnexistingConfigurationReturnEmpty() {
        assertThat(configurationService.findByKey("empty")).isEmpty();
    }

    @Test
    void addValidConfigurationWithSuccess() {
        Configuration configuration = builder.givenANotPersistedConfiguration("xxx");

        CommandResponse commandResponse = configurationService.add(configuration.toJsonObject());

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
    }

    @Test
    void addConfigurationWithAlreadyExistingKey() {
        Configuration configuration = builder.givenAConfiguration("xxx");

        Assertions.assertThrows(
            AlreadyExistException.class, () -> {
                configurationService.add(configuration.toJsonObject().withChange("id", null));
            }
        );
    }

    @Test
    void editValidConfigurationWithSuccess() {
        Configuration configuration = builder.givenAConfiguration("xxx");

        CommandResponse commandResponse = configurationService.update(
            configuration,
            configuration.toJsonObject().withChange("value", "NEW VALUE")
        );

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        assertThat(configurationService.findByKey("xxx")).isPresent();
        Configuration edited = configurationService.findByKey("xxx").get();
        assertThat(edited.getValue()).isEqualTo("NEW VALUE");
    }


    @Test
    void deleteConfigurationWithSuccess() {
        Configuration configuration = builder.givenAConfiguration("xxx");

        CommandResponse commandResponse = configurationService.delete(configuration, null, null, true);

        assertThat(commandResponse).isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(200);
        assertThat(configurationService.findByKey("xxx").isEmpty());
    }
}
