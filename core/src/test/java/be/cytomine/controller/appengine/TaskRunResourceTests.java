package be.cytomine.controller.appengine;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.common.PostGisTestConfiguration;
import be.cytomine.config.MongoTestConfiguration;
import be.cytomine.config.WiremockRepository;
import be.cytomine.domain.appengine.TaskRun;
import be.cytomine.dto.appengine.task.TaskRunProvisionedResponse;
import be.cytomine.repository.appengine.TaskRunRepository;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
@Import({MongoTestConfiguration.class, PostGisTestConfiguration.class, WiremockRepository.class})
@Transactional
public class TaskRunResourceTests {

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private TaskRunRepository taskRunRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WiremockRepository wiremockRepository;

    @Value("${application.appEngine.apiBasePath}")
    private String apiBasePath;

    static {
        configureFor("localhost", WiremockRepository.SERVER.port());
    }

    @Test
    @Transactional
    public void addValidTaskRun() throws Exception {
        TaskRun taskRun = builder.givenANotPersistedTaskRun();
        taskRun.setTaskRunId(UUID.randomUUID());
        String taskId = UUID.randomUUID().toString();
        String queryBody = "{\"image\": \"" + taskRun.getImage().getId() + "\"}";

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> mockResponseMap = Map.of(
            "task", Map.of(
                "name", "string",
                "namespace", "string",
                "version", "string",
                "description", "string",
                "authors", List.of(Map.of(
                    "first_name", "string",
                    "last_name", "string",
                    "organization", "string",
                    "email", "string",
                    "is_contact", true
                ))
            ),
            "id", taskRun.getTaskRunId().toString(),
            "state", "created"
        );
        String mockResponse = objectMapper.writeValueAsString(mockResponseMap);

        WiremockRepository.SERVER.stubFor(WireMock.post(urlEqualTo(apiBasePath + "tasks/" + taskId + "/runs"))
            .willReturn(aResponse().withBody(mockResponse).withHeader("Content-Type", "application/json"))
        );

        mockResponseMap = Map.of(
            "id", taskRun.getTaskRunId().toString(),
            "name", "test name",
            "displayName", "test display name",
            "description", "test description",
            "optional", false,
            "type", Map.of("id", "string"),
            "derivedFrom", ""
        );

        mockResponse = objectMapper.writeValueAsString(List.of(mockResponseMap));

        WiremockRepository.SERVER.stubFor(WireMock.get(urlEqualTo(apiBasePath + "tasks/" + taskId + "/outputs"))
            .willReturn(aResponse().withBody(mockResponse).withHeader("Content-Type", "application/json"))
        );

        mockMvc.perform(post("/api/app-engine/project/" + taskRun.getProject().getId() + "/tasks/" + taskId + "/runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(queryBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(taskRun.getTaskRunId().toString()))
            .andExpect(jsonPath("$.state").value("created"))
            .andExpect(jsonPath("$.task").isNotEmpty());
    }

    @Test
    @Transactional
    public void shouldProvisionSingleParameterOfTask() throws Exception {
        TaskRun taskRun = builder.givenANotPersistedTaskRun();
        taskRunRepository.saveAndFlush(taskRun);
        UUID taskRunId = taskRun.getTaskRunId();

        String parameterName = "my_param";
        String queryBody = "{\"value\": 0, \"parameterName\": \""
            + parameterName
            + "\", \"type\": { \"id\" : \"integer\"}}";
        String mockResponse = "{\"value\": 0, \"parameterName\": \""
            + parameterName
            + "\", \"taskRunId\": \""
            + taskRunId
            + "\"}";
        String appEngineUriSection = "task-runs/" + taskRunId + "/input-provisions/" + parameterName;
        WiremockRepository.SERVER.stubFor(WireMock.put(urlEqualTo(apiBasePath + appEngineUriSection))
            .willReturn(aResponse().withBody(mockResponse))
        );

        mockMvc.perform(put("/api/app-engine/project/" + taskRun.getProject().getId() + "/" + appEngineUriSection)
                .contentType(MediaType.APPLICATION_JSON)
                .content(queryBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.taskRunId").value(taskRunId.toString()))
            .andExpect(jsonPath("$.parameterName").value(parameterName))
            .andExpect(jsonPath("$.value").value(0));
    }

    @Test
    @Transactional
    public void shouldProvisionBatchParametersOfTask() throws Exception {
        TaskRun taskRun = builder.givenANotPersistedTaskRun();
        taskRunRepository.saveAndFlush(taskRun);
        UUID taskRunId = taskRun.getTaskRunId();

        List<TaskRunProvisionedResponse> mockResponses = List.of(
            new TaskRunProvisionedResponse("first-parameter", taskRun.getTaskRunId(), 0),
            new TaskRunProvisionedResponse("second-parameter", taskRun.getTaskRunId(), 2)
        );

        List<Map<String, Object>> queryBody = mockResponses.stream()
            .map(r -> Map.of(
                "parameterName", r.parameterName(),
                "value", r.value(),
                "type", Map.of("id", "integer")
            ))
            .toList();

        mockResponses.forEach(wiremockRepository::stubProvisionParameter);

        String uri = UriComponentsBuilder.fromPath("/api/app-engine/project/")
            .pathSegment(taskRun.getProject().getId().toString(), "task-runs", taskRunId.toString(), "input-provisions")
            .toUriString();

        mockMvc.perform(put(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(queryBody)))
            .andExpect(status().isOk())
            .andExpect(result -> {
                List<TaskRunProvisionedResponse> actual = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, TaskRunProvisionedResponse.class)
                );
                assertThat(actual).containsExactlyElementsOf(mockResponses);
            });
    }

    @Test
    @Transactional
    public void getTaskRun() throws Exception {
        TaskRun taskRun = builder.givenANotPersistedTaskRun();
        taskRunRepository.saveAndFlush(taskRun);
        UUID taskRunId = taskRun.getTaskRunId();
        String mockResponse = getTaskRunBody(taskRunId);
        String appEngineUriSection = "task-runs/" + taskRunId;
        WiremockRepository.SERVER.stubFor(WireMock.get(urlEqualTo(apiBasePath + appEngineUriSection))
            .willReturn(aResponse().withBody(mockResponse))
        );

        mockMvc.perform(get("/api/app-engine/project/" + taskRun.getProject().getId() + "/" + appEngineUriSection)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("id").value(taskRunId.toString()))
            .andExpect(jsonPath("state").value("CREATED"))
            .andExpect(jsonPath("task").isNotEmpty());
    }

    @Test
    @Transactional
    public void postStateAction() throws Exception {
        TaskRun taskRun = builder.givenANotPersistedTaskRun();
        taskRunRepository.saveAndFlush(taskRun);
        UUID taskRunId = taskRun.getTaskRunId();
        String queryBody = "{\"desired\": \"running\"}";
        String mockResponse = getTaskRunBody(taskRunId);
        String appEngineUriSection = "task-runs/" + taskRunId + "/state-actions";
        WiremockRepository.SERVER.stubFor(WireMock.post(urlEqualTo(apiBasePath + appEngineUriSection))
            .willReturn(aResponse().withBody(mockResponse))
        );

        mockMvc.perform(post("/api/app-engine/project/" + taskRun.getProject().getId() + "/" + appEngineUriSection)
                .contentType(MediaType.APPLICATION_JSON)
                .content(queryBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("id").value(taskRunId.toString()))
            .andExpect(jsonPath("state").value("CREATED"))
            .andExpect(jsonPath("task").isNotEmpty());
    }

    protected String getTaskRunBody(UUID taskRunId) {
        Map<String, Object> task = Map.of(
            "name", "string",
            "namespace", "string",
            "version", "string",
            "description", "string",
            "authors", List.of(
                Map.of(
                    "first_name", "string",
                    "last_name", "string",
                    "organization", "string",
                    "email", "string",
                    "is_contact", true
                )
            )
        );

        Map<String, Object> taskRunBody = Map.of(
            "task", task,
            "id", taskRunId.toString(),
            "state", "CREATED",
            "createdAt", "2023-12-20T10:49:21.272Z",
            "updatedAt", "2023-12-20T10:49:21.272Z"
        );

        try {
            return objectMapper.writeValueAsString(taskRunBody);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
