package be.cytomine.appengine.handlers.scheduler.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.HostPathVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.ResourceRequirementsBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import be.cytomine.appengine.dto.handlers.scheduler.CollectionSymlink;
import be.cytomine.appengine.dto.handlers.scheduler.Schedule;
import be.cytomine.appengine.dto.handlers.scheduler.Symlink;
import be.cytomine.appengine.exceptions.SchedulingException;
import be.cytomine.appengine.handlers.SchedulerHandler;
import be.cytomine.appengine.handlers.scheduler.impl.utils.PodInformer;
import be.cytomine.appengine.models.task.Run;
import be.cytomine.appengine.models.task.Task;
import be.cytomine.appengine.repositories.RunRepository;
import be.cytomine.appengine.states.TaskRunState;

@Service
@Slf4j
@RequiredArgsConstructor
public class KubernetesScheduler implements SchedulerHandler {

    private final Environment environment;

    private final KubernetesClient kubernetesClient;

    private final RunRepository runRepository;

    @Value("${scheduler.registry-advertised-url}")
    private String registryAdvertisedUrl;

    @Value("${scheduler.helper-containers-resources.ram}")
    private String helperContainerRam;

    @Value("${scheduler.helper-containers-resources.cpu}")
    private String helperContainerCpu;

    @Value("${storage.base-path}")
    private String storageBasePath;

    @Value("${scheduler.run.mode}")
    private String runMode;

    @Value("${scheduler.run.storage-base-path}")
    private String runModeStorageBasePath;

    @Value("${scheduler.datasets-path}")
    private String imagesDatasetsPath;

    private PodInformer podInformer;

    @Value("${scheduler.advertised-url}")
    private String advertisedUrl;

    @Value("${app-engine.api_prefix}")
    private String apiPrefix;

    @Value("${app-engine.api_version}")
    private String apiVersion;

    @Value("${scheduler.use-host-network}")
    private boolean useHostNetwork;

    private String baseInputPath;

    private String baseOutputPath;

    private String baseUrl;

    @PostConstruct
    public void buildTaskRunsUrl() {
        baseUrl = UriComponentsBuilder
            .fromUriString(advertisedUrl)
            .path(apiPrefix)
            .path(apiVersion)
            .path("/task-runs/")
            .toUriString();

        String basePath = "";
        if (runMode.equalsIgnoreCase("local")) {
            basePath = runModeStorageBasePath;
        }
        if (runMode.equalsIgnoreCase("cluster")) {
            basePath = "/tmp/app-engine";
        }
        this.baseInputPath = basePath + "/task-run-inputs-";
        this.baseOutputPath = basePath + "/task-run-outputs-";
    }

    @Override
    public Schedule schedule(Schedule schedule) throws SchedulingException {
        log.info("Schedule: get Task parameters");

        Run run = schedule.getRun();
        String runId = run.getId().toString();
        Map<String, String> labels = new HashMap<>();
        labels.put("runId", runId);

        Task task = run.getTask();
        String runSecret = String.valueOf(run.getSecret());

        log.info("Schedule: create task pod...");

        // Define helper container resources
        ResourceRequirementsBuilder helperContainersResourcesBuilder = new ResourceRequirements()
            .toBuilder()
            .addToRequests("cpu", new Quantity(helperContainerCpu))
            .addToRequests("memory", new Quantity(helperContainerRam))
            .addToLimits("cpu", new Quantity(helperContainerCpu))
            .addToLimits("memory", new Quantity(helperContainerRam));

        ResourceRequirements helperContainersResources = helperContainersResourcesBuilder.build();

        // Define task resources for the task
        ResourceRequirementsBuilder taskResourcesBuilder = new ResourceRequirements()
            .toBuilder()
            .addToRequests("cpu", new Quantity(Integer.toString(task.getCpus())))
            .addToRequests("memory", new Quantity(task.getRam()))
            .addToLimits("cpu", new Quantity(Integer.toString(task.getCpus())))
            .addToLimits("memory", new Quantity(task.getRam()));

        if (task.getGpus() > 0) {
            taskResourcesBuilder = taskResourcesBuilder
                .addToRequests("nvidia.com/gpu", new Quantity(Integer.toString(task.getGpus())))
                .addToLimits("nvidia.com/gpu", new Quantity(Integer.toString(task.getGpus())));
        }


        String url = baseUrl + runId;
        String and = " && ";

        // set up symlinks creator
        StringBuilder createSymlinks = new StringBuilder();
        if (Objects.nonNull(schedule.getLinks()) && !schedule.getLinks().isEmpty()) { // refs exist
            // loop the symlink containers
            for (Symlink link : schedule.getLinks()) {
                if (link instanceof CollectionSymlink collectionSymlink) {
                    int collectionSize = collectionSymlink.getSymlinks().size();
                    createSymlinks =
                        new StringBuilder("mkdir -p "
                            + task.getInputFolder()
                            + "/"
                            + collectionSymlink.getParameterName()
                            + " && ");
                    for (Map.Entry<String, String> entry :
                        collectionSymlink.getSymlinks().entrySet()) {
                        createSymlinks
                            .append("ln -s ")
                            .append(entry.getValue())
                            .append(" ")
                            .append(task.getInputFolder())
                            .append("/")
                            .append(collectionSymlink.getParameterName())
                            .append("/")
                            .append(convertBracketsToPath(entry.getKey()))
                            .append(" && ");
                    }
                    createSymlinks
                        .append("echo 'size: ")
                        .append(collectionSize)
                        .append("' > ")
                        .append(task.getInputFolder())
                        .append("/")
                        .append(collectionSymlink.getParameterName())
                        .append("/array.yml");
                }
            }

        }


        String fetchInputs = "curl -L -o inputs.zip " + url + "/inputs.zip";
        String unzipInputs = "time unzip -o inputs.zip -d " + task.getInputFolder();

        Container inputContainer = new ContainerBuilder()
            .withName("inputs-provisioning")
            .withImage("cytomineuliege/alpine-task-utils:latest")
            .withImagePullPolicy("IfNotPresent")
            .withCommand("/bin/sh", "-c", fetchInputs + and + unzipInputs)

            // request and limit helper container resources
            .withResources(helperContainersResources)

            // Mount volume for inputs provisioning
            .addNewVolumeMount()
            .withName("inputs")
            .withMountPath(task.getInputFolder())
            .endVolumeMount()

            .build();


        String sendOutputs = "curl -X POST -F 'outputs=@outputs.zip' ";
        sendOutputs += url + "/" + runSecret + "/outputs.zip";
        String zipOutputs = "cd " + task.getOutputFolder() + and + "-0 zip -r outputs.zip .";
        String wait = "export TOKEN=$(cat /var/run/secrets/kubernetes.io/serviceaccount/token); ";
        wait += "while ! curl -vk -H \"Authorization: Bearer $TOKEN\" ";
        wait += "https://${KUBERNETES_SERVICE_HOST}:${KUBERNETES_SERVICE_PORT_HTTPS}/api/v1" +
            "/namespaces/default/pods/${POD_NAME}/status ";
        wait += "| jq '.status | .containerStatuses[] | select(.name == \"task\") | .state ";
        wait += "| keys[0]' | grep -q -F \"terminated\"; do sleep 2; done";

        String clusterOutputCommand = wait + and + zipOutputs + and + sendOutputs;
        String updateAPIRequestJson = " '{ \"desired\" : \"FINISHED\" }' ";
        String requestToUpdateRunStateToFinished
            = "curl -X POST -H 'Content-Type: application/json' -d";
        requestToUpdateRunStateToFinished += updateAPIRequestJson;
        requestToUpdateRunStateToFinished += url + "/" + "state-actions";
        String localOutputCommand = wait + and + requestToUpdateRunStateToFinished;

        String command = "";
        if (runMode.equalsIgnoreCase("local")) {
            command = localOutputCommand;
        }
        if (runMode.equalsIgnoreCase("cluster")) {
            command = clusterOutputCommand;
        }

        Container outputContainer = new ContainerBuilder()
            .withName("outputs-sending")
            .withImage("cytomineuliege/alpine-task-utils:latest")
            .withImagePullPolicy("IfNotPresent")
            .withCommand("/bin/sh", "-c", command)

            // request and limit helper container resources
            .withResources(helperContainersResources)

            .addNewVolumeMount()
            .withName("outputs")
            .withMountPath(task.getOutputFolder())
            .endVolumeMount()

            .withEnv(
                new EnvVarBuilder()
                    .withName("POD_NAME")
                    .withNewValueFrom()
                    .withNewFieldRef()
                    .withFieldPath("metadata.name")
                    .endFieldRef()
                    .endValueFrom()
                    .build())

            .build();

        String permissions = "chmod -R 777 " + task.getInputFolder() + " " + task.getOutputFolder();
        Container permissionContainer = new ContainerBuilder()
            .withName("permissions")
            .withImage("cytomineuliege/alpine-task-utils:latest")
            .withImagePullPolicy("IfNotPresent")
            .withCommand("/bin/sh", "-c", permissions)

            .withResources(helperContainersResources)

            .addNewVolumeMount()
            .withName("inputs")
            .withMountPath(task.getInputFolder())
            .endVolumeMount()
            .addNewVolumeMount()
            .withName("outputs")
            .withMountPath(task.getOutputFolder())
            .endVolumeMount()

            .build();

        boolean isClusterMode = this.runMode.equalsIgnoreCase("cluster");
        // Defining the pod image to run
        String podName = task.getName().toLowerCase().replaceAll("[^a-zA-Z0-9]", "") + "-" + runId;
        String imageName = registryAdvertisedUrl + "/" + task.getImageName();

        PodBuilder podBuilder = new PodBuilder()
            .withNewMetadata()
            .withName(podName)
            .withLabels(labels)
            .endMetadata()
            .withNewSpec()

            .withHostNetwork(useHostNetwork)

            .addNewInitContainerLike(permissionContainer)
            .and()
            .withRestartPolicy("Never")
            .endSpec();

        Pod pod = podBuilder.build();
        PodBuilder newPodBuilder = new PodBuilder(pod);
        List<Container> initContainers = new ArrayList<>(pod.getSpec().getInitContainers());
        List<Container> containers = new ArrayList<>(pod.getSpec().getContainers());
        List<Volume> volumes = new ArrayList<>(pod.getSpec().getVolumes());

        // Add inputContainer conditionally
        if (isClusterMode) {
            initContainers.add(inputContainer);
        }

        Container symlinksCreatorContainer = new ContainerBuilder()
            .withName("symlinks-creator")
            .withImage("cytomineuliege/alpine-task-utils:latest")
            .withImagePullPolicy("IfNotPresent")
            .withCommand("/bin/sh", "-c", createSymlinks.toString())
            .withResources(helperContainersResources)
            .addNewVolumeMount()
            .withName("inputs")
            .withMountPath(task.getInputFolder())
            .endVolumeMount()
            .addNewVolumeMount()
            .withName("images-datasets")
            .withMountPath("/datasets")
            .withReadOnly(true) // to avoid corrupting the dataset
            .endVolumeMount()
            .build();

        // add symlink creator init container if run mode is local and also the container is used
        if (!isClusterMode && !schedule.getLinks().isEmpty()) {
            initContainers.add(symlinksCreatorContainer);
        }

        ResourceRequirements taskResources = taskResourcesBuilder.build();
        Container taskContainer = new ContainerBuilder()
            .withName("task")
            .withImage(imageName)
            .withImagePullPolicy("IfNotPresent")
            // request and limit task resources
            .withResources(taskResources)
            // Mount volumes for inputs and outputs
            .addNewVolumeMount()
            .withName("inputs")
            .withMountPath(task.getInputFolder())
            .endVolumeMount()
            .addNewVolumeMount()
            .withName("outputs")
            .withMountPath(task.getOutputFolder())
            .endVolumeMount()

            .build();

        // Add taskContainer unconditionally
        containers.add(taskContainer);
        containers.add(outputContainer);

        // Add inputs and outputs volumes conditionally
        volumes.add(new VolumeBuilder()
            .withName("inputs")
            .withHostPath(
                new HostPathVolumeSourceBuilder().withPath(baseInputPath + runId).build())
            .build());
        volumes.add(new VolumeBuilder()
            .withName("outputs")
            .withHostPath(
                new HostPathVolumeSourceBuilder().withPath(baseOutputPath + runId).build())
            .build());
        // add another host path volume to where the datasets of large images is
        volumes.add(new VolumeBuilder()
            .withName("images-datasets")
            .withHostPath(
                new HostPathVolumeSourceBuilder().withPath(imagesDatasetsPath).build())
            .build());

        newPodBuilder = newPodBuilder.editOrNewSpec()
            .withInitContainers(initContainers)
            .withContainers(containers)
            .withVolumes(volumes)
            .endSpec();

        podBuilder = newPodBuilder;

        log.info("Schedule: Task Pod scheduled to run on the cluster");
        try {
            kubernetesClient
                .pods()
                .inNamespace("default")
                .resource(podBuilder.build())
                .create();
        } catch (KubernetesClientException e) {
            e.printStackTrace();
            throw new SchedulingException("Task Pod failed to be scheduled on the cluster");
        }

        run.setState(TaskRunState.QUEUED);
        runRepository.saveAndFlush(run);
        log.info("Schedule: Task Pod queued for execution on the cluster");

        return schedule;
    }

    @Override
    public void alive() throws SchedulingException {
        log.info("Alive: check if the scheduler is up and running");

        try {
            kubernetesClient
                .pods()
                .inNamespace("default")
                .list();
        } catch (KubernetesClientException e) {
            throw new SchedulingException("Scheduler is not alive");
        }
    }

    @Override
    @PostConstruct
    public void monitor() throws SchedulingException {
        if (Arrays.asList(environment.getActiveProfiles()).contains("test")) {
            log.info("Monitor: disabled in test mode");
            return;
        }

        log.info("Monitor: add informer to the cluster");
        kubernetesClient
            .pods()
            .inNamespace("default")
            .inform(podInformer)
            .run();
        log.info("Monitor: informer added");
    }

    public String convertBracketsToPath(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        // Replace all '][' with a '/'
        String temp = input.replace("][", "/");
        // Remove the leading '[' and trailing ']'
        return temp.substring(1, temp.length() - 1);
    }
}
