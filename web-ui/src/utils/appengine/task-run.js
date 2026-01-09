import Model from './model';
import Task from './task';
import {Cytomine} from '@/api';
import {BINARY_TYPES} from '@/utils/app';

export default class TaskRun extends Model {
  static get STATES() {
    return {
      CREATED: 'CREATED',
      PROVISIONED: 'PROVISIONED',
      QUEUING: 'QUEUING',
      QUEUED: 'QUEUED',
      PENDING: 'PENDING',
      RUNNING: 'RUNNING',
      FAILED: 'FAILED',
      FINISHED: 'FINISHED',
    };
  }

  static get TERMINAL_STATES() {
    return new Set([
      this.STATES.FAILED,
      this.STATES.FINISHED,
    ]);
  }

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

  isFinished() {
    return this.state === TaskRun.STATES.FINISHED;
  }

  isTerminalState() {
    return TaskRun.TERMINAL_STATES.has(this.state);
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

  // Step-3 Run/Execute the Provisioned Task
  async start() {
    let {data} = await Cytomine.instance.api.post(`${this.uri}/state-actions`, {'desired': 'RUNNING'});
    return data;
  }

  async fetchInputs() {
    if (this.state === TaskRun.STATES.CREATED) {
      return null;
    }

    this.inputs = (await Cytomine.instance.api.get(`${this.uri}/inputs`)).data;

    return this.inputs;
  }

  async fetchOutputs() {
    if (this.state !== TaskRun.STATES.FINISHED) {
      return null;
    }

    this.outputs = (await Cytomine.instance.api.get(`${this.uri}/outputs`)).data;

    return this.outputs;
  }

}
