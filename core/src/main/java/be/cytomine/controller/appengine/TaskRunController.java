package be.cytomine.controller.appengine;

import be.cytomine.service.appengine.TaskRunService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;
import java.util.UUID;

@Slf4j
@ConditionalOnExpression("${application.appEngine.enabled: false}")
@RequiredArgsConstructor
@RequestMapping("/api/app-engine")
@RestController
public class TaskRunController {

    private final TaskRunService taskRunService;

    @PostMapping("/project/{project}/tasks/{task}/runs")
    public String add(
        @PathVariable Long project,
        @PathVariable UUID task,
        @RequestBody JsonNode body
    ) {
        return taskRunService.addTaskRun(project, "tasks/" + task + "/runs", body);
    }

    @PostMapping("/project/{project}/tasks/{namespace}/{version}/runs")
    public String add(
        @PathVariable Long project,
        @PathVariable String namespace,
        @PathVariable String version,
        @RequestBody JsonNode body
    ) {
        String uri = "tasks/" + namespace + "/" + version + "/runs";
        return taskRunService.addTaskRun(project, uri, body);
    }

    @GetMapping("/project/{project}/task-runs/{task}")
    public String get(
        @PathVariable Long project,
        @PathVariable UUID task
    ) {
        return taskRunService.getTaskRun(project, task);
    }

    @PutMapping("/project/{project}/task-runs/{task}/input-provisions")
    public String batchProvision(
        @PathVariable Long project,
        @PathVariable UUID task,
        @RequestBody List<JsonNode> body
    ) {
        return taskRunService.batchProvisionTaskRun(body, project, task);
    }

    @PutMapping(
        value = "/project/{project}/task-runs/{task}/input-provisions/{parameter_name}",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public String provision(
        @PathVariable Long project,
        @PathVariable UUID task,
        @PathVariable("parameter_name") String parameterName,
        @RequestBody JsonNode json
    ) {
        return taskRunService.provisionTaskRun(json, project, task, parameterName);
    }

    @PutMapping(
        value = "/project/{project}/task-runs/{task}/input-provisions/{parameter_name}",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public String provision(
            @PathVariable Long project,
            @PathVariable UUID task,
            @PathVariable("parameter_name") String parameterName,
            @RequestParam MultipartFile file
    ) {
        return taskRunService.provisionBinaryData(file, project, task, parameterName);
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
            HttpServletResponse response
    ) {
        File file = taskRunService.getTaskRunIOParameter(project, task, parameterName, "input");
        try (InputStream is = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + parameterName + "\"");
            response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            response.setHeader(HttpHeaders.PRAGMA, "no-cache");
            response.setHeader(HttpHeaders.EXPIRES, "0");

            is.transferTo(os);
            os.flush();
            file.delete();
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
            HttpServletResponse response
    ) {
        File file = taskRunService.getTaskRunIOParameter(project, task, parameterName, "output");
        try (InputStream is = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + parameterName + "\"");
            response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            response.setHeader(HttpHeaders.PRAGMA, "no-cache");
            response.setHeader(HttpHeaders.EXPIRES, "0");

            is.transferTo(os);
            os.flush();
            file.delete();
        } catch (IOException e) {
            throw new RuntimeException("Error while streaming the output parameter", e);
        }
    }
}
