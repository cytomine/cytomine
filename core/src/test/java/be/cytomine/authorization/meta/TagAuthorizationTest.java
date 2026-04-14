package be.cytomine.authorization.meta;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.authorization.AbstractAuthorizationTest;
import be.cytomine.domain.meta.Tag;
import be.cytomine.domain.meta.TagDomainAssociation;
import be.cytomine.service.meta.TagService;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@AutoConfigureMockMvc
@SpringBootTest(classes = CytomineCoreApplication.class)
@Transactional
public class TagAuthorizationTest extends AbstractAuthorizationTest {

    private Tag tag = null;

    @Autowired
    TagService tagService;

    @Autowired
    BasicInstanceBuilder builder;

    protected void whenIAddDomain() {
        tagService.add(builder.givenANotPersistedTag("xxx").toJsonObject());
    }

    @BeforeEach
    public void before() throws Exception {
        if (tag == null) {
            tag = builder.givenATag();
        }
    }

    @Test
    @WithMockUser(username = GUEST)
    public void shouldReturnTagWhenSearchingByNameAsGuest() {
        tagService.findByName(tag.getName());
    }

    @Test
    @WithMockUser(username = GUEST)
    public void shouldReturnTagWhenSearchingByIdAsGuest() {
        tagService.find(tag.getId());
    }

    @Test
    @WithMockUser(username = GUEST)
    public void everyoneCanListTags() {
        assertThat(tagService.list()).contains(tag);
    }

    @Test
    @WithMockUser(username = GUEST)
    public void guestCannotAddTag() {
        expectForbidden(this::whenIAddDomain);
    }

    @Test
    @WithMockUser(username = USER_NO_ACL)
    public void userCanAddTag() {
        expectOK(this::whenIAddDomain);
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void adminCanUpdateTagEvenIfLinkedWithAssociations() {
        Tag tagToEdit = builder.givenATag();
        TagDomainAssociation association = builder.givenATagAssociation(tagToEdit, builder.givenAProject());
        expectOK(() -> tagService.update(tagToEdit, tagToEdit.toJsonObject()));
    }

    @Test
    @WithMockUser(username = CREATOR)
    public void creatorCannotUpdateTagIfLinkedWithAssociations() {
        Tag tagToEdit = builder.givenATag();
        tagToEdit.setUser(userRepository.findByUsernameLikeIgnoreCase(CREATOR).get());
        builder.persistAndReturn(tagToEdit);
        TagDomainAssociation association = builder.givenATagAssociation(tagToEdit, builder.givenAProject());
        expectForbidden(() -> tagService.update(tagToEdit, tagToEdit.toJsonObject()));
    }

    @Test
    @WithMockUser(username = CREATOR)
    public void creatorCanUpdateTagIfNotLinkedWithAssociations() {
        Tag tagToEdit = builder.givenATag();
        tagToEdit.setUser(userRepository.findByUsernameLikeIgnoreCase(CREATOR).get());
        builder.persistAndReturn(tagToEdit);
        expectOK(() -> tagService.update(tagToEdit, tagToEdit.toJsonObject()));
    }

    @Test
    @WithMockUser(username = SUPERADMIN)
    public void adminCanTagTag() {
        Tag tagToEdit = builder.givenATag();
        TagDomainAssociation association = builder.givenATagAssociation(tagToEdit, builder.givenAProject());
        expectOK(() -> tagService.delete(tagToEdit, null, null, false));
    }

    @Test
    @WithMockUser(username = CREATOR)
    public void creatorCannotDeleteTagIfLinkedWithAssociations() {
        Tag tagToDelete = builder.givenATag();
        tagToDelete.setUser(userRepository.findByUsernameLikeIgnoreCase(CREATOR).get());
        builder.persistAndReturn(tagToDelete);
        TagDomainAssociation association = builder.givenATagAssociation(tagToDelete, builder.givenAProject());
        expectForbidden(() -> tagService.delete(tagToDelete, null, null, false));
    }

    @Test
    @WithMockUser(username = CREATOR)
    public void creatorCanDeleteTagIfNotLinkedWithAssociations() {
        Tag tagToDelete = builder.givenATag();
        tagToDelete.setUser(userRepository.findByUsernameLikeIgnoreCase(CREATOR).get());
        builder.persistAndReturn(tagToDelete);
        expectOK(() -> tagService.delete(tagToDelete, null, null, false));
    }
}
