package be.cytomine.service.appengine;

import be.cytomine.domain.annotation.AnnotationLayer;
import be.cytomine.domain.appengine.TaskRun;
import be.cytomine.domain.appengine.TaskRunLayer;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.ontology.AnnotationDomain;
import be.cytomine.domain.ontology.UserAnnotation;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.dto.appengine.task.TaskRunDetail;
import be.cytomine.dto.appengine.task.TaskRunOutputResponse;
import be.cytomine.dto.appengine.task.TaskRunResponse;
import be.cytomine.dto.appengine.task.TaskRunValue;
import be.cytomine.dto.appengine.task.type.CollectionType;
import be.cytomine.dto.appengine.task.type.GeometryType;
import be.cytomine.dto.appengine.task.type.TaskParameterType;
import be.cytomine.dto.image.CropParameter;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.repository.appengine.TaskRunLayerRepository;
import be.cytomine.repository.appengine.TaskRunRepository;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.annotation.AnnotationLayerService;
import be.cytomine.service.annotation.AnnotationService;
import be.cytomine.service.image.ImageInstanceService;
import be.cytomine.service.middleware.ImageServerService;
import be.cytomine.service.ontology.UserAnnotationService;
import be.cytomine.service.project.ProjectService;
import be.cytomine.service.security.SecurityACLService;
import be.cytomine.service.utils.GeometryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.springframework.security.acls.domain.BasePermission.READ;

@Slf4j
@RequiredArgsConstructor
@Service
public class TaskRunService {

    private final AnnotationService annotationService;

    private final AnnotationLayerService annotationLayerService;

    private final AppEngineService appEngineService;

    private final CurrentUserService currentUserService;

    private final GeometryService geometryService;

    private final ImageInstanceService imageInstanceService;

    private final ImageServerService imageServerService;

    private final ProjectService projectService;

    private final SecurityACLService securityACLService;

    private final UserAnnotationService userAnnotationService;

    private final TaskRunRepository taskRunRepository;

    private final TaskRunLayerRepository taskRunLayerRepository;

    private final ObjectMapper objectMapper;

    public String addTaskRun(Long projectId, String taskId, JsonNode body) {
        Project project = projectService.get(projectId);
        User currentUser = currentUserService.getCurrentUser();
        securityACLService.checkUser(currentUser);
        securityACLService.check(project, READ);
        securityACLService.checkIsNotReadOnly(project);

        String appEngineResponse = appEngineService.post("/tasks/" + taskId + "/runs", null, MediaType.APPLICATION_JSON);

        TaskRunResponse taskRunResponse;
        try {
            taskRunResponse = objectMapper.readValue(appEngineResponse, TaskRunResponse.class);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error parsing JSON response");
        }
        ImageInstance image = imageInstanceService.get(body.get("image").asLong());

        TaskRun taskRun = new TaskRun();
        taskRun.setUser(currentUser);
        taskRun.setProject(project);
        taskRun.setTaskRunId(taskRunResponse.id());
        taskRun.setImage(image);
        taskRun = taskRunRepository.saveAndFlush(taskRun);

        List<TaskRunOutputResponse> taskRunOutputResponse;
        String taskOutputsResponse = appEngineService.get("/tasks/" + taskId + "/outputs");
        try {
            taskRunOutputResponse = objectMapper.readValue(taskOutputsResponse, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error parsing JSON response");
        }

        boolean hasGeometry = false;
        for (TaskRunOutputResponse taskRunOutput : taskRunOutputResponse) {
            TaskParameterType type = taskRunOutput.type();

            if (type instanceof GeometryType) {
                hasGeometry = true;
                break;
            }

            if (type instanceof CollectionType array && array.subType() instanceof GeometryType) {
                hasGeometry = true;
                break;
            }
        }

        if (hasGeometry) {
            String layerName = annotationLayerService.createLayerName(taskRunResponse.task().name(), taskRunResponse.task().version(), taskRun.getCreated());
            AnnotationLayer annotationLayer = annotationLayerService.createAnnotationLayer(layerName);
            TaskRunLayer newLayer = new TaskRunLayer();
            newLayer.setAnnotationLayer(annotationLayer);
            newLayer.setTaskRun(taskRun);
            newLayer.setImage(taskRun.getImage());
            newLayer.setXOffset(0);
            newLayer.setYOffset(0);
            taskRunLayerRepository.saveAndFlush(newLayer);
        }

        // We return the App engine response. Should we include information from Cytomine (project ID, user ID, created, ... ?)
        return appEngineResponse;
    }

    public List<TaskRunDetail> getTaskRuns(Long projectId) {
        User currentUser = currentUserService.getCurrentUser();
        Project project = projectService.find(projectId)
            .orElseThrow(() -> new ObjectNotFoundException("Project", projectId));

        securityACLService.checkUser(currentUser);
        securityACLService.check(project, READ);

        List<TaskRun> taskRuns = taskRunRepository.findAllByProjectId(projectId);

        return taskRuns.stream()
            .map(taskRun -> new TaskRunDetail(
                taskRun.getProject().getId(),
                taskRun.getUser().getId(),
                taskRun.getImage().getId(),
                taskRun.getTaskRunId().toString(),
                taskRun.getCreated()
            ))
            .toList();
    }

    private void checkTaskRun(Long projectId, UUID taskRunId) {
        Optional<TaskRun> taskRun = taskRunRepository.findByProjectIdAndTaskRunId(projectId, taskRunId);
        if (taskRun.isEmpty()) {
            throw new ObjectNotFoundException("TaskRun", taskRunId);
        }

        User currentUser = currentUserService.getCurrentUser();
        Project project = projectService.get(projectId);

        securityACLService.checkUser(currentUser);
        securityACLService.check(project, READ);
        securityACLService.checkIsNotReadOnly(project);
    }

    private List<JsonNode> processProvisions(List<JsonNode> json) {
        List<JsonNode> requestBody = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        for (JsonNode provision : json) {
            ObjectNode processedProvision = provision.deepCopy();
            processedProvision.remove("type");

            // Process the input if it is an annotation type
            if (provision.get("type").get("id").asText().equals("geometry")) {
                Long annotationId = provision.get("value").asLong();
                UserAnnotation annotation = userAnnotationService.get(annotationId);
                processedProvision.put("value", geometryService.WKTToGeoJSON(annotation.getWktLocation()));
            }

            if (provision.get("type").get("id").asText().equals("array") && provision.get("value").isArray()) {
                int index = 0;
                ArrayNode valueListNode = mapper.createArrayNode();
                boolean subTypeIsGeometry = provision.get("type").get("subType").get("id").asText().equals("geometry");
                for (JsonNode element : provision.get("value")) {
                    ObjectNode itemJsonObject = mapper.createObjectNode();
                    itemJsonObject.put("index", index);

                    if (subTypeIsGeometry) {
                        Long annotationId = element.asLong();
                        UserAnnotation annotation = userAnnotationService.get(annotationId);
                        itemJsonObject.put("value", geometryService.WKTToGeoJSON(annotation.getWktLocation()));
                    } else {
                        itemJsonObject.set("value", element);
                    }
                    valueListNode.add(itemJsonObject);
                    index++;
                }
                processedProvision.set("value", valueListNode);
            }

            requestBody.add(processedProvision);
        }

        return requestBody;
    }

    public String batchProvisionTaskRun(List<JsonNode> requestBody, Long projectId, UUID taskRunId) {
        checkTaskRun(projectId, taskRunId);
        List<JsonNode> body = processProvisions(requestBody);
        return appEngineService.put("task-runs/" + taskRunId + "/input-provisions", body, MediaType.APPLICATION_JSON);
    }

    private byte[] getImageAnnotation(AnnotationDomain annotation) {
        CropParameter parameters = new CropParameter();
        parameters.setComplete(true);
        parameters.setDraw(true);
        parameters.setFormat("png");
        parameters.setLocation(annotation.getWktLocation());

        try {
            ResponseEntity<byte[]> response = imageServerService.crop(annotation, parameters, null, null);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Image server returned error status: " + response.getStatusCode());
            }

            byte[] imageData = response.getBody();
            if (imageData == null || imageData.length == 0) {
                throw new RuntimeException("Image server returned empty response for annotation " + annotation.getId());
            }

            return imageData;
        } catch (Exception e) {
            log.error("Failed to get annotation crop for annotation {}", annotation.getId(), e);
            throw new RuntimeException("Unable to process annotation: " + e.getMessage(), e);
        }
    }

    public String provisionTaskRun(JsonNode json, Long projectId, UUID taskRunId, String parameterName)
        throws JsonProcessingException {
        checkTaskRun(projectId, taskRunId);

        String uri = "task-runs/" + taskRunId + "/input-provisions/" + parameterName;
        String arrayTypeUri = "task-runs/" + taskRunId + "/input-provisions/" + parameterName + "/indexes";
        ObjectMapper mapper = new ObjectMapper();
        File wsi = null;
        if (json.get("type").isObject() && json.get("type").get("id").asText().equals("array")) {
            String subtype = json.get("type").get("subType").get("id").asText();

            JsonNode value = json.get("value");
            String type = value.get("type").asText();

            Long[] itemsArray = mapper.convertValue(value.get("ids"), Long[].class);

            if (subtype.equals("image")) {
                ArrayNode responseArray = mapper.createArrayNode();
                for (int i = 0; i < itemsArray.length; i++) {
                    Long imageId = itemsArray[i];
                    if (type.equalsIgnoreCase("annotation")) {
                        MultiValueMap<String, Object> body = prepareImage(imageId, "annotation");

                        String response = provisionCollectionItem(arrayTypeUri, i, body);
                        if (response != null) {
                            JsonNode itemNode = mapper.readTree(response);
                            responseArray.add(itemNode);
                        }
                    }
                    if (type.equalsIgnoreCase("image")) {
                        MultiValueMap<String, Object> body = prepareImage(imageId, "image");

                        String response = provisionCollectionItem(arrayTypeUri, i, body);
                        if (response != null) {
                            JsonNode itemNode = mapper.readTree(response);
                            responseArray.add(itemNode);
                        }
                    }
                }
                return responseArray.toString();
            }

            if (subtype.equals("geometry")) {
                ObjectNode provision = json.deepCopy();
                provision.remove("type");
                provision.remove("value");

                ArrayNode valueListNode = mapper.createArrayNode();
                for (int i = 0; i < itemsArray.length; i++) {
                    Long annotationId = itemsArray[i];
                    UserAnnotation annotation = userAnnotationService.get(annotationId);

                    ObjectNode itemJsonObject = mapper.createObjectNode();
                    itemJsonObject.put("index", i);
                    itemJsonObject.put("value", geometryService.WKTToGeoJSON(annotation.getWktLocation()));

                    valueListNode.add(itemJsonObject);
                }
            }
        }

        if (json.get("type").get("id").asText().equals("image")) {
            JsonNode value = json.get("value");
            String type = Objects.nonNull(value.get("type")) ? value.get("type").asText() : null;
            Long id = Objects.nonNull(value.get("id")) ? value.get("id").asLong() : null;

            MultiValueMap<String, Object> body;
            if (Objects.nonNull(type))
            {
                if (type.equals("annotation")) {
                    UserAnnotation annotation = userAnnotationService.get(id);
                    Envelope bounds = GeometryService.getBounds(annotation.getWktLocation());

                    TaskRun taskRun =
                        taskRunRepository.findByProjectIdAndTaskRunId(projectId, taskRunId)
                            .orElseThrow(() -> new ObjectNotFoundException("TaskRun", taskRunId));

                    Optional<TaskRunLayer> optionalTaskRunLayer = taskRunLayerRepository
                        .findByTaskRunAndImage(taskRun, taskRun.getImage());
                    if (optionalTaskRunLayer.isPresent()) {
                        TaskRunLayer taskRunLayer = optionalTaskRunLayer.get();
                        taskRunLayer.setXOffset((int) bounds.getMinX());
                        taskRunLayer.setYOffset((int) bounds.getMinY());
                        taskRunLayerRepository.saveAndFlush(taskRunLayer);
                    }

                    body = prepareImage(id, "annotation");
                } else if (type.equals("image")) {
                    wsi = downloadWsi(id);

                    body = new LinkedMultiValueMap<>();
                    body.add("file", new FileSystemResource(wsi));
                } else {
                    throw new IllegalArgumentException("Unsupported type: " + type);
                }


                String response = appEngineService.post(uri, body, MediaType.MULTIPART_FORM_DATA);

                if (wsi != null) {
                    wsi.delete();
                }

                return response;
            }
        }

        ObjectNode provision = json.deepCopy();
        if (provision.get("type").get("id").asText().equals("geometry")) {
            Long annotationId = provision.get("value").asLong();
            UserAnnotation annotation = userAnnotationService.get(annotationId);
            provision.put("value", geometryService.WKTToGeoJSON(annotation.getWktLocation()));
        }

        provision.remove("type");

        return appEngineService.put(uri, provision, MediaType.APPLICATION_JSON);
    }

    private String provisionCollectionItem(String arrayTypeUri, int i,
                                                           MultiValueMap<String, Object> body)
    {
        Map<String, String> params = new HashMap<>();
        params.put("value", String.valueOf(i));

        return appEngineService.postWithParams(arrayTypeUri, body, MediaType.MULTIPART_FORM_DATA, params);

    }

    private MultiValueMap<String, Object> prepareImage(Long id, String type) {

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        if (type.equals("annotation")) {
            UserAnnotation annotation = userAnnotationService.get(id);
            byte[] imageData = getImageAnnotation(annotation);

            body.add("file", new ByteArrayResource(imageData) {
                @Override
                public String getFilename() {
                    return id + ".png";
                }
            });
        }
        if (type.equals("image")) {
            File wsi = downloadWsi(id);

            body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(wsi));
        }
        return body;
    }

    public File downloadFile(URI uri, File destinationFile) {
        ResponseExtractor<Void> responseExtractor = response -> {
            try (InputStream in = response.getBody();
                 OutputStream out = new FileOutputStream(destinationFile)) {
                StreamUtils.copy(in, out);
                return null;
            }
        };

        new RestTemplate().execute(uri, HttpMethod.GET, null, responseExtractor);

        return destinationFile;
    }

    private File downloadWsi(Long imageId) {
        ImageInstance ii = imageInstanceService.find(imageId)
            .orElseThrow(() -> new ObjectNotFoundException("ImageInstance", imageId));

        String imagePath = URLEncoder
            .encode(ii.getBaseImage().getPath(), StandardCharsets.UTF_8)
            .replace("%2F", "/");

        URI uri = UriComponentsBuilder
            .fromHttpUrl(imageServerService.internalImageServerURL())
            .pathSegment("image", imagePath, "export")
            .queryParam("filename", ii.getBaseImage().getOriginalFilename())
            .build()
            .toUri();

        Path filePath = Paths.get(ii.getBaseImage().getOriginalFilename());

        return downloadFile(uri, filePath.toFile());
    }

    public String provisionBinaryData(MultipartFile file, Long projectId, UUID taskRunId, String parameterName) {
        checkTaskRun(projectId, taskRunId);

        String uri = "task-runs/" + taskRunId + "/input-provisions/" + parameterName;

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        return appEngineService.put(uri, body, MediaType.MULTIPART_FORM_DATA);
    }

    public String getTaskRun(Long projectId, UUID taskRunId) {
        checkTaskRun(projectId, taskRunId);
        return appEngineService.get("task-runs/" + taskRunId);
    }

    public String postStateAction(JsonNode body, Long projectId, UUID taskRunId) {
        checkTaskRun(projectId, taskRunId);
        return appEngineService.post("task-runs/" + taskRunId + "/state-actions", body,
            MediaType.APPLICATION_JSON);
    }

    public String getOutputs(Long projectId, UUID taskRunId) {
        checkTaskRun(projectId, taskRunId);

        String response = appEngineService.get("task-runs/" + taskRunId + "/outputs");
        TaskRun taskRun = taskRunRepository.findByProjectIdAndTaskRunId(projectId, taskRunId)
                .orElseThrow(() -> new ObjectNotFoundException("TaskRun", taskRunId));

        List<TaskRunValue> outputs;
        try {
            outputs = new ObjectMapper().readValue(response, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new ObjectNotFoundException("Outputs from", taskRunId);
        }

        List<String> geometries = outputs
            .stream()
                .map(TaskRunValue::getValue)
            .filter(value -> value instanceof String geometry && geometryService.isGeometry(geometry))
            .map(value -> (String) value)
            .toList();

        String taskRunData = appEngineService.get("task-runs/" + taskRunId);
        TaskRunResponse taskRunResponse;
        try {
            taskRunResponse = new ObjectMapper().readValue(taskRunData, TaskRunResponse.class);
        } catch(JsonProcessingException e) {
            throw new ObjectNotFoundException("Task run", taskRunId);
        }

        String layerName = annotationLayerService.createLayerName(taskRunResponse.task().name(), taskRunResponse.task().version(), taskRun.getCreated());
        AnnotationLayer annotationLayer = annotationLayerService.createAnnotationLayer(layerName);
        TaskRunLayer taskRunLayer = taskRunLayerRepository
                .findByTaskRunAndImage(taskRun, taskRun.getImage())
                .orElse(new TaskRunLayer());

        for (String geometry : geometries) {
            String wktGeometry = geometryService.GeoJSONToWKT(geometry);
            Geometry parsedGeometry = GeometryService.addOffset(wktGeometry, taskRunLayer.getXOffset(), taskRunLayer.getYOffset());
            annotationService.createAnnotation(annotationLayer, parsedGeometry.toString());
        }

        List<TaskRunValue> geoArrayValues = outputs
                .stream()
                .filter(output -> output.getType().equals("ARRAY")
                && output.getSubType().equalsIgnoreCase("GEOMETRY"))
                .toList();

        if (!geoArrayValues.isEmpty()) {
            for (TaskRunValue arrayValue : geoArrayValues) {
                    JsonNode items = new ObjectMapper().convertValue(arrayValue.getValue(), JsonNode.class);
                    for (JsonNode item : items) {
                        if (geometryService.isGeometry(item.get("value").asText())) {
                            String wktGeometry = geometryService.GeoJSONToWKT(item.get("value").asText());
                            Geometry parsedGeometry = GeometryService.addOffset(wktGeometry, taskRunLayer.getXOffset(), taskRunLayer.getYOffset());
                            annotationService.createAnnotation(annotationLayer, parsedGeometry.toString());
                        }
                    }
            }
        }

        return response;
    }

    public String getInputs(Long projectId, UUID taskRunId) {
        checkTaskRun(projectId, taskRunId);
        return appEngineService.get("task-runs/" + taskRunId + "/inputs");
    }

    public File getTaskRunIOParameter(Long projectId, UUID taskRunId, String parameterName, String type) {
        checkTaskRun(projectId, taskRunId);
        return appEngineService.getStreamedFile("task-runs/" + taskRunId + "/" + type + "/" + parameterName);
    }
}
