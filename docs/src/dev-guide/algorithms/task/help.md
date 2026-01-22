---
title: Help
---

# {{ $frontmatter.title }}

::: warning
App Engine and its Task system are still in BETA. Therefore, all information presented in this documentation is subject to potentially breaking changes.
:::
## How To Debug

#### Tasks run steps :
1. Select the task you want to run. (drop down menu)
2. select inputs (by entering the values or selecting them from cytomine)
2. provision task inputs.
3. Run the task.
4. expand the run to see both inputs and outputs. (output images are synchronously added to the project)

::: tip
provisioning and scheduling a task are done sequentially when you click the run task button in UI
:::

Task run goes through different states depending on many factors like availability of data and other things check out [Task Run States](/dev-guide/algorithms/task/run) for more details. Running 
a task does not always run as expected. So to help developers debug their tasks and identify issues, a few logs are available to help troubleshoot.

#### Logs

1. Check the logs of the task run in app-engine using the following command
```
docker compose logs -f app-engine
```
2. Check the logs of `K3s` the following command lists all the pods if you have `kubectl` installed
```
kubectl --kubeconfig=./.kube/shared/config -n app-engine-tasks get pods
```
otherwise you can use
```
docker compose exec -it k3s kubectl -n app-engine-tasks get pods
```
3. To check the logs of the app/algorithm inside the pod, `<name-of-pod>` comes from name column of the command number 2 above
```
kubectl --kubeconfig=./.kube/shared/config -n app-engine-tasks logs <name-of-pod>
```
if you don't have `kubectl` installed you can use the following command, `<name-of-pod>` comes from name column of the command number 2 above
```
docker compose exec -it k3s kubectl -n app-engine-tasks logs <name-of-pod>
```

#### Debugging steps

1. For provisioning issues, check the logs of the task run in app-engine commands 1 above.
2. For scheduling issues, check the logs of `K3s` using the command number 2 above.
3. For algorithm issues, check the logs of the app/algorithm inside the pod using the command number 3 above.

## Frequently Asked Questions

#### My task is stuck in `Queuing` state?
The most common reason for this is that the task failed to be scheduled, either due to insufficient resources or disk pressure.
#### My task failed after being in queuing for a while, In the app-engine logs, I see `Failed to read file` what does it mean?
The most common reason for this is that the task failed to be scheduled or failed to generate the output file
#### My task failed to be scheduled, what should I do?
Check the logs of the task run to see why it failed. usually it's due to insufficient resources or disk pressure.
#### My task failed to be scheduled, and I see `disk-pressure` what does it mean?
This means that the node that the task is scheduled on has insufficient disk space. Kubernetes expects the disk to have at least 10% empty space. The usual suspect is docker volumes.
this might make the task stuck in `Queuing` state.

