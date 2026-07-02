package be.cytomine.controller.meta;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.TagDomainAssociationHttpContract;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.TagDomainAssociationResponse;
import be.cytomine.common.repository.model.tagdomainassociation.payload.CreateTagDomainAssociation;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.meta.TagDomainAssociation;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "admin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
public class TagDomainAssociationResourceTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TagDomainAssociationHttpContract httpContract;

    private TagDomainAssociationResponse toResponse(TagDomainAssociation tda) {
        return new TagDomainAssociationResponse(tda.getId(), tda.getTag().getId(), tda.getDomainClassName(),
            tda.getDomainIdent(), LocalDateTime.now(), Optional.empty(), Optional.empty());
    }

    @Test
    @Transactional
    public void shouldReturnTagDomainAssociationById() throws Exception {
        TagDomainAssociation tda = builder.givenATagAssociation(builder.givenATag(), builder.givenAProject());
        long userId = builder.givenDefaultAdmin().getId();
        when(httpContract.read(eq(tda.getId()), eq(userId))).thenReturn(Optional.of(toResponse(tda)));

        mockMvc.perform(get("/api/tag_domain_association/{id}.json", tda.getId())).andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(tda.getId().intValue()));
    }

    @Test
    @Transactional
    public void getAnTagDomainAssociationDoesNotExists() throws Exception {
        long userId = builder.givenDefaultAdmin().getId();
        when(httpContract.read(eq(0L), eq(userId))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tag_domain_association/{id}.json", 0L)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void listAllTagDomainAssociation() throws Exception {
        TagDomainAssociation tda = builder.givenATagAssociation(builder.givenATag(), builder.givenAProject());
        long userId = builder.givenDefaultAdmin().getId();
        when(httpContract.readAll(eq(userId), any(Pageable.class))).thenReturn(
            new PageImpl<>(List.of(toResponse(tda))));

        mockMvc.perform(get("/api/tag_domain_association.json")).andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.collection[0].domainId").value(tda.getDomainIdent().intValue()));
    }

    @Test
    @Transactional
    public void listTagDomainAssociationsByDomain() throws Exception {
        TagDomainAssociation tda = builder.givenATagAssociation(builder.givenATag(), builder.givenAProject());
        long userId = builder.givenDefaultAdmin().getId();
        when(httpContract.readAllByDomain(eq(tda.getDomainClassName()), eq(tda.getDomainIdent()), eq(userId),
            any(Pageable.class))).thenReturn(new PageImpl<>(List.of(toResponse(tda))));

        mockMvc.perform(
                get("/api/domain/{domainClassName}/{domainId}/tag_domain_association.json", tda.getDomainClassName(),
                    tda.getDomainIdent())).andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.collection[0].id").value(tda.getId().intValue()));
    }

    @Test
    @Transactional
    public void listTagDomainAssociationsByDomainReturnsEmptyWhenNotAccessible() throws Exception {
        TagDomainAssociation tda = builder.givenATagAssociation(builder.givenATag(), builder.givenAProject());
        long userId = builder.givenDefaultAdmin().getId();
        when(httpContract.readAllByDomain(eq(tda.getDomainClassName()), eq(0L), eq(userId),
            any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(
                get("/api/domain/{domainClassName}/{domainId}/tag_domain_association.json", tda.getDomainClassName(),
                    0))
            .andExpect(status().isOk()).andExpect(jsonPath("$.collection", hasSize(0)));
    }

    @Test
    @Transactional
    public void addValidAssociation() throws Exception {
        TagDomainAssociation tda = builder.givenATagAssociation(builder.givenATag(), builder.givenAProject());
        long userId = builder.givenDefaultAdmin().getId();
        UUID commandId = UUID.randomUUID();
        when(httpContract.create(eq(userId), any())).thenReturn(Optional.of(
            new HttpCommandResponse(true, toResponse(tda), commandId, Commands.CREATE_TAG_DOMAIN_ASSOCIATION,
                Set.of())));

        String body = objectMapper.writeValueAsString(
            new CreateTagDomainAssociation(tda.getTag().getId(), tda.getDomainClassName(), tda.getDomainIdent()));
        mockMvc.perform(
                post("/api/domain/{domainClassName}/{domainId}/tag_domain_association.json", tda.getDomainClassName(),
                    tda.getDomainIdent()).contentType(MediaType.APPLICATION_JSON)
                    .content(body)).andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.command").value(Commands.CREATE_TAG_DOMAIN_ASSOCIATION));
    }

    @Test
    @Transactional
    public void addValidPropertyOtherPath() throws Exception {
        TagDomainAssociation tda = builder.givenATagAssociation(builder.givenATag(), builder.givenAProject());
        long userId = builder.givenDefaultAdmin().getId();
        UUID commandId = UUID.randomUUID();
        when(httpContract.create(eq(userId), any())).thenReturn(Optional.of(
            new HttpCommandResponse(true, toResponse(tda), commandId, Commands.CREATE_TAG_DOMAIN_ASSOCIATION,
                Set.of())));

        String body = objectMapper.writeValueAsString(
            new CreateTagDomainAssociation(tda.getTag().getId(), tda.getDomainClassName(), tda.getDomainIdent()));
        mockMvc.perform(post("/api/tag_domain_association.json").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk()).andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.command").value(Commands.CREATE_TAG_DOMAIN_ASSOCIATION));
    }

    @Test
    @Transactional
    public void deleteTagDomainAssociation() throws Exception {
        TagDomainAssociation tda = builder.givenATagAssociation(builder.givenATag(), builder.givenAProject());
        long userId = builder.givenDefaultAdmin().getId();
        UUID commandId = UUID.randomUUID();
        when(httpContract.delete(eq(tda.getId()), eq(userId))).thenReturn(Optional.of(
            new HttpCommandResponse(true, toResponse(tda), commandId, Commands.DELETE_TAG_DOMAIN_ASSOCIATION,
                Set.of())));

        mockMvc.perform(delete("/api/tag_domain_association/{id}.json", tda.getId())).andExpect(status().isOk());
    }
}
