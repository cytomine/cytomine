package be.cytomine.appengine.controller;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;

import be.cytomine.appengine.controllers.TaskRunController;
import be.cytomine.appengine.dto.inputs.task.TaskRunParameterValue;
import be.cytomine.appengine.dto.inputs.task.types.bool.BooleanValue;
import be.cytomine.appengine.dto.inputs.task.types.collection.CollectionItemValue;
import be.cytomine.appengine.dto.inputs.task.types.collection.CollectionValue;
import be.cytomine.appengine.dto.inputs.task.types.datetime.DateTimeValue;
import be.cytomine.appengine.dto.inputs.task.types.enumeration.EnumerationValue;
import be.cytomine.appengine.dto.inputs.task.types.integer.IntegerValue;
import be.cytomine.appengine.dto.inputs.task.types.number.NumberValue;
import be.cytomine.appengine.dto.inputs.task.types.string.StringValue;
import be.cytomine.appengine.handlers.SchedulerHandler;
import be.cytomine.appengine.models.task.ValueType;
import be.cytomine.appengine.repositories.RunRepository;
import be.cytomine.appengine.services.RunService;
import be.cytomine.appengine.services.TaskProvisioningService;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
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

    private static TaskRunParameterValue buildOutput(TaskRunParameterValue value, ValueType type) {
        value.setTaskRunId(randomUUID());
        value.setParameterName(randomUUID().toString());
        value.setType(type);
        return value;
    }

    private static Stream<Arguments> primitiveOutputValueProvider() {
        return Stream.of(
            Arguments.of(buildOutput(new BooleanValue(true), ValueType.BOOLEAN)),
            Arguments.of(buildOutput(new IntegerValue(42), ValueType.INTEGER)),
            Arguments.of(buildOutput(new NumberValue(4.2), ValueType.NUMBER)),
            Arguments.of(buildOutput(new StringValue("hello"), ValueType.STRING)),
            Arguments.of(buildOutput(new EnumerationValue("enum"), ValueType.ENUMERATION)),
            Arguments.of(buildOutput(new DateTimeValue(Instant.now()), ValueType.DATETIME))
        );
    }

    @ParameterizedTest
    @MethodSource("primitiveOutputValueProvider")
    void getRunOutputsShouldReturnOkWithPrimitiveOutputs(TaskRunParameterValue output) throws Exception {
        String runId = randomUUID().toString();
        List<TaskRunParameterValue> outputs = List.of(output);
        when(taskProvisioningService.retrieveRunOutputs(runId)).thenReturn(outputs);

        mockMvc.perform(get(baseUrl() + "/" + runId + "/outputs").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(outputs), JsonCompareMode.STRICT));
    }

    @Test
    void getRunOutputsShouldReturnOkWithAllPrimitiveOutputs() throws Exception {
        String runId = randomUUID().toString();
        List<TaskRunParameterValue> outputs = primitiveOutputValueProvider()
            .map(args -> (TaskRunParameterValue) args.get()[0])
            .toList();
        when(taskProvisioningService.retrieveRunOutputs(runId)).thenReturn(outputs);

        mockMvc.perform(get(baseUrl() + "/" + runId + "/outputs").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(outputs), JsonCompareMode.STRICT));
    }

    @Test
    void getRunOutputsShouldReturnOkWithNestedGeometryCollectionOutputs() throws Exception {
        String runId = randomUUID().toString();

        CollectionValue geometryInner = new CollectionValue(
            List.of(
                new CollectionItemValue(
                    0,
                    "{\"type\": \"Polygon\", \"coordinates\": [[[36.295784, 52.0], [35.856007, 50.437344]]]}"
                ),
                new CollectionItemValue(
                    1,
                    "{\"type\": \"Polygon\", \"coordinates\": [[[78.462036, 52.0], [77.688614, 50.470638]]]}"
                ),
                new CollectionItemValue(
                    2,
                    "{\"type\": \"Polygon\", \"coordinates\": [[[93.146004, 40.0], [92.848824, 38.239861]]]}"
                ),
                new CollectionItemValue(
                    3,
                    "{\"type\": \"Polygon\", \"coordinates\": [[[52.529213, 2.0], [52.444271, 0.320328]]]}"
                )
            ),
            ValueType.GEOMETRY
        );

        // Outer geometry CollectionValue (subType=ARRAY)
        CollectionValue geometryOuter = (CollectionValue) buildOutput(new CollectionValue(), ValueType.ARRAY);
        geometryOuter.setValue(List.of(geometryInner));
        geometryOuter.setSubType(ValueType.ARRAY);

        // Inner number CollectionValue (subType=NUMBER)
        CollectionValue numberInner = new CollectionValue(
            List.of(
                new CollectionItemValue(0, 0.8604097f),
                new CollectionItemValue(1, 0.85321754f),
                new CollectionItemValue(2, 0.83373106f),
                new CollectionItemValue(3, 0.56986123f)
            ),
            ValueType.NUMBER
        );

        CollectionValue numberOuter = (CollectionValue) buildOutput(new CollectionValue(), ValueType.ARRAY);
        numberOuter.setValue(List.of(numberInner));
        numberOuter.setSubType(ValueType.ARRAY);

        List<TaskRunParameterValue> outputs = List.of(geometryOuter, numberOuter);
        when(taskProvisioningService.retrieveRunOutputs(runId)).thenReturn(outputs);

        String result = mockMvc.perform(get(baseUrl() + "/" + runId + "/outputs")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(outputs), JsonCompareMode.STRICT))
            .andReturn()
            .getResponse()
            .getContentAsString();

        // outer CollectionValue MUST have parent fields
        assertThat(result).contains("\"parameterName\":\"" + geometryOuter.getParameterName() + "\"");
        assertThat(result).contains("\"taskRunId\":\"" + geometryOuter.getTaskRunId() + "\"");
        assertThat(result).contains("\"parameterName\":\"" + numberOuter.getParameterName() + "\"");
        assertThat(result).contains("\"taskRunId\":\"" + numberOuter.getTaskRunId() + "\"");

        // CollectionItemValue MUST NOT have parent fields (intentionally ignored)
        // CollectionItemValue MUST have index and value
        JsonNode root = objectMapper.readTree(result);
        for (int outerIndex = 0; outerIndex < root.size(); outerIndex++) {
            JsonNode items = root.get(outerIndex).get("value").get(0).get("value");
            for (JsonNode item : items) {
                assertThat(item.has("taskRunId")).isFalse();
                assertThat(item.has("type")).isFalse();
                assertThat(item.has("parameterName")).isFalse();
                assertThat(item.has("index")).isTrue();
                assertThat(item.has("value")).isTrue();
            }
        }
    }
}
