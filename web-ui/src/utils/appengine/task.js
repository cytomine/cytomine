import Vue from 'vue';
import Model from './model';
import {Cytomine} from '@/api';
import {isGeometry} from '@/utils/app';


export default class Task extends Model {
  /** @inheritdoc */
  static get callbackIdentifier() {
    return 'app-engine/tasks';
  }

  /** @inheritdoc */
  _initProperties() {
    super._initProperties();
    this.namespace = null;
    this.version = null;
    this.name = null;
    this.description = null;
    this.authors = [];
    this.outputs = [];
  }

  static async fetchAll() {
    let {data} = await Cytomine.instance.api.get(`${this.callbackIdentifier}`);
    return data.map(props => new Task(props));
  }

  static async fetchNamespaceVersion(namespace, version, host = null) {
    let {data} = await Cytomine.instance.api.get(`${this.callbackIdentifier}/${namespace}/${version}`, {params: {host}});
    return data;
  }

  static async uploadTask(compressedTask) {
    return await Cytomine.instance.api.post(`${this.callbackIdentifier}`, compressedTask);
  }

  static async fetchTaskInputs(namespace, version) {
    let {data} = await Cytomine.instance.api.get(`${this.callbackIdentifier}/${namespace}/${version}/inputs`);
    return data;
  }

  // Step-1: Create TaskRun Must be part of a project to run a task
  static async createTaskRun(project, namespace, version, image) {
    let {data} = await Cytomine.instance.api.post(`/app-engine/project/${project}/tasks/${namespace}/${version}/runs`, {'image': image});
    return data;
  }

  // Step-2: Provision task / user inputs
  static async batchProvisionTask(project, runId, params) {
    let {data} = await Cytomine.instance.api.put(`/app-engine/project/${project}/task-runs/${runId}/input-provisions`, params);
    return data;
  }

  static async singleProvisionTask(project, runId, paramName, param) {
    let {data} = await Cytomine.instance.api.put(`/app-engine/project/${project}/task-runs/${runId}/input-provisions/${paramName}`, param);
    return data;
  }

  // Step-3 Run/Execute the Provisioned Task
  static async runTask(project, runId) {
    let {data} = await Cytomine.instance.api.post(`/app-engine/project/${project}/task-runs/${runId}/state-actions`, {'desired': 'RUNNING'});
    return data;
  }

  // Status: get the current status of the TaskRun
  static async fetchTaskRunStatus(project, runId) {
    let {data} = await Cytomine.instance.api.get(`/app-engine/project/${project}/task-runs/${runId}`);
    return data;
  }

  async fetchOutputs() {
    let {data} = await Cytomine.instance.api.get(`${this.callbackIdentifier}/${this.namespace}/${this.version}/outputs`);
    Vue.set(this, 'outputs', data);

    return data;
  }

  hasGeometryOutput() {
    return this.outputs.some(output => isGeometry(output));
  }
}
