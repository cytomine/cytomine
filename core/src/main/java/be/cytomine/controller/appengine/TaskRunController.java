package be.cytomine.controller.appengine;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import be.cytomine.repository.appengine.TaskRunRepository;
import be.cytomine.service.appengine.AppEngineService;
import be.cytomine.service.appengine.TaskRunService;

@Slf4j
@ConditionalOnExpression("${application.appEngine.enabled: false}")
@RequiredArgsConstructor
@RequestMapping("/api/app-engine")
@RestController
public class TaskRunController {

    private final AppEngineService appEngineService;

    private final TaskRunRepository taskRunRepository;

    private final TaskRunService taskRunService;

    @PostMapping("/project/{project}/tasks/{task}/runs")
    public String add(
        @PathVariable Long project,
        @PathVariable UUID task,
        @RequestBody JsonNode body
    ) {
        return taskRunService.addTaskRun(project, task.toString(), body);
    }

    @PostMapping("/project/{project}/tasks/{namespace}/{version}/runs")
    public String add(
        @PathVariable Long project,
        @PathVariable String namespace,
        @PathVariable String version,
        @RequestBody JsonNode body
    ) {
        return taskRunService.addTaskRun(project, namespace + "/" + version, body);
    }

    @GetMapping("/project/{project}/task-runs/{task}")
    public String get(
        @PathVariable Long project,
        @PathVariable UUID task
    ) {
        return taskRunService.getTaskRun(project, task);
    }

    @PreAuthorize("authentication.name == 'admin'")
    @DeleteMapping("/project/{projectId}/task-runs/{runId}")
    public void deleteTask(@PathVariable long projectId, @PathVariable UUID runId) {
        log.info("DELETE /project/{}/task-runs/{}", projectId, runId);

        taskRunRepository.deleteTaskRunByTaskRunId(runId);
        appEngineService.delete("task-runs/" + runId);

        log.info("DELETE /project/{}/task-runs/{} - ENDED", projectId, runId);
    }

    @PutMapping("/project/{projectId}/task-runs/{taskRunId}/input-provisions")
    public List<String> batchProvision(
        @PathVariable Long projectId,
        @PathVariable UUID taskRunId,
        @RequestBody List<JsonNode> body
    ) {
        log.info("PUT Batch /project/{}/task-runs/{}/input-provisions", projectId, taskRunId);
        return taskRunService.batchProvisionTaskRun(body, projectId, taskRunId);
    }

    @PutMapping(
        value = "/project/{projectId}/task-runs/{taskRunId}/input-provisions/{parameterName}",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public String provision(
        @PathVariable Long projectId,
        @PathVariable UUID taskRunId,
        @PathVariable String parameterName,
        @RequestBody JsonNode json
    ) throws JsonProcessingException {
        log.info("PUT JSON /project/{}/task-runs/{}/input-provisions/{}", projectId, taskRunId, parameterName);
        return taskRunService.provisionTaskRun(json, projectId, taskRunId, parameterName);
    }

    @PutMapping(
        value = "/project/{projectId}/task-runs/{taskRunId}/input-provisions/{parameterName}",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public String provision(
        @PathVariable Long projectId,
        @PathVariable UUID taskRunId,
        @PathVariable String parameterName,
        @RequestParam MultipartFile file
    ) {
        log.info("PUT DATA /project/{}/task-runs/{}/input-provisions/{}", projectId, taskRunId, parameterName);
        return taskRunService.provisionBinaryData(file, projectId, taskRunId, parameterName);
    }

    @PostMapping("/project/{project}/task-runs/{task}/state-actions")
    public String stateAction(
        @PathVariable Long project,
        @PathVariable UUID task,
        @RequestBody JsonNode body
    ) {
        return taskRunService.postStateAction(body, project, task);
    }

    @GetMapping("/project/{project}/task-runs/{task}/inputs")
    public String getInputs(
        @PathVariable Long project,
        @PathVariable UUID task
    ) {
        return taskRunService.getInputs(project, task);
    }

    @GetMapping("/project/{project}/task-runs/{task}/input/{parameter_name}")
    public void getTaskRunInputParameter(
        @PathVariable Long project,
        @PathVariable UUID task,
        @PathVariable("parameter_name") String parameterName,
        @RequestParam(required = true) String auth, // don't remove this parameter, it's used by the security filter'
        HttpServletResponse response
    ) {
        if (parameterName.endsWith(".geojson")) {
            response.setContentType("application/geo+json");
        } else {
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        }
        response.setHeader(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + parameterName + "\""
        );
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
        response.setHeader(HttpHeaders.EXPIRES, "0");

        try (OutputStream os = response.getOutputStream()) {
            taskRunService.getTaskRunIOParameter(project, task, parameterName, "input", os);
            os.flush();
        } catch (IOException e) {
            throw new RuntimeException("Error while streaming the input parameter", e);
        }
    }

    @GetMapping("/project/{project}/task-runs/{task}/outputs")
    public String getOutputs(
        @PathVariable Long project,
        @PathVariable UUID task
    ) {
        log.info("GET /project/{}/task-runs/{}/outputs", project, task);
        return taskRunService.getOutputs(project, task);
    }

    @GetMapping("/project/{project}/task-runs/{task}/output/{parameter_name}")
    public void getTaskRunOutputParameter(
        @PathVariable Long project,
        @PathVariable UUID task,
        @PathVariable("parameter_name") String parameterName,
        @RequestParam(required = true) String auth, // don't remove this parameter, it's used by the security filter
        HttpServletResponse response
    ) {

        if (parameterName.endsWith(".geojson")) {
            response.setContentType("application/geo+json");
        } else {
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        }
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + parameterName + "\"");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
        response.setHeader(HttpHeaders.EXPIRES, "0");

        try (OutputStream os = response.getOutputStream()) {

            taskRunService.getTaskRunIOParameter(project, task, parameterName, "output", os);
            os.flush();
        } catch (IOException e) {
            throw new RuntimeException("Error while streaming the output parameter", e);
        }
    }

    @GetMapping("/project/{projectId}/task-runs/{runId}/logs")
    public String getTaskRunLogs(@PathVariable Long projectId, @PathVariable UUID runId) {
        log.info("GET /project/{}/task-runs/{}/logs", projectId, runId);
        return appEngineService.get("task-runs/" + runId + "/logs");
    }
}
