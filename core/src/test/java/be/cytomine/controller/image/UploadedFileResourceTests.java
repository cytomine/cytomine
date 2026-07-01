package be.cytomine.controller.image;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.client.WireMock;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.common.repository.model.command.Commands;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.command.payload.response.UploadedFileResponse;
import be.cytomine.common.repository.model.uploadedfile.payload.CreateUploadedFile;
import be.cytomine.common.repository.model.uploadedfile.payload.UpdateUploadedFile;
import be.cytomine.common.repository.utils.SpringPage;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.config.WiremockRepository;
import be.cytomine.domain.image.AbstractImage;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "admin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class, WiremockRepository.class})
@Transactional
class UploadedFileResourceTests {

    private final ObjectMapper objectMapper =
        new ObjectMapper().registerModule(new Jdk8Module()).registerModule(new JavaTimeModule());
    @Autowired
    private BasicInstanceBuilder builder;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldListUploadedFiles() throws Exception {
        WiremockRepository.SERVER.stubFor(WireMock.get(urlPathEqualTo("/uploaded-files/all")).willReturn(
            aResponse().withStatus(HttpStatus.OK.value()).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(pageJson(anUploadedFileResponse(42L)))));

        mockMvc.perform(get("/api/uploadedfile.json")).andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(1))).andExpect(jsonPath("$.collection[0].id").value(42))
            .andExpect(jsonPath("$.size").value(1)).andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void shouldListUploadedFilesWithPagination() throws Exception {
        WiremockRepository.SERVER.stubFor(WireMock.get(urlPathEqualTo("/uploaded-files/all")).willReturn(
            aResponse().withStatus(HttpStatus.OK.value()).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(pageJson(1, 3L, anUploadedFileResponse(1L)))));

        mockMvc.perform(get("/api/uploadedfile.json").param("size", "1").param("page", "0")).andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(1))).andExpect(jsonPath("$.perPage").value(1))
            .andExpect(jsonPath("$.size").value(3)).andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void shouldReturnEmptyCollectionWhenNoUploadedFiles() throws Exception {
        WiremockRepository.SERVER.stubFor(WireMock.get(urlPathEqualTo("/uploaded-files/all")).willReturn(
            aResponse().withStatus(HttpStatus.OK.value()).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(emptyPageJson())));

        mockMvc.perform(get("/api/uploadedfile.json")).andExpect(status().isOk())
            .andExpect(jsonPath("$.collection", hasSize(0))).andExpect(jsonPath("$.size").value(0));
    }

    @Test
    void shouldReturnUploadedFileById() throws Exception {
        WiremockRepository.SERVER.stubFor(WireMock.get(urlPathEqualTo("/uploaded-files/42")).willReturn(
            aResponse().withStatus(HttpStatus.OK.value()).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(anUploadedFileResponse(42L)))));

        mockMvc.perform(get("/api/uploadedfile/{id}.json", 42)).andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(42)).andExpect(jsonPath("$.filename").value("file_42.tif"))
            .andExpect(jsonPath("$.originalFilename").value("original_42.tif"));
    }

    @Test
    void shouldReturn404WhenUploadedFileNotFound() throws Exception {
        WiremockRepository.SERVER.stubFor(WireMock.get(urlPathEqualTo("/uploaded-files/999"))
            .willReturn(aResponse().withStatus(HttpStatus.OK.value())));

        mockMvc.perform(get("/api/uploadedfile/{id}.json", 999)).andExpect(status().isNotFound());
    }

    @Test
    void shouldIncludeThumbnailUrlWhenAbstractImageExists() throws Exception {
        AbstractImage abstractImage = builder.givenAnAbstractImage();
        long uploadedFileId = abstractImage.getUploadedFile().getId();

        WiremockRepository.SERVER.stubFor(WireMock.get(urlPathEqualTo("/uploaded-files/" + uploadedFileId)).willReturn(
            aResponse().withStatus(HttpStatus.OK.value()).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(anUploadedFileResponse(uploadedFileId)))));

        mockMvc.perform(get("/api/uploadedfile/{id}.json", uploadedFileId)).andExpect(status().isOk())
            .andExpect(jsonPath("$.thumbnailUrl").isString());
    }

    @Test
    void shouldCreateUploadedFile() throws Exception {
        UUID commandId = UUID.randomUUID();
        WiremockRepository.SERVER.stubFor(WireMock.post(urlPathEqualTo("/uploaded-files")).willReturn(
            aResponse().withStatus(HttpStatus.OK.value()).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(
                    new HttpCommandResponse(true, null, commandId, Commands.CREATE_UPLOADED_FILE, Set.of())))));

        CreateUploadedFile payload =
            new CreateUploadedFile(1L, 1L, Optional.empty(), "test.tif", "test.tif", "tif", "PYRTIFF", 100L, 0,
                Set.of());

        mockMvc.perform(post("/api/uploadedfile.json").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload))).andExpect(status().isOk())
            .andExpect(jsonPath("$.commandId").value(commandId.toString()))
            .andExpect(jsonPath("$.printMessage").value(true));
    }

    @Test
    void shouldUpdateUploadedFile() throws Exception {
        UUID commandId = UUID.randomUUID();
        WiremockRepository.SERVER.stubFor(WireMock.put(urlPathEqualTo("/uploaded-files/42")).willReturn(
            aResponse().withStatus(HttpStatus.OK.value()).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(
                    new HttpCommandResponse(true, null, commandId, Commands.UPDATE_UPLOADED_FILE, Set.of())))));

        UpdateUploadedFile payload =
            new UpdateUploadedFile(Optional.of("updated.tif"), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());

        mockMvc.perform(put("/api/uploadedfile/{id}.json", 42).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload))).andExpect(status().isOk())
            .andExpect(jsonPath("$.commandId").value(commandId.toString()));
    }

    @Test
    void shouldReturn404WhenUpdatingNonexistentUploadedFile() throws Exception {
        WiremockRepository.SERVER.stubFor(WireMock.put(urlPathEqualTo("/uploaded-files/999"))
            .willReturn(aResponse().withStatus(HttpStatus.OK.value())));

        UpdateUploadedFile payload =
            new UpdateUploadedFile(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());

        mockMvc.perform(put("/api/uploadedfile/{id}.json", 999).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(payload))).andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteUploadedFile() throws Exception {
        UUID commandId = UUID.randomUUID();
        WiremockRepository.SERVER.stubFor(WireMock.delete(urlPathEqualTo("/uploaded-files/42")).willReturn(
            aResponse().withStatus(HttpStatus.OK.value()).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(
                    new HttpCommandResponse(true, null, commandId, Commands.DELETE_UPLOADED_FILE, Set.of())))));

        mockMvc.perform(delete("/api/uploadedfile/{id}.json", 42)).andExpect(status().isOk())
            .andExpect(jsonPath("$.commandId").value(commandId.toString()));
    }

    @Test
    void shouldReturn404WhenDeletingNonexistentUploadedFile() throws Exception {
        WiremockRepository.SERVER.stubFor(WireMock.delete(urlPathEqualTo("/uploaded-files/999"))
            .willReturn(aResponse().withStatus(HttpStatus.OK.value())));

        mockMvc.perform(delete("/api/uploadedfile/{id}.json", 999)).andExpect(status().isNotFound());
    }

    private UploadedFileResponse anUploadedFileResponse(long id) {
        return new UploadedFileResponse(id, Optional.of(1L), Optional.empty(), Optional.of(1L), "file_" + id + ".tif",
            "original_" + id + ".tif", "tif", "PYRTIFF", 100L, "/data/file_" + id + ".tif", 0, Set.of(),
            LocalDateTime.of(2024, 1, 1, 0, 0), Optional.empty(), Optional.empty(), Optional.empty());
    }

    private String pageJson(UploadedFileResponse item) throws Exception {
        return objectMapper.writeValueAsString(new SpringPage<>(List.of(item), 0, 1, 1L));
    }

    private String pageJson(int size, long totalElements, UploadedFileResponse item) throws Exception {
        return objectMapper.writeValueAsString(new SpringPage<>(List.of(item), 0, size, totalElements));
    }

    private String emptyPageJson() throws Exception {
        return objectMapper.writeValueAsString(new SpringPage<>(List.of(), 0, 20, 0L));
    }
}
