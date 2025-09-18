package be.cytomine.authorization.image;

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

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.authorization.CRUDAuthorizationTest;
import be.cytomine.domain.image.AbstractImage;
import be.cytomine.service.PermissionService;
import be.cytomine.service.image.AbstractImageService;
import be.cytomine.service.security.SecurityACLService;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.security.acls.domain.BasePermission.WRITE;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class AbstractImageAuthorizationTest extends CRUDAuthorizationTest {

    // We need more flexibility:

    @Autowired
    AbstractImageService abstractImageService;
    @Autowired
    BasicInstanceBuilder builder;
    @Autowired
    SecurityACLService securityACLService;
    @Autowired
    PermissionService permissionService;
    private AbstractImage abstractImage = null;

    @BeforeEach
    public void before() throws Exception {
        if (abstractImage == null) {
            Long start = System.currentTimeMillis();
            abstractImage = builder.given_an_abstract_image();
            System.out.println("EXECUTION given_an_abstract_image:" + (System.currentTimeMillis() - start));
            start = System.currentTimeMillis();
            System.out.println("EXECUTION initUser:" + (System.currentTimeMillis() - start));
            start = System.currentTimeMillis();
            initACL(abstractImage.container());
            System.out.println("EXECUTION initACL:" + (System.currentTimeMillis() - start));
        }
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void admin_can_list_abstractImages() {
        assertThat(abstractImageService.list()).contains(abstractImage);
        AbstractImage anotherAbstractImage = builder.given_an_abstract_image();
        assertThat(abstractImageService.list()).contains(anotherAbstractImage);
    }

    @Test
    @WithMockUser(username = USER_ACL_READ)
    public void user_cannot_list_all_abstractImages() {
        expectOK(() -> abstractImageService.list());
        AbstractImage anotherAbstractImage = builder.given_an_abstract_image();
        assertThat(abstractImageService.list()).doesNotContain(anotherAbstractImage);
    }


    @Override
    public void when_i_get_domain() {
        abstractImageService.get(abstractImage.getId());
    }

    @Override
    protected void when_i_add_domain() {
        abstractImageService.add(builder.given_a_not_persisted_abstract_image().toJsonObject());
    }

    @Override
    public void when_i_edit_domain() {
        abstractImageService.update(abstractImage, abstractImage.toJsonObject());
    }

    @Override
    protected void when_i_delete_domain() {
        AbstractImage abstractImageToDelete = abstractImage;
        abstractImageService.delete(abstractImageToDelete, null, null, true);
    }

    @Override
    protected Optional<Permission> minimalPermissionForCreate() {
        return Optional.empty();
    }

    @Override
    protected Optional<Permission> minimalPermissionForDelete() {
        return Optional.of(WRITE);
    }

    @Override
    protected Optional<Permission> minimalPermissionForEdit() {
        return Optional.of(WRITE);
    }


    @Override
    protected Optional<String> minimalRoleForCreate() {
        return Optional.of("ROLE_USER");
    }

    @Override
    protected Optional<String> minimalRoleForDelete() {
        return Optional.of("ROLE_USER");
    }

    @Override
    protected Optional<String> minimalRoleForEdit() {
        return Optional.of("ROLE_USER");
    }
}
