package be.cytomine.service.meta;

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
import be.cytomine.config.WiremockRepository;
import be.cytomine.domain.meta.Tag;
import be.cytomine.domain.meta.TagDomainAssociation;
import be.cytomine.domain.ontology.UserAnnotation;
import be.cytomine.domain.project.Project;
import be.cytomine.repository.meta.TagDomainAssociationRepository;
import be.cytomine.utils.filters.SearchOperation;
import be.cytomine.utils.filters.SearchParameterEntry;

import static be.cytomine.authorization.AbstractAuthorizationTest.SUPERADMIN;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = SUPERADMIN)
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class, WiremockRepository.class})
@Transactional
public class TagDomainAssociationServiceTests {

    @Autowired
    TagDomainAssociationService tagDomainAssociationService;

    @Autowired
    TagDomainAssociationRepository tagDomainAssociationRepository;

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

}
