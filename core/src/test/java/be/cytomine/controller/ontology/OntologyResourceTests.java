package be.cytomine.controller.ontology;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.http.OntologyHttpContract;
import be.cytomine.common.repository.http.TermHttpContract;
import be.cytomine.common.repository.http.TermRelationHttpContract;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.OntologyResponse;
import be.cytomine.common.repository.model.command.payload.response.TermResponse;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.ontology.Ontology;
import be.cytomine.domain.ontology.Term;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
public class OntologyResourceTests {

    @MockitoBean
    TermRelationHttpContract termRelationHttpContract;
    @MockitoBean
    private TermHttpContract termHttpContract;
    @MockitoBean
    private OntologyHttpContract ontologyHttpContract;
    @Autowired
    private BasicInstanceBuilder basicInstanceBuilder;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Transactional
    public void listAllOntologies() throws Exception {
        Ontology ontology = basicInstanceBuilder.givenAnOntology();
        mockMvc.perform(get("/api/ontology.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.collection[?(@.name=='" + ontology.getName() + "')]").exists())
            .andExpect(jsonPath("$.collection[?(@.name=='" + ontology.getName() + "')].projects").exists());
    }

    @Test
    @Transactional
    public void listAllOntologiesLight() throws Exception {
        Ontology ontology = basicInstanceBuilder.givenAnOntology();
        mockMvc.perform(get("/api/ontology.json").param("light", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.collection[?(@.name=='" + ontology.getName() + "')]").exists())
            .andExpect(jsonPath("$.collection[?(@.name=='" + ontology.getName() + "')].projects").doesNotExist());
    }

    @Test
    @Transactional
    public void shouldReturnOntology() throws Exception {
        Ontology ontology = basicInstanceBuilder.givenAnOntology();
        Long userId = basicInstanceBuilder.givenSuperAdmin().getId();
        when(ontologyHttpContract.get(eq(ontology.getId()), eq(userId)))
            .thenReturn(Optional.of(new OntologyResponse(
                ontology.getName(),
                ontology.getId(),
                Set.of(),
                LocalDateTime.ofInstant(ontology.getCreated().toInstant(), ZoneId.systemDefault()),
                LocalDateTime.ofInstant(ontology.getUpdated().toInstant(), ZoneId.systemDefault()),
                Optional.empty()
            )));

        mockMvc.perform(get("/api/ontology/{id}.json", ontology.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ontology.getId().intValue()))
            .andExpect(jsonPath("$.name").value(ontology.getName()))
            .andExpect(jsonPath("$.created").exists())
            .andExpect(jsonPath("$.updated").exists());
    }

    @Test
    @Transactional
    public void addValidOntology() throws Exception {
        Ontology ontology = basicInstanceBuilder.givenANotPersistedOntology();
        Long userId = basicInstanceBuilder.givenSuperAdmin().getId();
        UUID commandId = UUID.randomUUID();
        when(ontologyHttpContract.create(eq(userId), any())).thenReturn(Optional.of(new HttpCommandResponse(
            true,
            new OntologyResponse(
                ontology.getName(),
                1L,
                Set.of(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                Optional.empty()
            ),
            commandId,
            Commands.CREATE_ONTOLOGY
        )));

        mockMvc.perform(post("/api/ontology.json")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ontology.toJSON()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.command").value(Commands.CREATE_ONTOLOGY))
            .andExpect(jsonPath("$.data.name").value(ontology.getName()));
    }

    @Test
    @Transactional
    public void addOntologyReturnsEmpty() throws Exception {
        Ontology ontology = basicInstanceBuilder.givenANotPersistedOntology();
        Long userId = basicInstanceBuilder.givenSuperAdmin().getId();
        when(ontologyHttpContract.create(eq(userId), any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/ontology.json")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ontology.toJSON()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    @Transactional
    public void editValidOntology() throws Exception {
        Ontology ontology = basicInstanceBuilder.givenAnOntology();
        Long userId = basicInstanceBuilder.givenSuperAdmin().getId();
        UUID commandId = UUID.randomUUID();
        when(ontologyHttpContract.update(eq(ontology.getId()), eq(userId), any())).thenReturn(Optional.of(
            new HttpCommandResponse(
                true,
                new OntologyResponse(
                    ontology.getName(),
                    ontology.getId(),
                    Set.of(),
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    Optional.empty()
                ),
                commandId,
                Commands.UPDATE_ONTOLOGY
            )));

        mockMvc.perform(put("/api/ontology/{id}.json", ontology.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(ontology.toJSON()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.command").value(Commands.UPDATE_ONTOLOGY))
            .andExpect(jsonPath("$.data.id").value(ontology.getId()))
            .andExpect(jsonPath("$.data.name").value(ontology.getName()));
    }

    @Test
    @Transactional
    public void failWhenEditingOntologyDoesNotExists() throws Exception {
        Ontology ontology = basicInstanceBuilder.givenAnOntology();
        Long userId = basicInstanceBuilder.givenSuperAdmin().getId();
        when(ontologyHttpContract.update(eq(0L), eq(userId), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/ontology/{id}.json", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ontology.toJSON()))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void deleteOntology() throws Exception {
        Ontology ontology = basicInstanceBuilder.givenAnOntology();
        Long userId = basicInstanceBuilder.givenSuperAdmin().getId();
        UUID commandId = UUID.randomUUID();
        when(ontologyHttpContract.delete(eq(ontology.getId()), eq(userId))).thenReturn(Optional.of(
            new HttpCommandResponse(
                true,
                new OntologyResponse(
                    ontology.getName(),
                    ontology.getId(),
                    Set.of(),
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    Optional.of(LocalDateTime.now())
                ),
                commandId,
                Commands.DELETE_ONTOLOGY
            )));

        mockMvc.perform(delete("/api/ontology/{id}.json", ontology.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(ontology.toJSON()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.printMessage").value(true))
            .andExpect(jsonPath("$.command").value(Commands.DELETE_ONTOLOGY))
            .andExpect(jsonPath("$.data.id").value(ontology.getId()))
            .andExpect(jsonPath("$.data.name").value(ontology.getName()));
    }

    @Test
    @Transactional
    public void failWhenDeleteOntologyNotExists() throws Exception {
        Long userId = basicInstanceBuilder.givenSuperAdmin().getId();
        when(ontologyHttpContract.delete(eq(0L), eq(userId))).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/ontology/{id}.json", 0)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void exportShouldReturnOkWithCorrectStructure() throws Exception {
        Ontology ontology = basicInstanceBuilder.givenAnOntology();
        Term term = basicInstanceBuilder.givenATerm(ontology);
        Long userId = basicInstanceBuilder.givenSuperAdmin().getId();
        when(termHttpContract.findTermsByOntology(eq(ontology.getId()), eq(userId), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(
                new TermResponse(
                    term.getId(), term.getName(), term.getColor(), term.getOntology().getId(),
                    LocalDateTime.ofInstant(term.getCreated().toInstant(), ZoneId.systemDefault()),
                    LocalDateTime.ofInstant(term.getUpdated().toInstant(), ZoneId.systemDefault()),
                    Optional.empty(), Optional.ofNullable(term.getComment()), Set.of()
                )))
            );

        mockMvc.perform(get("/api/ontology/{id}/export", ontology.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(header().string("Content-Disposition", containsString("attachment; filename=")))
            .andExpect(header().string("Content-Disposition", containsString(".json")))
            .andExpect(jsonPath("$.name").value(ontology.getName()))
            .andExpect(jsonPath("$.terms[0].name").value(term.getName()))
            .andExpect(jsonPath("$.terms[0].color").value(term.getColor()));
    }

    @Test
    public void exportShouldReturnNotFoundWhenOntologyDoesNotExist() throws Exception {
        Long nonExistentId = 0L;

        mockMvc.perform(get("/api/ontology/{id}/export", nonExistentId))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
            .andExpect(jsonPath("$.title").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.detail").value("Ontology not found with id: " + nonExistentId))
            .andExpect(jsonPath("$.instance").value("/api/ontology/" + nonExistentId + "/export"));
    }
}
