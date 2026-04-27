package be.cytomine.service.appengine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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

import be.cytomine.domain.annotation.AnnotationLayer;
import be.cytomine.domain.appengine.CropOffset;
import be.cytomine.domain.appengine.TaskRun;
import be.cytomine.domain.appengine.TaskRunLayer;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.ontology.AnnotationDomain;
import be.cytomine.domain.ontology.UserAnnotation;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.dto.UserSummary;
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

    private final AsyncService asyncService;

    private boolean containsGeometry(TaskParameterType type) {
        if (type instanceof GeometryType) {
            return true;
        }
        if (type instanceof CollectionType collection) {
            return containsGeometry(collection.subType());
        }
        return false;
    }

    public String addTaskRun(Long projectId, String taskId, JsonNode body) {
        Project project = projectService.get(projectId);
        User currentUser = currentUserService.getCurrentUser();
        securityACLService.checkUser(currentUser);
        securityACLService.check(project, READ);
        securityACLService.checkIsNotReadOnly(project);

        String appEngineResponse = appEngineService.post(
            "/tasks/" + taskId + "/runs",
            null,
            MediaType.APPLICATION_JSON
        );

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

        Set<TaskRunOutputResponse> outputResponses = taskRunOutputResponse.stream()
            .filter(output -> containsGeometry(output.type()))
            .collect(Collectors.toSet());

        for (TaskRunOutputResponse output : outputResponses) {
            String layerName = annotationLayerService.createLayerName(
                taskRunResponse.task().name(),
                taskRunResponse.task().version(),
                taskRun.getCreated()
            );
            AnnotationLayer annotationLayer = annotationLayerService.createAnnotationLayer(layerName);
            TaskRunLayer newLayer = new TaskRunLayer();
            newLayer.setAnnotationLayer(annotationLayer);
            newLayer.setTaskRun(taskRun);
            newLayer.setImage(taskRun.getImage());
            newLayer.setParameterName(output.name());
            newLayer.setDerivedFrom(output.derivedFrom());
            taskRunLayerRepository.saveAndFlush(newLayer);
        }

        // We return the App engine response.
        // Should we include information from Cytomine (project ID, user ID, created, ... ?)
        return appEngineResponse;
    }

    public void deleteAllTaskRunForTask(String taskId) {
        log.info("Deleting all task runs associated to task '{}'", taskId);

        String response = appEngineService.get("/tasks/" + taskId + "/runs");
        List<TaskRunResponse> taskRunResponses;
        try {
            taskRunResponses = objectMapper.readValue(response, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error parsing JSON response");
        }

        for (TaskRunResponse taskRun : taskRunResponses) {
            log.info("Delete task run '{}'", taskRun.id());
            taskRunRepository.deleteTaskRunByTaskRunId(taskRun.id());
        }

        log.info("Deleted all task runs associated to task '{}'", taskId);
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
                new UserSummary(
                    taskRun.getUser().getId(),
                    taskRun.getUser().getUsername(),
                    taskRun.getUser().getName()
                ),
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

    private ObjectNode processProvision(JsonNode provision, Long projectId, UUID taskRunId) {
        ObjectNode processedProvision = provision.deepCopy();
        processedProvision.remove("type");

        String typeId = provision.get("type").get("id").asText();
        String parameterName = provision.get("param_name").asText();

        if (typeId.equals("geometry") && !provision.get("value").isNull()) {
            Long annotationId = provision.get("value").asLong();
            UserAnnotation annotation = userAnnotationService.get(annotationId);
            processedProvision.put("value", geometryService.wktToGeoJson(annotation.getWktLocation()));

            Envelope bounds = GeometryService.getBounds(annotation.getWktLocation());
            TaskRun taskRun = taskRunRepository.findByProjectIdAndTaskRunId(projectId, taskRunId)
                .orElseThrow(() -> new ObjectNotFoundException("TaskRun", taskRunId));

            saveCropOffset(taskRun, parameterName, bounds);
        }

        if (typeId.equals("array") && provision.get("value").isArray()) {
            ArrayNode valueListNode = objectMapper.createArrayNode();
            boolean subTypeIsGeometry = provision.get("type").get("subType").get("id").asText().equals("geometry");

            if (!provision.get("value").isNull()) {
                int index = 0;
                for (JsonNode element : provision.get("value")) {
                    ObjectNode itemJsonObject = objectMapper.createObjectNode();
                    itemJsonObject.put("index", index);

                    if (subTypeIsGeometry) {
                        Long annotationId = element.asLong();
                        UserAnnotation annotation = userAnnotationService.get(annotationId);
                        itemJsonObject.put("value", geometryService.wktToGeoJson(annotation.getWktLocation()));
                        Envelope bounds = GeometryService.getBounds(annotation.getWktLocation());

                        TaskRun taskRun = taskRunRepository.findByProjectIdAndTaskRunId(projectId, taskRunId)
                            .orElseThrow(() -> new ObjectNotFoundException("TaskRun", taskRunId));

                        saveCropOffset(taskRun, parameterName, bounds);
                    } else {
                        itemJsonObject.set("value", element);
                    }

                    valueListNode.add(itemJsonObject);
                    index++;
                }
            }

            processedProvision.set("value", valueListNode);
        }

        return processedProvision;
    }

    public String batchProvisionTaskRun(List<JsonNode> provisions, Long projectId, UUID taskRunId) {
        checkTaskRun(projectId, taskRunId);
        List<JsonNode> body = provisions.stream()
            .map(provision -> processProvision(provision, projectId, taskRunId))
            .collect(Collectors.toList());
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

    private MultiValueMap<String, Object> prepareAnnotationBody(Long id, UserAnnotation annotation, Envelope bounds) {
        byte[] imageData = getImageAnnotation(annotation);

        int xOffset = (int) -bounds.getMinX();
        int yOffset = (int) -bounds.getMinY();
        Geometry shifted = GeometryService.addOffset(annotation.getWktLocation(), xOffset, yOffset);
        String geometry = geometryService.wktToGeoJson(shifted.toText());

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add(
            "file", new ByteArrayResource(imageData) {
                @Override
                public String getFilename() {
                    return id + ".png";
                }
            }
        );
        body.add(
            "location", new ByteArrayResource(geometry.getBytes(StandardCharsets.UTF_8)) {
                @Override
                public String getFilename() {
                    return id + ".geojson";
                }
            }
        );
        return body;
    }

    private void saveCropOffset(TaskRun taskRun, String parameterName, Envelope bounds) {
        TaskRunLayer taskRunLayer = taskRunLayerRepository
            .findByTaskRunAndDerivedFrom(taskRun, parameterName)
            .orElseThrow(() -> new RuntimeException("Task run layer not found for " + parameterName));
        taskRunLayer.getOffsets().add(new CropOffset((int) bounds.getMinX(), (int) bounds.getMinY()));
        taskRunLayerRepository.saveAndFlush(taskRunLayer);
    }

    public String provisionTaskRun(JsonNode json, Long projectId, UUID taskRunId, String parameterName)
        throws JsonProcessingException {
        checkTaskRun(projectId, taskRunId);

        String uri = "task-runs/" + taskRunId + "/input-provisions/" + parameterName;
        String arrayTypeUri = uri + "/indexes";
        if (json.get("type").isObject() && json.get("type").get("id").asText().equals("array")) {
            String subtype = json.get("type").get("subType").get("id").asText();

            JsonNode value = json.get("value");
            if (!json.get("value").isNull()) {
                String type = value.get("type").asText();

                Long[] itemsArray = objectMapper.convertValue(value.get("ids"), Long[].class);

                if (subtype.equals("image")) {
                    ArrayNode responseArray = objectMapper.createArrayNode();
                    for (int i = 0; i < itemsArray.length; i++) {
                        Long id = itemsArray[i];
                        if (type.equalsIgnoreCase("annotation")) {
                            UserAnnotation annotation = userAnnotationService.get(id);
                            Envelope bounds = GeometryService.getBounds(annotation.getWktLocation());

                            TaskRun taskRun = taskRunRepository.findByProjectIdAndTaskRunId(projectId, taskRunId)
                                .orElseThrow(() -> new ObjectNotFoundException("TaskRun", taskRunId));

                            MultiValueMap<String, Object> body = prepareAnnotationBody(id, annotation, bounds);

                            String response = provisionCollectionItem(arrayTypeUri, i, body);
                            if (response != null) {
                                JsonNode itemNode = objectMapper.readTree(response);
                                responseArray.add(itemNode);
                            }

                            saveCropOffset(taskRun, parameterName, bounds);
                        }
                        if (type.equalsIgnoreCase("image")) {
                            File wsi = downloadWsi(id);

                            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                            body.add("file", new FileSystemResource(wsi));

                            String response = provisionCollectionItem(arrayTypeUri, i, body);
                            if (response != null) {
                                JsonNode itemNode = objectMapper.readTree(response);
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

                    ArrayNode valueListNode = objectMapper.createArrayNode();
                    for (int i = 0; i < itemsArray.length; i++) {
                        Long annotationId = itemsArray[i];
                        UserAnnotation annotation = userAnnotationService.get(annotationId);

                        ObjectNode itemJsonObject = objectMapper.createObjectNode();
                        itemJsonObject.put("index", i);
                        itemJsonObject.put("value", geometryService.wktToGeoJson(annotation.getWktLocation()));

                        valueListNode.add(itemJsonObject);
                    }
                }
            }
        }

        if (json.get("type").get("id").asText().equals("image")) {
            File wsi = null;
            JsonNode value = json.get("value");
            if (value.isNull()) {
                throw new RuntimeException("value cannot be null");
            }

            String type = value.path("type").asText(null);
            long id = value.path("id").asLong(0);

            if (type == null) {
                throw new RuntimeException("type cannot be null");
            }

            MultiValueMap<String, Object> body;
            switch (type) {
                case "annotation" -> {
                    UserAnnotation annotation = userAnnotationService.get(id);
                    Envelope bounds = GeometryService.getBounds(annotation.getWktLocation());

                    TaskRun taskRun = taskRunRepository.findByProjectIdAndTaskRunId(projectId, taskRunId)
                        .orElseThrow(() -> new ObjectNotFoundException("TaskRun", taskRunId));

                    saveCropOffset(taskRun, parameterName, bounds);

                    body = prepareAnnotationBody(id, annotation, bounds);
                }

                case "image" -> {
                    wsi = downloadWsi(id);
                    body = new LinkedMultiValueMap<>();
                    body.add("file", new FileSystemResource(wsi));
                }

                default -> throw new IllegalArgumentException("Unsupported type: " + type);
            }

            String response = appEngineService.post(uri, body, MediaType.MULTIPART_FORM_DATA);

            if (wsi != null) {
                wsi.delete();
            }

            return response;
        }

        ObjectNode provision = json.deepCopy();
        if (provision.get("type").get("id").asText().equals("geometry")) {
            if (!provision.get("value").isNull()) {
                Long annotationId = provision.get("value").asLong();
                UserAnnotation annotation = userAnnotationService.get(annotationId);
                provision.put("value", geometryService.wktToGeoJson(annotation.getWktLocation()));
            }
        }

        provision.remove("type");

        return appEngineService.put(uri, provision, MediaType.APPLICATION_JSON);
    }

    private String provisionCollectionItem(
        String arrayTypeUri, int i,
        MultiValueMap<String, Object> body
    ) {
        Map<String, String> params = new HashMap<>();
        params.put("value", String.valueOf(i));

        return appEngineService.postWithParams(arrayTypeUri, body, MediaType.MULTIPART_FORM_DATA, params);

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
            .fromUriString(imageServerService.internalImageServerURL())
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
        return appEngineService.post("task-runs/" + taskRunId + "/state-actions", body, MediaType.APPLICATION_JSON);
    }

    private void processGeometryValue(
        TaskRunValue value,
        AnnotationLayer annotationLayer,
        TaskRunLayer taskRunLayer,
        int index
    ) {
        if (!"ARRAY".equals(value.getType())) {
            return;
        }

        List<?> items = (List<?>) value.getValue();
        if (items == null || items.isEmpty()) {
            return;
        }

        if ("GEOMETRY".equals(value.getSubType())) {
            List<CropOffset> offsets = taskRunLayer.getOffsets();
            CropOffset offset = index < offsets.size() ? offsets.get(index) : new CropOffset();

            JsonNode jsonItems = objectMapper.convertValue(items, JsonNode.class);
            for (JsonNode item : jsonItems) {
                String geoJson = item.get("value").asText();
                if (geometryService.isGeometry(geoJson)) {
                    String wktGeometry = geometryService.geoJsonToWkt(geoJson);
                    Geometry parsedGeometry = GeometryService.addOffset(wktGeometry, offset.getX(), offset.getY());
                    annotationService.createAnnotation(annotationLayer, parsedGeometry.toString());
                }
            }
        } else if ("ARRAY".equals(value.getSubType())) {
            List<TaskRunValue> innerValues = objectMapper.convertValue(items, new TypeReference<>() {});

            for (int i = 0; i < items.size(); i++) {
                processGeometryValue(innerValues.get(i), annotationLayer, taskRunLayer, i);
            }
        }
    }

    private boolean hasGeometrySubType(TaskRunValue value) {
        if (!"ARRAY".equals(value.getType())) {
            return false;
        }

        String subType = value.getSubType();
        if ("GEOMETRY".equals(subType)) {
            return true;
        }

        if ("ARRAY".equals(subType)) {
            if (!(value.getValue() instanceof List<?> innerList)) {
                return false;
            }

            return innerList.stream()
                .map(item -> objectMapper.convertValue(item, TaskRunValue.class))
                .anyMatch(this::hasGeometrySubType);
        }

        return false;
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

        // pull the images and store them in the project
        asyncService.launchImageAdditionJob(outputs, projectId, currentUserService.getCurrentUser());

        String taskRunData = appEngineService.get("task-runs/" + taskRunId);
        TaskRunResponse taskRunResponse;
        try {
            taskRunResponse = new ObjectMapper().readValue(taskRunData, TaskRunResponse.class);
        } catch (JsonProcessingException e) {
            throw new ObjectNotFoundException("Task run", taskRunId);
        }

        String layerName = annotationLayerService.createLayerName(
            taskRunResponse.task().name(),
            taskRunResponse.task().version(),
            taskRun.getCreated()
        );
        AnnotationLayer annotationLayer = annotationLayerService.createAnnotationLayer(layerName);
        if (!annotationLayer.getAnnotations().isEmpty()) {
            return response;
        }

        Set<TaskRunLayer> taskRunLayers = taskRunLayerRepository.findAllByTaskRunAndImage(taskRun, taskRun.getImage());
        Map<String, TaskRunLayer> layersByParameterName = taskRunLayers.stream()
            .collect(Collectors.toMap(TaskRunLayer::getParameterName, Function.identity()));

        Set<TaskRunValue> geometries = outputs
            .stream()
            .filter(v -> v.getValue() instanceof String geometry && geometryService.isGeometry(geometry))
            .collect(Collectors.toSet());

        for (TaskRunValue geometry : geometries) {
            TaskRunLayer matchedLayer = layersByParameterName.get(geometry.getParameterName());
            CropOffset offset = matchedLayer.getOffsets().get(0);
            String wktGeometry = geometryService.geoJsonToWkt((String) geometry.getValue());
            Geometry parsedGeometry = GeometryService.addOffset(wktGeometry, offset.getX(), offset.getY());
            annotationService.createAnnotation(annotationLayer, parsedGeometry.toString());
        }

        List<TaskRunValue> geometryArrays = outputs
            .stream()
            .filter(this::hasGeometrySubType)
            .toList();

        for (TaskRunValue arrayValue : geometryArrays) {
            TaskRunLayer matchedLayer = layersByParameterName.get(arrayValue.getParameterName());
            processGeometryValue(arrayValue, annotationLayer, matchedLayer, 0);
        }

        return response;
    }

    public String getInputs(Long projectId, UUID taskRunId) {
        checkTaskRun(projectId, taskRunId);
        return appEngineService.get("task-runs/" + taskRunId + "/inputs");
    }

    public void getTaskRunIOParameter(
        Long projectId,
        UUID taskRunId,
        String parameterName,
        String type,
        OutputStream outputStream
    ) {
        checkTaskRun(projectId, taskRunId);
        appEngineService.getStreamedFile("task-runs/" + taskRunId + "/" + type + "/" + parameterName, outputStream);
    }
}
