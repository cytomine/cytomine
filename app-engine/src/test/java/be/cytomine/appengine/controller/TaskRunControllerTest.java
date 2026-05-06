package be.cytomine.appengine.controller;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import be.cytomine.appengine.controllers.TaskRunController;
import be.cytomine.appengine.dto.inputs.task.TaskRunParameterValue;
import be.cytomine.appengine.handlers.SchedulerHandler;
import be.cytomine.appengine.models.task.ValueType;
import be.cytomine.appengine.repositories.RunRepository;
import be.cytomine.appengine.services.RunService;
import be.cytomine.appengine.services.TaskProvisioningService;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskRunController.class)
class TaskRunControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RunRepository runRepository;

    @MockitoBean
    private RunService runService;

    @MockitoBean
    private SchedulerHandler schedulerHandler;

    @MockitoBean
    private TaskProvisioningService taskProvisioningService;

    @Value("${app-engine.api_prefix}")
    private String apiPrefix;

    @Value("${app-engine.api_version}")
    private String apiVersion;

    private String baseUrl() {
        return apiPrefix + apiVersion + "/task-runs";
    }

    @Test
    void getRunOutputsListShouldReturnOkWithOutputs() throws Exception {
        String runId = randomUUID().toString();
        List<TaskRunParameterValue> outputs = List.of(
            new TaskRunParameterValue(randomUUID(), "output1", ValueType.STRING),
            new TaskRunParameterValue(randomUUID(), "output2", ValueType.STRING)
        );
        when(taskProvisioningService.retrieveRunOutputs(runId)).thenReturn(outputs);

        mockMvc.perform(get(baseUrl() + "/" + runId + "/outputs").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(outputs)));
    }
}
