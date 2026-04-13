package be.cytomine.appengine.integration.cucumber;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

import com.cytomine.registry.client.RegistryClient;
import com.cytomine.registry.client.http.resp.CatalogResp;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestClientResponseException;

import be.cytomine.appengine.AppEngineApplication;
import be.cytomine.appengine.dto.handlers.filestorage.Storage;
import be.cytomine.appengine.dto.inputs.task.TaskDescription;
import be.cytomine.appengine.dto.responses.errors.ErrorCode;
import be.cytomine.appengine.dto.responses.errors.ErrorDefinitions;
import be.cytomine.appengine.exceptions.FileStorageException;
import be.cytomine.appengine.handlers.StorageData;
import be.cytomine.appengine.handlers.StorageHandler;
import be.cytomine.appengine.models.task.Task;
import be.cytomine.appengine.repositories.TaskRepository;
import be.cytomine.appengine.utils.ApiClient;
import be.cytomine.appengine.utils.FileHelper;
import be.cytomine.appengine.utils.TestTaskBuilder;

@ContextConfiguration(classes = AppEngineApplication.class, loader = SpringBootContextLoader.class)
public class UploadTaskStepDefinitions {

    @LocalServerPort
    private String port;

    @Autowired
    private ApiClient apiClient;

    @Autowired
    private StorageHandler storageHandler;

    @Autowired
    private TaskRepository taskRepository;

    @Value("${app-engine.api_prefix}")
    private String apiPrefix;

    @Value("${app-engine.api_version}")
    private String apiVersion;

    @Value("${registry.url}")
    private String registry;

    private String persistedNamespace;

    private String persistedVersion;

    private ClassPathResource persistedBundle;

    private RestClientResponseException persistedException;

    private TaskDescription persistedUploadResponse;

    private Task persistedTask;

    private Task uploaded;

    @Before
    public void setUp() {
        apiClient.setBaseUrl("http://localhost:" + port + apiPrefix + apiVersion);
        apiClient.setPort(port);
    }

    @Given("App Engine is up and running")
    public void appEngineIsHealthy() {
        ResponseEntity<String> health = apiClient.checkHealth();
        Assertions.assertTrue(health.getStatusCode().is2xxSuccessful());
        taskRepository.deleteAll();
    }

    @Given("File storage service is up and running")
    public void fileStorageServiceIsHealthy() throws FileStorageException {
        // ping the file storage service health endpoint and make sure we get no errors
        // as long as it responds it means it's up and running
        storageHandler.checkStorageExists("random");
    }

    @Given("Registry service is up and running")
    public void registryServiceIsHealthy() throws IOException {
        try {
            RegistryClient.delete(
                "registry:5000/img@sha256:d53ef00848a227ce64ce71cd7cceb7184fd1f116e0202289b26a576cf87dc4cb"
            );
        } catch (IOException ignored) {
            // Ignored
        }
    }

    @Given("a task uniquely identified by an {string} and a {string}")
    public void taskIdentifiedBy(String namespace, String version) {
        this.persistedNamespace = namespace;
        this.persistedVersion = version;
    }

    @Given("this task identified by an {string} and a {string} is not yet known to the App Engine")
    public void taskIsUnknownToAppEngine(String namespace, String version) {
        taskRepository.deleteAll();
    }

    @Given("this task is represented by a zip archive containing a task descriptor file and a docker image")
    public void taskIsPackagedAsZipArchive() {
        // make sure valid test archive is in classpath
        String bundleFilename = persistedNamespace + "-" + persistedVersion + ".zip";
        persistedBundle = TestTaskBuilder.buildByBundleFilename(bundleFilename);
        Assertions.assertNotNull(persistedBundle);
    }

    @Given("the task descriptor is a YAML file stored in the zip archive named {string} "
        + "structured following an agreed descriptor schema")
    public void taskDescriptorIsYamlNamed(String string) {
        // test archive contains valid yml file and following the agreed schema
    }

    @Given("the Docker image stored in the zip archive is represented by a tar file of which the "
        + "relative path is {string}")
    public void dockerImageIsTarAtPath(String string) {
        // archive contains docker image in tar path
    }

    @Given("{string} is either defined by the field {string} in the task descriptor or, if this "
        + "field is missing, is {string}")
    public void fieldIsDefinedOrDefaultsTo(String string, String string2, String string3) {
        // docker image location is defined in field configuration.image.file in descriptor.yml
        // already in artifacts
    }

    @When("user calls POST on endpoint with the zip archive as a multipart file parameter")
    public void userUploadsZipArchive() {
        try {
            persistedUploadResponse = apiClient.uploadTask(persistedBundle.getFile());
            Assertions.assertNotNull(persistedUploadResponse);
        } catch (IOException e) {
            Assertions.fail("bundle '" + persistedBundle.getFilename() + "' not found, cannot upload");
        } catch (RestClientResponseException e) {
            persistedException = e;
            Assertions.assertNotNull(persistedException);
        }
    }

    @Then("App Engine unzip the zip archive")
    public void appEngineUnzipsArchive() {
        Assertions.assertNotNull(persistedUploadResponse);
    }

    @Then("App Engine successfully validates the task descriptor against the descriptor schema")
    public void appEngineValidatesDescriptor() {
        Assertions.assertNotNull(persistedUploadResponse);
    }

    @Then("App Engine successfully validates the docker image by checking that the tar file "
        + "contains a {string} file")
    public void appEngineValidatesDockerImage(String string) {
        Assertions.assertNotNull(persistedUploadResponse);
    }

    @Then("App Engine creates a unique {string} for referencing the task")
    public void appEngineCreatesUniqueReference(String string) {
        Task uploaded = taskRepository.findByNamespaceAndVersion(
            persistedNamespace,
            persistedVersion
        );
        Assertions.assertEquals(persistedUploadResponse.getId(), uploaded.getIdentifier());
    }

    @Then("App Engine creates a task storage \\(e.g. a bucket reserved for the task) in the File "
        + "Storage service with a unique {string} as follows {string}")
    public void appEngineCreatesTaskBucket(String string, String string2) throws FileStorageException {
        // retrieve from file storage
        String bucketName = uploaded.getStorageReference();
        boolean found = storageHandler.checkStorageExists(bucketName);
        Assertions.assertTrue(found);
    }

    @Then("App Engine stores the task descriptor in the task storage {string} of the File Storage service")
    public void appEngineStoresDescriptorInBucket(String string) throws FileStorageException {
        // retrieve from file storage
        String bucket = uploaded.getStorageReference();
        String object = "descriptor.yml";
        StorageData descriptorFileData = new StorageData(object, bucket);
        StorageData descriptor = storageHandler.readStorageData(descriptorFileData);
        Assertions.assertNotNull(descriptor);
    }

    @Then("App Engine pushes the docker image to the Registry service with an {string} built "
        + "based on the {string} and {string} \\(replace {string} by {string} in {string}, then "
        + "append :{string})")
    public void appEnginePushesImageToRegistry(
        String string, String string2, String string3, String string4, String string5, String string6, String string7
    ) {
        // not implemented yet .. assume pushed
    }

    @Then("App Engine stores relevant task metadata \\(a subset of the task descriptor content) "
        + "in the database associated with the {string}")
    public void appEngineStoresTaskMetadata(String string) {
        // stores task
        uploaded = taskRepository.findByNamespaceAndVersion(persistedNamespace, persistedVersion);
        Assertions.assertNotNull(uploaded);
        Assertions.assertTrue(uploaded.getNamespace().equalsIgnoreCase(persistedNamespace));
        Assertions.assertTrue(uploaded.getVersion().equalsIgnoreCase(persistedVersion));

    }

    @Then("App Engine returns an HTTP {string} OK response")
    public void appEngineReturnsOk(String responseCode) {
        Assertions.assertNull(persistedException);
    }

    @Then("App Engine cleans up any temporary file created during the process \\(e.g. uploaded zip file, etc)")
    public void appEngineCleansUpTempFiles() {
        // tmp files are stored in /tmp/appengine
        final File rootFolder = new File("/tmp/appengine");
        final File multiPartTempFiles = new File("/tmp/appengine/work/Tomcat/localhost/ROOT");
        if (rootFolder.exists() && multiPartTempFiles.exists()) {
            Assertions.assertTrue(multiPartTempFiles.isDirectory());
            Assertions.assertEquals(
                0,
                Objects.requireNonNull(multiPartTempFiles.listFiles()).length
            );
        }
    }

    @Given("this task is already registered by the App Engine")
    public void taskIsAlreadyRegistered() {
        // store task in database
        String bundleFilename = persistedNamespace + "-" + persistedVersion + ".zip";
        persistedTask = TestTaskBuilder.buildTaskFromResource(bundleFilename);

        Assertions.assertNotNull(persistedTask);
        taskRepository.save(persistedTask);

        // create bucket
        try {
            Storage storage = new Storage(persistedTask.getStorageReference());
            if (!storageHandler.checkStorageExists(storage)) {
                storageHandler.createStorage(storage);
            }

            // save file using defined storage reference
            File file = TestTaskBuilder.getDescriptorFromBundleResource(bundleFilename);

            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                JsonNode descriptor = mapper.readTree(fileInputStream);
                ((ObjectNode) descriptor).put("name_short", "must_not_have_changed");

                StorageData fileData = new StorageData(
                    FileHelper.write("descriptor.yml", mapper.writeValueAsBytes(descriptor)),
                    "descriptor.yml"
                );
                storageHandler.saveStorageData(storage, fileData);
            }
        } catch (IOException | FileStorageException e) {
            throw new RuntimeException(e);
        }
    }

    @Then("App Engine reads {string} and {string} from the task descriptor")
    public void appEngineReadsNamespaceAndVersion(String namespace, String version) {
        // it's already in the descriptor
    }

    @Then("App Engine returns an HTTP {string} conflict error because this version of the task exists already")
    public void appEngineReturnsConflictError(String conflictCode) throws JsonProcessingException {
        Assertions.assertEquals(
            Integer.parseInt(conflictCode),
            persistedException.getStatusCode().value()
        );
        JsonNode jsonPayLoad = new ObjectMapper().readTree(persistedException.getResponseBodyAsString());
        Assertions.assertEquals(
            jsonPayLoad.get("error_code").textValue(),
            ErrorDefinitions.fromCode(ErrorCode.INTERNAL_TASK_EXISTS).code
        );
    }

    @Then("App Engine does not create or overwrite the task and related data in the File storage,"
        + " registry and database services")
    public void appEngineDoesNotOverwriteTask() throws FileStorageException, IOException {
        // check app engine doesn't override data in database
        Task uploaded = taskRepository.findByNamespaceAndVersion(
            persistedNamespace,
            persistedVersion
        );
        Assertions.assertEquals(uploaded.getNameShort(), persistedTask.getNameShort());

        // and storage service
        String object = "descriptor.yml";
        StorageData fileData = new StorageData(object, persistedTask.getStorageReference());
        fileData = storageHandler.readStorageData(fileData);
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        JsonNode descriptor = mapper.readTree(fileData.peek().getData());

        Assertions.assertNotNull(fileData);
        String shortName = descriptor.get("name_short").textValue();
        Assertions.assertTrue(shortName.equalsIgnoreCase("must_not_have_changed"));

        // and registry
        CatalogResp response = RegistryClient.catalog(registry, 2, "");
        String imageName = uploaded.getNamespace().replace(".", "/");
        response.getRepositories().contains(imageName);
    }

    // invalid task bundle tests
    @Given("this task {string} is represented by a zip archive containing a task descriptor file "
        + "{string} and a docker image at {string}")
    public void taskIsPackagedAsZipWithDescriptorAndImage(String taskName, String version, String path) {
        // prepare bundles that represent all failure modes based on path and version
        switch (taskName) {
            case "task1":
                persistedBundle = new ClassPathResource("/artifacts/no_version.zip");
                break;
            case "task2":
                persistedBundle = new ClassPathResource("/artifacts/unexpected_path.zip");
                break;
            case "task3":
                persistedBundle = new ClassPathResource("/artifacts/wrong_name.zip");
                break;
            default:
                throw new RuntimeException("Unknown task: " + taskName);
        }
        Assertions.assertNotNull(persistedBundle);
    }

    @Given("the {string} can be retrieved from {string} in the descriptor file or is {string} if the field is missing")
    public void fieldIsRetrievedFromDescriptorOrDefaults(String expectedPath, String config, String image) {
        // reflected by invalid bundles in the artifacts folder in resources
    }

    @Given("the task descriptor is a YAML file stored in the zip archive named {string} which "
        + "does not abide to the agreed descriptor schema for a {string}")
    public void taskDescriptorViolatesSchema(String descriptor, String reason) {
        // reflected by invalid bundles in the artifacts folder in resources
    }

    @Then("App Engine does not create the {string} and related data in the File storage, registry"
        + " and database services")
    public void appEngineDoesNotCreateTask(String namespace) {
        // check database does not contain the task with the given namespace
        Task task = taskRepository.findByNamespaceAndVersion(persistedNamespace, persistedVersion);
        Assertions.assertNull(task);
        // storage is not created because we generate the bucket id based on a generated id
    }

    @Then("App Engine returns an HTTP {string} bad request error because the descriptor is incorrectly structured")
    public void appEngineReturnsBadRequestError(String responseCode) {
        Assertions.assertEquals(400, persistedException.getStatusCode().value());
    }

    @Then("App Engine fails to validate the task descriptor for task {string} against the descriptor schema")
    public void appEngineFailsDescriptorValidation(String taskName) throws JsonProcessingException {
        ErrorCode code = switch (taskName) {
            case "task1", "task3" -> ErrorCode.INTERNAL_SCHEMA_VALIDATION_ERROR;
            case "task2" -> ErrorCode.INTERNAL_DOCKER_IMAGE_TAR_NOT_FOUND;
            default -> throw new RuntimeException("Unknown task: " + taskName);
        };
        JsonNode jsonPayLoad = new ObjectMapper().readTree(persistedException.getResponseBodyAsString());
        Assertions.assertEquals(
            jsonPayLoad.get("error_code").textValue(),
            ErrorDefinitions.fromCode(code).code
        );
    }
}
