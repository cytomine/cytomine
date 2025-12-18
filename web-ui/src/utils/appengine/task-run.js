import Model from './model';
import Task from './task';
import {Cytomine} from '@/api';
import {BINARY_TYPES} from '@/utils/app';

export default class TaskRun extends Model {
  /** @inheritdoc */
  static get callbackIdentifier() {
    return '/app-engine/project/${project}/task-runs'; // not used
  }

  get uri() {
    if (!this.project) {
      throw new Error('TaskRun.project is required');
    }
    if (this.isNew()) {
      return `/app-engine/project/${this.project}/task-runs`;
      //   return `${this.callbackIdentifier}.json`;
    } else {
      //   return `${this.callbackIdentifier}/${this.id}.json`;
      return `/app-engine/project/${this.project}/task-runs/${this.id}`;
    }
  }

  _initProperties() {
    super._initProperties();
    this.project = null;
    this.task = new Task();
    this.state = null;
    /* eslint-disable */
    this.created_at = null;
    this.updated_at = null;
    this.last_state_transition_at = null;
    /* eslint-enable */
  }

  static async fetchByProject(projectId) {
    let {data} = await Cytomine.instance.api.get(`project/${projectId}/task-runs`);
    return data;
  }

  // Step-2: Provision task / user inputs
  async batchProvisionTask(params) {
    let {data} = await Cytomine.instance.api.put(`${this.uri}/input-provisions`, params);
    return data;
  }

  async singleProvisionTask(paramName, param) {
    let {data} = Cytomine.instance.api.put(`${this.uri}/input-provisions/${paramName}`, param);
    return data;
  }

  static async provisionTargetImage(project, taskRunId, paramName, param) {
    let {data} = await Cytomine.instance.api.put(`/app-engine/project/${project}/task-runs/${taskRunId}/input-provisions/${paramName}/geometry`, param);
    return data;
  }

  // Step-3 Run/Execute the Provisioned Task
  async start() {
    let {data} = await Cytomine.instance.api.post(`${this.uri}/state-actions`, {'desired': 'RUNNING'});
    return data;
  }

  async fetchInputs() {
    this.inputs = (await Cytomine.instance.api.get(`${this.uri}/inputs`)).data;

    const binaryInputs = this.inputs.filter(input => BINARY_TYPES.includes(input.type.toLowerCase()));

    await Promise.all(
      binaryInputs.map(async (input) => {
        input.value = await this.fetchSingleIO(input.param_name, 'input');
      })
    );

    return this.inputs;
  }

  async fetchOutputs() {
    this.outputs = (await Cytomine.instance.api.get(`${this.uri}/outputs`)).data;

    const binaryOutputs = this.outputs.filter(output => BINARY_TYPES.includes(output.type.toLowerCase()));

    await Promise.all(
      binaryOutputs.map(async (output) => {
        output.value = await this.fetchSingleIO(output.param_name, 'output');
      })
    );

    return this.outputs;
  }

  async fetchSingleIO(parameterName, type) {
    let {data} = await Cytomine.instance.api.get(`${this.uri}/${type}/${parameterName}`, {responseType: 'arraybuffer'});
    return data;
  }
}
