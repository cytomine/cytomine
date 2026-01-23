---
title: Help
---

# {{ $frontmatter.title }}

::: warning
App Engine and its Task system are still in BETA. Therefore, all information presented in this documentation is subject to potentially breaking changes.
:::
## How To Debug

#### Tasks run steps :

Check out [How to run a task](/user-guide/getting-started#run-a-task)

1. Select the task you want to run. (drop down menu)
2. select inputs (by entering the values or selecting them from cytomine)
2. provision task inputs.
3. Run the task.
4. expand the run to see both inputs and outputs. (output images are synchronously added to the project)

::: tip
provisioning and scheduling a task are done sequentially when you click the run task button in UI
:::

Task run goes through different states depending on many factors like availability of data and other things check out [Task Run States](/dev-guide/algorithms/task/#run) for more details. Running 
a task does not always run as expected. So to help developers debug their tasks and identify issues, a few logs are available to help troubleshoot.

#### Logs

::: warning
For docker compose commands, you may need sudo permission if you are not in the docker group.
:::

1. Check the logs of the task run in app-engine using the following command
```
docker compose logs -f app-engine
```
which generates logs similar to the following
```
app-engine-1  |   .   ____          _            __ _ _
app-engine-1  |  /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
app-engine-1  | ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
app-engine-1  |  \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
app-engine-1  |   '  |____| .__|_| |_|_| |_\__, | / / / /
app-engine-1  |  =========|_|==============|___/=/_/_/_/
app-engine-1  | 
app-engine-1  |  :: Spring Boot ::                (v4.0.0)
app-engine-1  | 
app-engine-1  | 2026-01-23T10:56:39.361Z  INFO 1 --- [           main] b.c.appengine.AppEngineApplication       : Starting AppEngineApplication using Java 17.0.17 with PID 1 (/app/cytomine-app-engine.jar started by root in /app)
app-engine-1  | 2026-01-23T10:56:39.369Z  INFO 1 --- [           main] b.c.appengine.AppEngineApplication       : No active profile set, falling back to 1 default profile: "default"
app-engine-1  | 2026-01-23T10:56:49.870Z  INFO 1 --- [           main] b.c.a.h.r.impl.DockerRegistryHandler     : Docker Registry Handler: initialised
app-engine-1  | 2026-01-23T10:56:50.854Z  INFO 1 --- [           main] b.c.a.h.s.impl.KubernetesScheduler       : Monitor: add informer to the cluster
app-engine-1  | 2026-01-23T10:56:51.375Z  INFO 1 --- [           main] b.c.a.h.s.impl.KubernetesScheduler       : Monitor: informer added
app-engine-1  | 2026-01-23T10:56:52.126Z  INFO 1 --- [           main] b.c.appengine.AppEngineApplication       : Started AppEngineApplication in 14.071 seconds (process running for 15.915)
app-engine-1  | 2026-01-23T11:13:24.614Z  INFO 1 --- [nio-8080-exec-1] b.c.a.controllers.TaskController         : tasks GET
app-engine-1  | 2026-01-23T11:13:24.615Z  INFO 1 --- [nio-8080-exec-1] b.c.a.controllers.TaskController         : tasks GET Ended
app-engine-1  | 2026-01-23T11:13:24.615Z  INFO 1 --- [nio-8080-exec-1] b.c.appengine.services.TaskService       : tasks: retrieving tasks...
app-engine-1  | 2026-01-23T11:13:24.762Z  INFO 1 --- [nio-8080-exec-1] b.c.appengine.services.TaskService       : tasks: retrieved tasks
app-engine-1  | 2026-01-23T11:13:24.912Z  INFO 1 --- [nio-8080-exec-2] b.c.a.controllers.TaskRunController      : /task-runs/113539bf-8285-4212-9ea5-aa61208ff5e8 GET
app-engine-1  | 2026-01-23T11:13:24.912Z  INFO 1 --- [nio-8080-exec-2] b.c.a.services.TaskProvisioningService   : Retrieving Run: retrieving...
app-engine-1  | 2026-01-23T11:13:24.931Z  INFO 1 --- [nio-8080-exec-2] b.c.a.services.TaskProvisioningService   : Retrieving Run: retrieved
app-engine-1  | 2026-01-23T11:13:24.932Z  INFO 1 --- [nio-8080-exec-2] b.c.a.controllers.TaskRunController      : /task-runs/113539bf-8285-4212-9ea5-aa61208ff5e8 GET Ended
app-engine-1  | 2026-01-23T11:13:26.633Z  INFO 1 --- [nio-8080-exec-3] b.c.a.controllers.TaskController         : tasks/{namespace}/{version}/inputs GET
app-engine-1  | 2026-01-23T11:13:26.633Z  INFO 1 --- [nio-8080-exec-3] b.c.appengine.services.TaskService       : tasks/{namespace}/{version}: retrieving task...
app-engine-1  | 2026-01-23T11:13:26.725Z  INFO 1 --- [nio-8080-exec-3] b.c.appengine.services.TaskService       : tasks/{namespace}/{version}: retrieved task...
app-engine-1  | 2026-01-23T11:13:26.725Z  INFO 1 --- [nio-8080-exec-3] b.c.a.controllers.TaskController         : tasks/{namespace}/{version}/inputs GET Ended
app-engine-1  | 2026-01-23T11:13:26.780Z  INFO 1 --- [nio-8080-exec-4] b.c.a.controllers.TaskController         : tasks/{namespace}/{version}/outputs GET
app-engine-1  | 2026-01-23T11:13:26.780Z  INFO 1 --- [nio-8080-exec-4] b.c.appengine.services.TaskService       : tasks/{namespace}/{version}: retrieving task...
app-engine-1  | 2026-01-23T11:13:26.786Z  INFO 1 --- [nio-8080-exec-4] b.c.appengine.services.TaskService       : tasks/{namespace}/{version}: retrieved task...
app-engine-1  | 2026-01-23T11:13:26.787Z  INFO 1 --- [nio-8080-exec-4] b.c.a.controllers.TaskController         : tasks/{namespace}/{version}/outputs GET Ended
app-engine-1  | 2026-01-23T11:13:33.163Z  INFO 1 --- [nio-8080-exec-5] b.c.a.controllers.TaskController         : tasks/{namespace}/{version}/runs POST
app-engine-1  | 2026-01-23T11:13:33.164Z  INFO 1 --- [nio-8080-exec-5] b.c.appengine.services.TaskService       : tasks/{namespace}/{version}/runs: creating run...
app-engine-1  | 2026-01-23T11:13:33.164Z  INFO 1 --- [nio-8080-exec-5] b.c.appengine.services.TaskService       : tasks/{namespace}/{version}/runs: retrieving associated task...
app-engine-1  | 2026-01-23T11:13:33.176Z  INFO 1 --- [nio-8080-exec-5] b.c.appengine.services.TaskService       : tasks/{namespace}/{version}/runs: retrieved task...
app-engine-1  | 2026-01-23T11:13:33.207Z  INFO 1 --- [nio-8080-exec-5] b.c.appengine.services.TaskService       : tasks/{namespace}/{version}/runs: Storage is created for task
app-engine-1  | 2026-01-23T11:13:33.207Z  INFO 1 --- [nio-8080-exec-5] b.c.appengine.services.TaskService       : tasks/{id}/runs: run created...
app-engine-1  | 2026-01-23T11:13:33.217Z  INFO 1 --- [nio-8080-exec-5] b.c.a.controllers.TaskController         : tasks/{namespace}/{version}/runs POST Ended
app-engine-1  | 2026-01-23T11:13:33.294Z  INFO 1 --- [nio-8080-exec-6] b.c.a.controllers.TaskController         : tasks/{namespace}/{version}/outputs GET
app-engine-1  | 2026-01-23T11:13:33.294Z  INFO 1 --- [nio-8080-exec-6] b.c.appengine.services.TaskService       : tasks/{namespace}/{version}: retrieving task...
app-engine-1  | 2026-01-23T11:13:33.298Z  INFO 1 --- [nio-8080-exec-6] b.c.appengine.services.TaskService       : tasks/{namespace}/{version}: retrieved task...
app-engine-1  | 2026-01-23T11:13:33.298Z  INFO 1 --- [nio-8080-exec-6] b.c.a.controllers.TaskController         : tasks/{namespace}/{version}/outputs GET Ended
app-engine-1  | 2026-01-23T11:13:33.894Z  INFO 1 --- [nio-8080-exec-7] b.c.a.controllers.TaskRunController      : /task-runs/10f80809-9a94-4dce-bc6c-9ba2174bfd33/input-provisions/input JSON POST
app-engine-1  | 2026-01-23T11:13:33.894Z  INFO 1 --- [nio-8080-exec-7] b.c.a.services.TaskProvisioningService   : provisioning streaming: preparing...
app-engine-1  | 2026-01-23T11:13:33.894Z  INFO 1 --- [nio-8080-exec-7] b.c.a.services.TaskProvisioningService   : provisioning streaming: streaming...
app-engine-1  | 2026-01-23T11:13:34.228Z  INFO 1 --- [nio-8080-exec-7] b.c.a.services.TaskProvisioningService   : provisioning streaming: file successfully streamed and saved 
app-engine-1  | 2026-01-23T11:13:34.230Z  INFO 1 --- [nio-8080-exec-7] b.c.a.services.TaskProvisioningService   : ProvisionParameter: finding associated task run...
app-engine-1  | 2026-01-23T11:13:34.237Z  INFO 1 --- [nio-8080-exec-7] b.c.a.services.TaskProvisioningService   : ProvisionParameter: found
app-engine-1  | 2026-01-23T11:13:34.237Z  INFO 1 --- [nio-8080-exec-7] b.c.a.services.TaskProvisioningService   : ProvisionParameter: validating provision against parameter type definition...
app-engine-1  | 2026-01-23T11:13:34.326Z  INFO 1 --- [nio-8080-exec-7] b.c.a.services.TaskProvisioningService   : ProvisionParameter: provision is valid
app-engine-1  | 2026-01-23T11:13:34.326Z  INFO 1 --- [nio-8080-exec-7] b.c.a.services.TaskProvisioningService   : ProvisionParameter: saving provision in database...
app-engine-1  | 2026-01-23T11:13:34.336Z  INFO 1 --- [nio-8080-exec-7] b.c.a.services.TaskProvisioningService   : ProvisionParameter: saved
app-engine-1  | 2026-01-23T11:13:34.337Z  INFO 1 --- [nio-8080-exec-7] b.c.a.services.TaskProvisioningService   : ProvisionParameter: Changing run state to PROVISIONED...
app-engine-1  | 2026-01-23T11:13:34.762Z  INFO 1 --- [nio-8080-exec-7] b.c.a.services.TaskProvisioningService   : ProvisionParameter: RUN PROVISIONED
app-engine-1  | 2026-01-23T11:13:34.773Z  INFO 1 --- [nio-8080-exec-7] b.c.a.controllers.TaskRunController      : ProvisionParameter: calculating & caching CRC32 checksum...
app-engine-1  | 2026-01-23T11:13:34.795Z  INFO 1 --- [nio-8080-exec-7] b.c.a.controllers.TaskRunController      : /task-runs/{run_id}/input-provisions/{param_name} File POST Ended
app-engine-1  | 2026-01-23T11:13:34.854Z  INFO 1 --- [nio-8080-exec-8] b.c.a.controllers.TaskRunController      : POST /task-runs/10f80809-9a94-4dce-bc6c-9ba2174bfd33/state-actions
app-engine-1  | 2026-01-23T11:13:34.854Z  INFO 1 --- [nio-8080-exec-8] b.c.a.services.TaskProvisioningService   : Update State: validating Run...
app-engine-1  | 2026-01-23T11:13:34.863Z  INFO 1 --- [nio-8080-exec-8] b.c.a.services.TaskProvisioningService   : Running Task: scheduling...
app-engine-1  | 2026-01-23T11:13:34.863Z  INFO 1 --- [nio-8080-exec-8] b.c.a.services.TaskProvisioningService   : Running Task: valid run
app-engine-1  | 2026-01-23T11:13:34.863Z  INFO 1 --- [nio-8080-exec-8] b.c.a.services.TaskProvisioningService   : Running Task: contacting scheduler...
app-engine-1  | 2026-01-23T11:13:34.875Z  INFO 1 --- [nio-8080-exec-8] b.c.a.h.s.impl.KubernetesScheduler       : Schedule: get Task parameters
app-engine-1  | 2026-01-23T11:13:34.876Z  INFO 1 --- [nio-8080-exec-8] b.c.a.h.s.impl.KubernetesScheduler       : Schedule: create task pod...
app-engine-1  | 2026-01-23T11:13:34.882Z  INFO 1 --- [nio-8080-exec-8] b.c.a.h.s.impl.KubernetesScheduler       : Schedule: Task Pod scheduled to run on the cluster
app-engine-1  | 2026-01-23T11:13:34.930Z  INFO 1 --- [nio-8080-exec-8] b.c.a.h.s.impl.KubernetesScheduler       : Schedule: Task Pod queued for execution on the cluster
app-engine-1  | 2026-01-23T11:13:34.930Z  INFO 1 --- [nio-8080-exec-8] b.c.a.services.TaskProvisioningService   : Running Task: scheduling done
app-engine-1  | 2026-01-23T11:13:34.934Z  INFO 1 --- [nio-8080-exec-8] b.c.a.services.TaskProvisioningService   : Running Task: updated Run state to QUEUING
app-engine-1  | 2026-01-23T11:13:34.935Z  INFO 1 --- [nio-8080-exec-8] b.c.a.services.TaskProvisioningService   : Running Task: scheduled
app-engine-1  | 2026-01-23T11:13:34.935Z  INFO 1 --- [nio-8080-exec-8] b.c.a.controllers.TaskRunController      : POST /task-runs/10f80809-9a94-4dce-bc6c-9ba2174bfd33/state-actions Ended
app-engine-1  | 2026-01-23T11:13:34.981Z  INFO 1 --- [nio-8080-exec-9] b.c.a.controllers.TaskRunController      : /task-runs/10f80809-9a94-4dce-bc6c-9ba2174bfd33 GET
app-engine-1  | 2026-01-23T11:13:34.981Z  INFO 1 --- [nio-8080-exec-9] b.c.a.services.TaskProvisioningService   : Retrieving Run: retrieving...
app-engine-1  | 2026-01-23T11:13:34.990Z  INFO 1 --- [nio-8080-exec-9] b.c.a.services.TaskProvisioningService   : Retrieving Run: retrieved
app-engine-1  | 2026-01-23T11:13:34.990Z  INFO 1 --- [nio-8080-exec-9] b.c.a.controllers.TaskRunController      : /task-runs/10f80809-9a94-4dce-bc6c-9ba2174bfd33 GET Ended
app-engine-1  | 2026-01-23T11:13:36.983Z  INFO 1 --- [io-8080-exec-10] b.c.a.controllers.TaskRunController      : /task-runs/10f80809-9a94-4dce-bc6c-9ba2174bfd33 GET
app-engine-1  | 2026-01-23T11:13:36.983Z  INFO 1 --- [io-8080-exec-10] b.c.a.services.TaskProvisioningService   : Retrieving Run: retrieving...
app-engine-1  | 2026-01-23T11:13:36.994Z  INFO 1 --- [io-8080-exec-10] b.c.a.services.TaskProvisioningService   : Retrieving Run: retrieved
app-engine-1  | 2026-01-23T11:13:36.994Z  INFO 1 --- [io-8080-exec-10] b.c.a.controllers.TaskRunController      : /task-runs/10f80809-9a94-4dce-bc6c-9ba2174bfd33 GET Ended
app-engine-1  | 2026-01-23T11:13:38.487Z  INFO 1 --- [nio-8080-exec-1] b.c.a.controllers.TaskRunController      : POST /task-runs/10f80809-9a94-4dce-bc6c-9ba2174bfd33/state-actions
app-engine-1  | 2026-01-23T11:13:38.487Z  INFO 1 --- [nio-8080-exec-1] b.c.a.services.TaskProvisioningService   : Update State: validating Run...
app-engine-1  | 2026-01-23T11:13:38.495Z  INFO 1 --- [nio-8080-exec-1] b.c.a.services.TaskProvisioningService   : Provisioning: processing outputs...
app-engine-1  | 2026-01-23T11:13:38.505Z  INFO 1 --- [nio-8080-exec-1] b.c.a.services.TaskProvisioningService   : Provisioning: reading outputs...
app-engine-1  | 2026-01-23T11:13:38.506Z  INFO 1 --- [nio-8080-exec-1] b.c.a.services.TaskProvisioningService   : Provisioning: validating output files...
app-engine-1  | 2026-01-23T11:13:38.506Z  INFO 1 --- [nio-8080-exec-1] b.c.a.services.TaskProvisioningService   : Posting Outputs Archive: validating files and directories contents and structure...
app-engine-1  | 2026-01-23T11:13:38.508Z  INFO 1 --- [nio-8080-exec-1] b.c.a.services.TaskProvisioningService   : Posting Outputs Archive: validated finished...
app-engine-1  | 2026-01-23T11:13:38.508Z  INFO 1 --- [nio-8080-exec-1] b.c.a.services.TaskProvisioningService   : Provisioning: storing output in database...
app-engine-1  | 2026-01-23T11:13:38.508Z  INFO 1 --- [nio-8080-exec-1] b.c.a.services.TaskProvisioningService   : Posting Outputs Archive: saving...
app-engine-1  | 2026-01-23T11:13:38.519Z  INFO 1 --- [nio-8080-exec-1] b.c.a.services.TaskProvisioningService   : Posting Outputs Archive: saved...
app-engine-1  | 2026-01-23T11:13:38.519Z  INFO 1 --- [nio-8080-exec-1] b.c.a.services.TaskProvisioningService   : Provisioning: calculating CRC32 checksum for zip entry output
app-engine-1  | 2026-01-23T11:13:38.555Z  INFO 1 --- [nio-8080-exec-1] b.c.a.services.TaskProvisioningService   : Provisioning: state updated to FINISHED
app-engine-1  | 2026-01-23T11:13:38.559Z  INFO 1 --- [nio-8080-exec-1] b.c.a.controllers.TaskRunController      : POST /task-runs/10f80809-9a94-4dce-bc6c-9ba2174bfd33/state-actions Ended
app-engine-1  | 2026-01-23T11:13:38.982Z  INFO 1 --- [nio-8080-exec-3] b.c.a.controllers.TaskRunController      : /task-runs/10f80809-9a94-4dce-bc6c-9ba2174bfd33 GET
app-engine-1  | 2026-01-23T11:13:38.982Z  INFO 1 --- [nio-8080-exec-3] b.c.a.services.TaskProvisioningService   : Retrieving Run: retrieving...
app-engine-1  | 2026-01-23T11:13:38.989Z  INFO 1 --- [nio-8080-exec-3] b.c.a.services.TaskProvisioningService   : Retrieving Run: retrieved
app-engine-1  | 2026-01-23T11:13:38.989Z  INFO 1 --- [nio-8080-exec-3] b.c.a.controllers.TaskRunController      : /task-runs/10f80809-9a94-4dce-bc6c-9ba2174bfd33 GET Ended

```
2. Check the logs of `K3s` the following command lists all the pods if you have `kubectl` installed
```
kubectl --kubeconfig=./.kube/shared/config -n app-engine-tasks get pods
```
otherwise you can use
```
docker compose exec -it k3s kubectl -n app-engine-tasks get pods
```
which lists all the pods along with their status as follows 
```
NAME                                                             READY   STATUS      RESTARTS   AGE
identitywithwsidicomimage-113539bf-8285-4212-9ea5-aa61208ff5e8   0/2     Completed   0          26m
```
3. To check the logs of the app inside the pod, `<name-of-pod>` comes from name column of the command number 2 above
```
kubectl --kubeconfig=./.kube/shared/config -n app-engine-tasks logs <name-of-pod>
```
if you don't have `kubectl` installed you can use the following command, `<name-of-pod>` comes from name column of the command number 2 above
```
docker compose exec -it k3s kubectl -n app-engine-tasks logs <name-of-pod>
```
4. To check scheduling and handling of your task by kubernetes run the following, `<name-of-pod>` comes from name column of the command number 2 above
```
kubectl --kubeconfig=./.kube/shared/config -n app-engine-tasks describe pod <name-of-pod>
```
it generates an output similar to the following

```
some-user:~/cytomine$ kubectl --kubeconfig=./.kube/shared/config -n app-engine-tasks describe pod  identitywithwsidicomimage-10f80809-9a94-4dce-bc6c-9ba2174bfd33
Name:             identitywithwsidicomimage-10f80809-9a94-4dce-bc6c-9ba2174bfd33
Namespace:        app-engine-tasks
Priority:         0
Service Account:  app-engine
Node:             d429b05f01eb/172.16.238.15
Start Time:       Fri, 23 Jan 2026 12:13:34 +0100
Labels:           app=task
                  runId=10f80809-9a94-4dce-bc6c-9ba2174bfd33
Annotations:      <none>
Status:           Succeeded
IP:               172.16.238.15
IPs:
  IP:  172.16.238.15
Init Containers:
  permissions:
    Container ID:  containerd://d88b54b352e36a4ff154fedef8985887b7a5180335b023e4079c411126fab1a7
    Image:         cytomineuliege/alpine-task-utils:latest
    Image ID:      docker.io/cytomineuliege/alpine-task-utils@sha256:7b1b37ae26ad8a5d36c93d0ba9fabf690b5e145cc3c332f43bc24acaead6e79a
    Port:          <none>
    Host Port:     <none>
    Command:
      /bin/sh
      -c
      chmod -R 777 /inputs /outputs
    State:          Terminated
      Reason:       Completed
      Exit Code:    0
      Started:      Fri, 23 Jan 2026 12:13:35 +0100
      Finished:     Fri, 23 Jan 2026 12:13:35 +0100
    Ready:          True
    Restart Count:  0
    Limits:
      cpu:     1
      memory:  1Gi
    Requests:
      cpu:        1
      memory:     1Gi
    Environment:  <none>
    Mounts:
      /inputs from inputs (rw)
      /outputs from outputs (rw)
      /var/run/secrets/kubernetes.io/serviceaccount from kube-api-access-qmxtv (ro)
Containers:
  task:
    Container ID:   containerd://933b712a47a7a6a3084a3ff67e87a0594be9115abd0ab1a3da01bf3ac1b8dd33
    Image:          172.16.238.4:5000/com/cytomine/dummy/identity/wsidicom/image:1.0.0
    Image ID:       172.16.238.4:5000/com/cytomine/dummy/identity/wsidicom/image@sha256:7ae5ff3f41c828b73c9555093870a92667d263387125f2f2899a202d0b2e2348
    Port:           <none>
    Host Port:      <none>
    State:          Terminated
      Reason:       Completed
      Exit Code:    0
      Started:      Fri, 23 Jan 2026 12:13:36 +0100
      Finished:     Fri, 23 Jan 2026 12:13:36 +0100
    Ready:          False
    Restart Count:  0
    Limits:
      cpu:     1
      memory:  4Gi
    Requests:
      cpu:        1
      memory:     4Gi
    Environment:  <none>
    Mounts:
      /inputs from inputs (rw)
      /outputs from outputs (rw)
      /var/run/secrets/kubernetes.io/serviceaccount from kube-api-access-qmxtv (ro)
  outputs-sending:
    Container ID:  containerd://0486a878fb46a5a5605ae361e52ba2cb5be54c95185c79fa29a5ecd80fbf1b11
    Image:         cytomineuliege/alpine-task-utils:latest
    Image ID:      docker.io/cytomineuliege/alpine-task-utils@sha256:7b1b37ae26ad8a5d36c93d0ba9fabf690b5e145cc3c332f43bc24acaead6e79a
    Port:          <none>
    Host Port:     <none>
    Command:
      /bin/sh
      -c
      export TOKEN=$(cat /var/run/secrets/kubernetes.io/serviceaccount/token); while ! curl -vk -H "Authorization: Bearer $TOKEN" https://${KUBERNETES_SERVICE_HOST}:${KUBERNETES_SERVICE_PORT_HTTPS}/api/v1/namespaces/app-engine-tasks/pods/${POD_NAME}/status | jq '.status | .containerStatuses[] | select(.name == "task") | .state | keys[0]' | grep -q -F "terminated"; do sleep 2; done && curl -X POST -H 'Content-Type: application/json' -d '{ "desired" : "FINISHED" }' http://172.16.238.10:8080/app-engine/v1/task-runs/10f80809-9a94-4dce-bc6c-9ba2174bfd33/state-actions
    State:          Terminated
      Reason:       Completed
      Exit Code:    0
      Started:      Fri, 23 Jan 2026 12:13:36 +0100
      Finished:     Fri, 23 Jan 2026 12:13:38 +0100
    Ready:          False
    Restart Count:  0
    Limits:
      cpu:     1
      memory:  1Gi
    Requests:
      cpu:     1
      memory:  1Gi
    Environment:
      POD_NAME:  identitywithwsidicomimage-10f80809-9a94-4dce-bc6c-9ba2174bfd33 (v1:metadata.name)
    Mounts:
      /outputs from outputs (rw)
      /var/run/secrets/kubernetes.io/serviceaccount from kube-api-access-qmxtv (ro)
Conditions:
  Type                        Status
  PodReadyToStartContainers   False 
  Initialized                 True 
  Ready                       False 
  ContainersReady             False 
  PodScheduled                True 
Volumes:
  inputs:
    Type:          HostPath (bare host directory volume)
    Path:          /app-engine-shared-inputs/task-run-inputs-10f80809-9a94-4dce-bc6c-9ba2174bfd33
    HostPathType:  
  outputs:
    Type:          HostPath (bare host directory volume)
    Path:          /app-engine-shared-inputs/task-run-outputs-10f80809-9a94-4dce-bc6c-9ba2174bfd33
    HostPathType:  
  images-datasets:
    Type:          HostPath (bare host directory volume)
    Path:          /app-engine-shared-datasets
    HostPathType:  
  kube-api-access-qmxtv:
    Type:                    Projected (a volume that contains injected data from multiple sources)
    TokenExpirationSeconds:  3607
    ConfigMapName:           kube-root-ca.crt
    Optional:                false
    DownwardAPI:             true
QoS Class:                   Guaranteed
Node-Selectors:              <none>
Tolerations:                 node.kubernetes.io/not-ready:NoExecute op=Exists for 300s
                             node.kubernetes.io/unreachable:NoExecute op=Exists for 300s
Events:
  Type    Reason     Age    From               Message
  ----    ------     ----   ----               -------
  Normal  Scheduled  2m47s  default-scheduler  Successfully assigned app-engine-tasks/identitywithwsidicomimage-10f80809-9a94-4dce-bc6c-9ba2174bfd33 to d429b05f01eb
  Normal  Pulled     2m46s  kubelet            Container image "cytomineuliege/alpine-task-utils:latest" already present on machine
  Normal  Created    2m46s  kubelet            Created container: permissions
  Normal  Started    2m46s  kubelet            Started container permissions
  Normal  Pulled     2m45s  kubelet            Container image "172.16.238.4:5000/com/cytomine/dummy/identity/wsidicom/image:1.0.0" already present on machine
  Normal  Created    2m45s  kubelet            Created container: task
  Normal  Started    2m45s  kubelet            Started container task
  Normal  Pulled     2m45s  kubelet            Container image "cytomineuliege/alpine-task-utils:latest" already present on machine
  Normal  Created    2m45s  kubelet            Created container: outputs-sending
  Normal  Started    2m45s  kubelet            Started container outputs-sending
```
each task pod contains:

1. `permissions` container : manages file permissions for the task
2. `inputs-sending` container : to provision inputs to task in `cluster mode`
3. `task` container : this is the container packaged by the developer, the main app
4. `outputs-sending` container : to send the outputs back to app-engine and cytomine

each container `status` should be `terminated` and the `reason` completed when successful, all resource limits will be included and any errors will be in the `events` table.

#### Debugging Options

1. For provisioning issues, check the logs of the task run in app-engine commands **1** above.
2. For scheduling issues, check the logs of `K3s` using the command number **2** above followed by number **4**.
3. For app issues, check the logs of the app inside the pod using the command number **2** above followed by number **3**.

## Frequently Asked Questions

#### 1. My task is stuck in `Queuing` state?
The most common reason for this is that the task failed to be scheduled, either due to

##### Insufficient resources
The task requires ram and cpu time to be scheduled in the correct node, tasks have default 1 cpu and 4Gi of ram and infinite ephemeral storage, if these resources can't be allocated to the
task scheduling fails leaving the task in queuing state in UI, also if the task consumes all the storage available on the node while running it will crash because kubernetes will
evict the pod to protect the node.
##### Disk pressure
Kubernetes protects nodes by not scheduling pods/tasks to node with over 90% disk use by tainting (tagging) the node (local installation using k3s has only on node) as invalid for
running pods.

Use debugging option **2** to check the container status something similar to the following should be in the events table of your pod, and in case of failure in 
scheduling it means you need to update the descriptor to add more resources if the issue was cpu or ram, or free some space in your disk.

```
Events:
  Type    Reason     Age    From               Message
  ----    ------     ----   ----               -------
  Warning  FailedScheduling  48s  default-scheduler  0/1 nodes are available: 1 node(s) had untolerated taint {node.kubernetes.io/disk-pressure: }. preemption: 0/1 nodes are available: 1 Preemption is not helpful for scheduling.

```
To check your disk space use and availability run the following command
```
df -h
```
which will generate a similar output to the following 

```
some-user:~/cytomine$ df -h
Filesystem      Size  Used Avail Use% Mounted on
tmpfs           6.3G  5.3M  6.3G   1% /run
/dev/nvme0n1p2  937G  650G  240G  74% /            #<--------------- this should be below 90%
tmpfs            32G   20M   32G   1% /dev/shm
tmpfs           5.0M   12K  5.0M   1% /run/lock
efivarfs        246K  173K   69K  72% /sys/firmware/efi/efivars
/dev/nvme0n1p1  1.1G  6.2M  1.1G   1% /boot/efi
tmpfs           512M   12K  512M   1% /var/snap/microk8s/common/var/lib/kubelet/pods/afefb481-e869-4755-b0b9-f4e0711f3e7b/volumes/kubernetes.io~projected/kube-api-access-9g6bf
tmpfs           170M   12K  170M   1% /var/snap/microk8s/common/var/lib/kubelet/pods/7cb83863-5bb2-45ce-aa24-a6780a04d08b/volumes/kubernetes.io~projected/kube-api-access-wgsp2
```

#### 2. My task failed after being in queuing for a while, In the app-engine logs, I see `Failed to read file` what does it mean?
The most common reason for this is that the task crashed after consuming all available ephemeral storage or failed to generate the output file
to handle the ephemeral storage issue check question number **1** above. Use debugging option **3** to confirm that the task actually generated the file successfully.
All task-run input/output files are in [app-engine storage](/dev-guide/algorithms/task/#storage) with the UUID of the task-run if the file is not there this means it most likely 
not generated by the task.

::: warning
For now all input/output files generated by the task **MUST** have no extension and **MUST** exactly match the name of the parameters in the descriptor otherwise app-engine 
will fail to process them.  
:::


 

