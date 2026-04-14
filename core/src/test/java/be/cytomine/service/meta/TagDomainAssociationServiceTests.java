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

import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.test.context.support.WithMockUser;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.meta.Tag;
import be.cytomine.domain.meta.TagDomainAssociation;
import be.cytomine.domain.ontology.UserAnnotation;
import be.cytomine.domain.project.Project;
import be.cytomine.repository.meta.TagDomainAssociationRepository;
import be.cytomine.utils.CommandResponse;
import be.cytomine.utils.filters.SearchOperation;
import be.cytomine.utils.filters.SearchParameterEntry;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
@Transactional
public class TagDomainAssociationServiceTests {

    @Autowired
    TagDomainAssociationService tagDomainAssociationService;

    @Autowired
    TagDomainAssociationRepository tagDomainAssociationRepository;

    @Autowired
    TagService tagService;

    @Autowired
    BasicInstanceBuilder builder;

    @Test
    public void specificationTest() {
        TagDomainAssociation tagDomainAssociation = builder.givenATagAssociation(
            builder.givenATag(),
            builder.givenAProject()
        );

        Specification specification =
            (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("tag"))
                .value(tagDomainAssociation.getTag());

        assertThat(tagDomainAssociationRepository.findAll(
            specification,
            Sort.by(Sort.Direction.DESC, "domainClassName")
        )).contains(tagDomainAssociation);

    }

    @Test
    public void findById() {
        TagDomainAssociation tagDomainAssociation = builder.givenATagAssociation(
            builder.givenATag(),
            builder.givenAProject()
        );
        assertThat(tagDomainAssociationService.find(tagDomainAssociation.getId())).isPresent();
    }

    @Test
    public void findByIdThatDoNotExists() {
        assertThat(tagDomainAssociationService.find(0L)).isEmpty();
    }

    @Test
    public void getById() {
        TagDomainAssociation tagDomainAssociation = builder.givenATagAssociation(
            builder.givenATag(),
            builder.givenAProject()
        );
        assertThat(tagDomainAssociationService.get(tagDomainAssociation.getId())).isNotNull();
    }

    @Test
    public void listAllForDomain() {
        Project project = builder.givenAProject();
        TagDomainAssociation tagDomainAssociation = builder.givenATagAssociation(builder.givenATag(), project);
        TagDomainAssociation
            tagDomainAssociationFromOtherDomain
            = builder.givenATagAssociation(builder.givenATag(), builder.givenAProject());
        assertThat(tagDomainAssociationService.listAllByDomain(project))
            .contains(tagDomainAssociation)
            .doesNotContain(tagDomainAssociationFromOtherDomain);
    }

    @Test
    public void listAllForTag() {
        Project project = builder.givenAProject();
        Tag tag = builder.givenATag();
        TagDomainAssociation tagDomainAssociation = builder.givenATagAssociation(tag, project);
        TagDomainAssociation tagDomainAssociationFromOtherTag = builder.givenATagAssociation(
            builder.givenATag(),
            builder.givenAProject()
        );
        assertThat(tagDomainAssociationService.listAllByTag(tag)).contains(tagDomainAssociation)
            .doesNotContain(tagDomainAssociationFromOtherTag);
    }

    @Test
    public void listAllForTagAndDomain() {
        Project domain1 = builder.givenAProject();
        UserAnnotation domain2 = builder.givenAUserAnnotation();
        Tag tag1 = builder.givenATag();
        Tag tag2 = builder.givenATag();

        TagDomainAssociation tag1Domain1 = builder.givenATagAssociation(tag1, domain1);
        TagDomainAssociation tag2Domain1 = builder.givenATagAssociation(tag2, domain1);
        TagDomainAssociation tag1Domain2 = builder.givenATagAssociation(tag1, domain2);
        TagDomainAssociation tag2Domain2 = builder.givenATagAssociation(tag2, domain2);

        assertThat(tagDomainAssociationService.list(new ArrayList<>(List.of(
            new SearchParameterEntry("tag", SearchOperation.in, List.of(tag1.getId(), tag2.getId())),
            new SearchParameterEntry("domainIdent", SearchOperation.in, List.of(domain1.getId(), domain2.getId()))
        )))).contains(tag1Domain1, tag2Domain1, tag1Domain2, tag2Domain2);

        assertThat(tagDomainAssociationService.list(new ArrayList<>(List.of(
            new SearchParameterEntry("tag", SearchOperation.in, List.of(tag1.getId())),
            new SearchParameterEntry("domainIdent", SearchOperation.in, List.of(domain1.getId(), domain2.getId()))
        )))).contains(tag1Domain1, tag1Domain2).doesNotContain(tag2Domain1, tag2Domain2);

        assertThat(tagDomainAssociationService.list(new ArrayList<>(List.of(
            new SearchParameterEntry("tag", SearchOperation.in, List.of(tag1.getId())),
            new SearchParameterEntry("domainIdent", SearchOperation.in, List.of(domain1.getId()))
        )))).contains(tag1Domain1).doesNotContain(tag2Domain1);

        assertThat(tagDomainAssociationService.list(new ArrayList<>(List.of(
            new SearchParameterEntry("tag", SearchOperation.in, List.of(builder.givenATag().getId())),
            new SearchParameterEntry("domainIdent", SearchOperation.in, List.of(domain1.getId()))
        )))).doesNotContain(tag1Domain1, tag1Domain2, tag2Domain1, tag2Domain2);
    }


    @Test
    public void createTagAssociation() throws ClassNotFoundException {
        CommandResponse
            add
            = tagDomainAssociationService.add(builder.givenANotPersistedTagAssociation(
            builder.givenATag(),
            builder.givenAProject()
        ).toJsonObject());
        assertThat(tagDomainAssociationService.listAllByTag(((TagDomainAssociation) add.getObject()).getTag())).hasSize(
            1);
    }

    @Test
    public void deleteTagAssociation() {
        TagDomainAssociation tagDomainAssociation = builder.givenATagAssociation(
            builder.givenATag(),
            builder.givenAProject()
        );
        CommandResponse delete = tagDomainAssociationService.delete(tagDomainAssociation, null, null, false);
        assertThat(tagDomainAssociationService.listAllByTag(tagDomainAssociation.getTag())).hasSize(0);
    }

}
