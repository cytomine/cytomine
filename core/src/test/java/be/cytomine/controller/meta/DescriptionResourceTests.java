package be.cytomine.controller.meta;

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
import be.cytomine.config.WiremockRepository;
import be.cytomine.domain.meta.Description;
import be.cytomine.service.UrlApi;

import static be.cytomine.authorization.AbstractAuthorizationTest.SUPER_ADMIN;
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
@WithMockUser(username = SUPER_ADMIN)
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class, WiremockRepository.class})
public class DescriptionResourceTests {

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private MockMvc restDescriptionControllerMockMvc;
    @Autowired
    private UrlApi urlApi;

    @Test
    @Transactional
    public void listAllDescription() throws Exception {
        Description description = builder.givenADescription(builder.givenAProject());
        restDescriptionControllerMockMvc.perform(get("/api/description.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.collection[?(@.domainIdent=='" + description.getDomainIdent() + "')]").exists());
    }

    @Test
    @Transactional
    public void getAnDescription() throws Exception {
        Description description = builder.givenADescription(builder.givenAProject());
        restDescriptionControllerMockMvc.perform(get(
                "/api/domain/{domainClassName}/{domainIdent}/description.json",
                description.getDomainClassName(),
                description.getDomainIdent()
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(description.getId().intValue()));
    }

    @Test
    @Transactional
    public void getAnDescriptionDoesNotExists() throws Exception {
        Description description = builder.givenANotPersistedDescription(builder.givenAProject());
        restDescriptionControllerMockMvc.perform(get(
                "/api/domain/{domainClassName}/{domainIdent}/description.json",
                description.getDomainClassName(),
                description.getDomainIdent()
            ))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void addValidDescription() throws Exception {
        Description description = builder.givenANotPersistedDescription(builder.givenAProject());
        restDescriptionControllerMockMvc.perform(post(
                "/api/domain/{domainClassName}/{domainIdent}/description.json",
                description.getDomainClassName(),
                description.getDomainIdent()
            )
                .contentType(MediaType.APPLICATION_JSON)
                .content(description.toJSON(urlApi)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.callback").exists())
            .andExpect(jsonPath("$.callback.descriptionID").exists())
            .andExpect(jsonPath("$.callback.method").value("be.cytomine.AddDescriptionCommand"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.command").exists())
            .andExpect(jsonPath("$.description.id").exists());
    }

    @Test
    @Transactional
    public void editValidDescription() throws Exception {
        Description description = builder.givenANotPersistedDescription(builder.givenAProject());
        builder.persistAndReturn(description);
        restDescriptionControllerMockMvc.perform(put(
                "/api/domain/{domainClassName}/{domainIdent}/description.json",
                description.getDomainClassName(),
                description.getDomainIdent()
            )
                .contentType(MediaType.APPLICATION_JSON)
                .content(description.toJsonObject(urlApi).withChange("data", "v2").toJsonString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.callback").exists())
            .andExpect(jsonPath("$.callback.descriptionID").exists())
            .andExpect(jsonPath("$.callback.method").value("be.cytomine.EditDescriptionCommand"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.command").exists())
            .andExpect(jsonPath("$.description.id").exists())
            .andExpect(jsonPath("$.description.data").value("v2"));
    }

    @Test
    @Transactional
    public void deleteDescription() throws Exception {
        Description description = builder.givenANotPersistedDescription(builder.givenAProject());
        builder.persistAndReturn(description);
        restDescriptionControllerMockMvc.perform(delete(
                "/api/domain/{domainClassName}/{domainIdent}/description.json",
                description.getDomainClassName(),
                description.getDomainIdent()
            )
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
}
