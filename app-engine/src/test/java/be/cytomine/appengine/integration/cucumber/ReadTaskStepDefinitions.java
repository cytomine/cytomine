package be.cytomine.appengine.integration.cucumber;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestClientResponseException;

import be.cytomine.appengine.AppEngineApplication;
import be.cytomine.appengine.dto.handlers.filestorage.Storage;
import be.cytomine.appengine.dto.inputs.task.TaskDescription;
import be.cytomine.appengine.exceptions.FileStorageException;
import be.cytomine.appengine.handlers.StorageData;
import be.cytomine.appengine.handlers.StorageHandler;
import be.cytomine.appengine.models.task.Parameter;
import be.cytomine.appengine.models.task.ParameterType;
import be.cytomine.appengine.models.task.Run;
import be.cytomine.appengine.models.task.Task;
import be.cytomine.appengine.repositories.RunRepository;
import be.cytomine.appengine.repositories.TaskRepository;
import be.cytomine.appengine.states.TaskRunState;
import be.cytomine.appengine.utils.ApiClient;
import be.cytomine.appengine.utils.DescriptorHelper;
import be.cytomine.appengine.utils.TaskTestsUtils;
import be.cytomine.appengine.utils.TestTaskBuilder;

@ContextConfiguration(classes = AppEngineApplication.class, loader = SpringBootContextLoader.class)
public class ReadTaskStepDefinitions {

    @LocalServerPort
    private String port;

    @Autowired
    private ApiClient apiClient;

    @Autowired
    private RunRepository taskRunRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private StorageHandler storageHandler;

    @Value("${app-engine.api_prefix}")
    private String apiPrefix;

    @Value("${app-engine.api_version}")
    private String apiVersion;

    private String persistedNamespace;

    private String persistedVersion;

    private String persistedUUID;

    private List<Parameter> persistedInputs;

    private List<Parameter> persistedOutputs;

    private File persistedDescriptor;

    private Task persistedTask;

    private TaskDescription persistedTaskDescription;

    private List<TaskDescription> tasks;

    private RestClientResponseException persistedException;

    private Resource resource;

    private Run persistedRun;

    @Before
    public void setUp() {
        apiClient.setBaseUrl("http://localhost:" + port + apiPrefix + apiVersion);
        apiClient.setPort(port);
    }

    @Given("a set of valid tasks has been successfully uploaded")
    public void validTasksAreUploaded() {
        taskRepository.save(TestTaskBuilder.buildHardcodedAddInteger());
        taskRepository.save(TestTaskBuilder.buildHardcodedSubtractInteger());
    }

    @When("user calls the endpoint {string} \\(excluding version prefix, e.g. {string}) with HTTP method {string}")
    public void userCallsEndpointWithMethod(String uri, String string2, String method) {
        tasks = apiClient.getTasks();
    }

    @Then("App Engine retrieves relevant data from the database")
    public void appEngineRetrievesData() {
        Assertions.assertNotNull(tasks);
        Assertions.assertEquals(2, tasks.size());
    }

    @Then("App Engine sends a {string} OK response with a payload containing the descriptions "
        + "of the available tasks as a JSON payload \\(see OpenAPI spec)")
    public void appEngineReturnsTaskListAsJson(String string) {
        for (TaskDescription description : tasks) {
            Assertions.assertNotNull(description.description());
            Assertions.assertNotNull(description.namespace());
            Assertions.assertNotNull(description.version());
            Assertions.assertNotNull(description.name());
            Assertions.assertNotNull(description.authors());
            Assertions.assertFalse(description.authors().isEmpty());
        }
    }

    private void createDescriptorInStorage(String bundleFilename, Task task) throws FileStorageException {
        // save it in file storage service
        Storage storage = new Storage(task.getStorageReference());
        if (!storageHandler.checkStorageExists(storage)) {
            storageHandler.createStorage(storage);
        }

        // save file using defined storage reference
        persistedDescriptor = TestTaskBuilder.getDescriptorFromBundleResource(bundleFilename);
        Assertions.assertNotNull(persistedDescriptor);

        StorageData fileData = new StorageData(persistedDescriptor, "descriptor.yml");
        storageHandler.saveStorageData(storage, fileData);
    }

    @Given("a valid task has a {string}, a {string} has been successfully uploaded")
    public void taskWithNamespaceAndVersionIsUploaded(String namespace, String version) throws FileStorageException {
        taskRepository.deleteAll();
        String bundleFilename = namespace + "-" + version + ".zip";
        persistedTask = TestTaskBuilder.buildTaskFromResource(bundleFilename);
        persistedTask = taskRepository.save(persistedTask);

        Storage storage = new Storage(persistedTask.getStorageReference());
        if (storageHandler.checkStorageExists(storage)) {
            storageHandler.deleteStorage(storage);
        }
        createDescriptorInStorage(bundleFilename, persistedTask);
    }

    @Given("a valid task has a {string} has been successfully uploaded")
    public void taskWithUuidIsUploaded(String uuid) throws FileStorageException {
        taskRepository.deleteAll();
        String bundleFilename = "com.cytomine.dummy.arithmetic.integer.subtraction-1.0.0.zip";
        persistedTask = TestTaskBuilder.buildTaskFromResource(bundleFilename, UUID.fromString(uuid));
        taskRepository.save(persistedTask);

        // clean storage
        Storage storage = new Storage(persistedTask.getStorageReference());
        if (storageHandler.checkStorageExists(storage)) {
            storageHandler.deleteStorage(storage);
        }
        createDescriptorInStorage(bundleFilename, persistedTask);
    }

    @Given("a valid task has a {string}, a {string} and {string} has been successfully uploaded")
    public void taskWithNamespaceVersionAndUuidIsUploaded(
        String namespace,
        String version,
        String uuid
    ) throws FileStorageException {
        taskRepository.deleteAll();
        String bundleFilename = namespace + "-" + version + ".zip";
        persistedTask = TestTaskBuilder.buildTaskFromResource(bundleFilename, UUID.fromString(uuid));
        taskRepository.save(persistedTask);

        // clean storage
        Storage storage = new Storage(persistedTask.getStorageReference());
        if (storageHandler.checkStorageExists(storage)) {
            storageHandler.deleteStorage(storage);
        }
        createDescriptorInStorage(bundleFilename, persistedTask);
    }

    @Then("App Engine retrieves task with {string}, a {string}  from the database")
    public void appEngineRetrievesTaskByNamespaceAndVersion(String namespace, String version) {
    }

    @Then("App Engine retrieves task inputs with {string}, a {string}  from the database")
    public void appEngineRetrievesTaskInputsByNamespaceAndVersion(String namespace, String version) {
        Assertions.assertNotNull(persistedInputs);
        Assertions.assertFalse(persistedInputs.isEmpty());
    }

    @Then("App Engine retrieves task with {string} from the database")
    public void appEngineRetrievesTaskByUuid(String uuid) {
    }

    @Then("App Engine retrieves task inputs with {string} from the database")
    public void appEngineRetrievesTaskInputsByUuid(String uuid) {
        // a Input Parameters is received
        Assertions.assertNotNull(persistedInputs);
        Assertions.assertFalse(persistedInputs.isEmpty());
        Assertions.assertEquals(2, persistedInputs.size());
    }

    @When("user calls the endpoint {string} with {string} and {string} with HTTP method GET")
    public void userCallsEndpointWithNamespaceAndVersion(String uri, String namespace, String version) {
        persistedTaskDescription = apiClient.getTask(namespace, version);
    }

    @When("user calls the endpoint {string} with id {string} HTTP method GET")
    public void userCallsEndpointWithUuid(String uri, String uuid) {
        persistedTaskDescription = apiClient.getTask(uuid);
    }

    @Then("App Engine sends a {string} OK response with a payload containing "
        + "the task description as a JSON payload \\(see OpenAPI spec)")
    public void appEngineReturnsTaskDescriptionAsJson(String string) {
        Assertions.assertNotNull(persistedTaskDescription);
        Assertions.assertEquals(persistedTaskDescription.namespace(), persistedTask.getNamespace());
        Assertions.assertEquals(persistedTaskDescription.version(), persistedTask.getVersion());
        Assertions.assertEquals(persistedTaskDescription.name(), persistedTask.getName());
        Assertions.assertEquals(persistedTaskDescription.authors().size(), persistedTask.getAuthors().size());
    }

    @Then("App Engine sends a {string} OK response with a payload containing "
        + "the task inputs as a JSON payload \\(see OpenAPI spec)")
    public void appEngineReturnsTaskInputsAsJson(String string) {
        Assertions.assertNotNull(persistedInputs);
        Set<Parameter> persistedTaskInputs = persistedTask
            .getParameters()
            .stream()
            .filter(parameter -> parameter.getParameterType().equals(ParameterType.INPUT))
            .collect(Collectors.toSet());
        Assertions.assertTrue(TaskTestsUtils.areSetEquals(persistedTaskInputs, persistedInputs));
    }

    @When("user calls the endpoint {string} with {string} and {string} HTTP method GET")
    public void userCallsHttpEndpointWithNamespaceAndVersion(String endpoint, String namespace, String version) {
        persistedInputs = apiClient.getInputs(namespace, version);
    }

    @When("user calls the outputs endpoint {string} with {string} and {string} HTTP method GET")
    public void userCallsOutputsEndpointWithNamespaceAndVersion(String endpoint, String namespace, String version) {
        persistedOutputs = apiClient.getOutputs(namespace, version);
    }

    @When("user calls the endpoint {string} with {string} HTTP method GET")
    public void userCallsHttpEndpointWithUuid(String endpoint, String uuid) {
        persistedInputs = apiClient.getInputs(uuid);
    }

    @When("user calls the outputs endpoint {string} with {string} HTTP method GET")
    public void userCallsOutputsEndpointWithUuid(String endpoint, String uuid) {
        persistedOutputs = apiClient.getOutputs(uuid);
    }

    @When("user calls the download endpoint with {string} and {string} with HTTP method GET")
    public void userCallsDownloadEndpointWithNamespaceAndVersion(String namespace, String version) {
        persistedDescriptor = apiClient.getTaskDescriptor(namespace, version);
    }

    @Given("the task descriptor is stored in the file storage service in storage {string} under filename {string}")
    public void taskDescriptorIsStoredInFileStorage(
        String storageReference,
        String descriptorFileName
    ) throws FileStorageException, IOException {
        // save it in file storage service
        Storage storage = new Storage(persistedTask.getStorageReference());
        Assertions.assertTrue(storageHandler.checkStorageExists(storage));

        File tempFile = Files.createTempFile(descriptorFileName, null).toFile();
        tempFile.deleteOnExit();

        StorageData emptyFile = new StorageData(tempFile, descriptorFileName);
        emptyFile.peek().setName("descriptor.yml");
        emptyFile.peek().setStorageId(storage.id());
        storageHandler.readStorageData(emptyFile);
        Assertions.assertTrue(Files.size(emptyFile.peek().getData().toPath()) > 0);
    }

    @When("user calls the download endpoint with {string} with HTTP method GET")
    public void userCallsDownloadEndpointWithUuid(String uuid) {
        persistedDescriptor = apiClient.getTaskDescriptor(uuid);
    }

    @Then("App Engine retrieves the descriptor file {string} from the file storage")
    public void appEngineRetrievesDescriptorFromStorage(String fileName) {
        // make sure descriptor is not null
        Assertions.assertNotNull(persistedDescriptor);
    }

    @Then("App Engine sends a {string} OK response with the descriptor file as a binary payload \\(see OpenAPI spec)")
    public void appEngineReturnsDescriptorAsBinary(String string) {
        Assertions.assertNotNull(persistedDescriptor);
        JsonNode descriptorJson = DescriptorHelper.parseDescriptor(persistedDescriptor);
        Assertions.assertTrue(descriptorJson.has("namespace"));
        Assertions.assertTrue(descriptorJson.has("version"));
        Assertions.assertEquals(persistedTask.getNamespace(), descriptorJson.get("namespace").textValue());
        Assertions.assertEquals(persistedTask.getVersion(), descriptorJson.get("version").textValue());
    }

    @Then("App Engine retrieves task outputs with {string}, a {string}  from the database")
    public void appEngineRetrievesTaskOutputsByNamespaceAndVersion(String namespace, String version) {
        Assertions.assertNotNull(persistedOutputs);
        Set<Parameter> persistedTaskOutputs = persistedTask
            .getParameters()
            .stream()
            .filter(parameter -> parameter.getParameterType().equals(ParameterType.OUTPUT))
            .collect(Collectors.toSet());
        Assertions.assertTrue(TaskTestsUtils.areSetEquals(persistedTaskOutputs, persistedOutputs));
    }

    @Then("App Engine sends a {string} OK response with a payload containing "
        + "the task outputs as a JSON payload \\(see OpenAPI spec)")
    public void appEngineReturnsTaskOutputsAsJson(String string) {
        Assertions.assertNotNull(persistedOutputs);
        Set<Parameter> persistedTaskOutputs = persistedTask
            .getParameters()
            .stream()
            .filter(parameter -> parameter.getParameterType().equals(ParameterType.OUTPUT))
            .collect(Collectors.toSet());
        Assertions.assertTrue(TaskTestsUtils.areSetEquals(persistedTaskOutputs, persistedOutputs));
    }

    @Then("App Engine retrieves task outputs with {string} from the database")
    public void appEngineRetrievesTaskOutputsByUuid(String string) {
        Assertions.assertNotNull(persistedOutputs);
        Set<Parameter> persistedTaskOutputs = persistedTask
            .getParameters()
            .stream()
            .filter(parameter -> parameter.getParameterType().equals(ParameterType.OUTPUT))
            .collect(Collectors.toSet());
        Assertions.assertTrue(TaskTestsUtils.areSetEquals(persistedTaskOutputs, persistedOutputs));
    }

    @Given("a task unknown to the App Engine has a {string} and a {string} and a {string}")
    public void unknownTaskWithNamespaceVersionAndUuid(String namespace, String version, String uuid) {
        // just make sure database is empty and doesn't contain the referenced tasks
        taskRepository.deleteAll();
        persistedNamespace = namespace;
        persistedVersion = version;
        persistedUUID = uuid;
    }

    @When("user calls the fetch endpoint {string} with HTTP method {string}")
    public void userCallsFetchEndpointWithMethod(String endpoint, String method) {
        // call the correct endpoint based on URI

        try {
            switch (endpoint) {
                case "/task/namespace/version/outputs":
                    apiClient.getTaskOutputs(persistedNamespace, persistedVersion);
                    break;
                case "/task/id/outputs":
                    apiClient.getTaskOutputs(persistedUUID);
                    break;
                case "/task/namespace/version/inputs":
                    apiClient.getTaskInputs(persistedNamespace, persistedVersion);
                    break;
                case "/task/id/inputs":
                    apiClient.getTaskInputs(persistedUUID);
                    break;
                case "/task/namespace/version":
                    apiClient.getTask(persistedNamespace, persistedVersion);
                    break;
                case "/task/id":
                    apiClient.getTask(persistedUUID);
                    break;
                case "/task/namespace/version/descriptor.yml":
                    apiClient.getTaskDescriptor(persistedNamespace, persistedVersion);
                    break;
                case "/task/id/descriptor.yml":
                    apiClient.getTaskDescriptor(persistedUUID);
                    break;
                default:
                    throw new RuntimeException("Unknown endpoint");
            }
        } catch (RestClientResponseException e) {
            persistedException = e;
        }
    }

    @Then("App Engine sends a {string} HTTP error with a standard error payload containing code {string}")
    public void appEngineReturnsHttpError(
        String expectedStatusCode,
        String appEngineErrorCode
    ) throws JsonProcessingException {
        // make sure it's a 404 response
        String actualStatusCode = String.valueOf(persistedException.getStatusCode().value());
        Assertions.assertEquals(expectedStatusCode, actualStatusCode);

        // reply with expected error code
        JsonNode jsonPayLoad = new ObjectMapper().readTree(persistedException.getResponseBodyAsString());
        Assertions.assertTrue(jsonPayLoad.get("error_code").textValue().startsWith(appEngineErrorCode));
    }

    @When("user calls the endpoint {string} with {string} and {int} HTTP method GET")
    public void userCallsTheEndpointWithAndAndHTTPMethodGET(String link, String inputName, int indexes) {

        String uuid = persistedRun.getId().toString();
        try {
            resource = apiClient.retrieveInputPart(uuid, inputName, indexes);
        } catch (RestClientResponseException e) {
            persistedException = e;
        }

    }

    @And("input {string} with collection item with index {int} is already provisioned")
    public void inputWithCollectionItemWithIndexIsAlreadyProvisioned(String inputName, int indexes) {
        String uuid = persistedRun.getId().toString();
        apiClient.provisionInputPart(uuid, inputName, "integer", "100", indexes);
    }

    @Then("App Engine sends a {string} OK response with a payload containing the task input")
    public void appEngineSendsAOKResponseWithAPayloadContainingTheTaskInput(String code) {
        Assertions.assertNotNull(resource);
    }

    @Given("a new task run has been created for this task")
    public void taskRunIsCreated() throws FileStorageException {
        persistedRun = new Run(UUID.randomUUID(), TaskRunState.CREATED, null);
        persistedRun = taskRunRepository.save(persistedRun);
        persistedRun.setTask(persistedTask);
        persistedRun = taskRunRepository.saveAndFlush(persistedRun);
        Storage runStorage = new Storage("task-run-inputs-" + persistedRun.getId().toString());
        storageHandler.createStorage(runStorage);
        runStorage = new Storage("task-run-outputs-" + persistedRun.getId().toString());
        storageHandler.createStorage(runStorage);
    }
}
