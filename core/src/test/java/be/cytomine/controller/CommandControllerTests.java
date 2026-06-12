package be.cytomine.controller;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.domain.command.Command;
import be.cytomine.domain.command.DeleteCommand;
import be.cytomine.domain.image.UploadedFile;
import be.cytomine.repository.command.CommandRepository;
import be.cytomine.repository.ontology.OntologyRepository;
import be.cytomine.service.image.UploadedFileService;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class})
public class CommandControllerTests {

    @Autowired
    private BasicInstanceBuilder basicInstanceBuilder;

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private OntologyRepository ontologyRepository;

    @Autowired
    private MockMvc restCommandControllerMockMvc;

    @Autowired
    private CommandRepository commandRepository;

    @Autowired
    private UploadedFileService uploadedFileService;

    @Test
    @Transactional
    @WithMockUser(username = "superadmin")
    public void listDeleteCommand() throws Exception {

        Long start = System.currentTimeMillis();

        int initialSize = (int) commandRepository.findAll()
            .stream()
            .filter(x -> x instanceof DeleteCommand)
            .count();
        int initialSizeUploadedFileDeleteCommand = (int) commandRepository.findAll()
            .stream()
            .filter(x -> x instanceof DeleteCommand && x.getServiceName().equals("UploadedFileService"))
            .count();

        restCommandControllerMockMvc.perform(get("/api/deletecommand.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(initialSize))));

        restCommandControllerMockMvc.perform(
                get("/api/deletecommand.json").param("domain", "uploadedFile"))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.collection", hasSize(equalTo(initialSizeUploadedFileDeleteCommand))));

        UploadedFile uploadedFile = builder.givenAUploadedFile();

        Command c = new DeleteCommand(builder.givenSuperAdmin(), null);
        uploadedFileService.executeCommand(c, uploadedFile, null);

        restCommandControllerMockMvc.perform(get("/api/deletecommand.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(initialSize + 1))));

        restCommandControllerMockMvc.perform(
                get("/api/deletecommand.json").param("domain", "uploadedFile"))
            .andExpect(status().isOk())
            .andExpect(jsonPath(
                "$.collection",
                hasSize(equalTo(initialSizeUploadedFileDeleteCommand + 1))
            ));

        restCommandControllerMockMvc.perform(
                get("/api/deletecommand.json").param("domain", "uploadedFile")
                    .param("after", String.valueOf(new Date().getTime())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(0))));

        restCommandControllerMockMvc.perform(
                get("/api/deletecommand.json").param("domain", "uploadedFile")
                    .param("after", String.valueOf(start)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(equalTo(1))));
    }
}
