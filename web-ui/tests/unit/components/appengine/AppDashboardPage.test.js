import {shallowMount} from '@vue/test-utils';

import AppDashboardPage from '@/components/appengine/AppDashboardPage.vue';
import Task from '@/utils/appengine/task';
import TaskRun from '@/utils/appengine/task-run';
import {flushPromises} from '../../../utils';

const mockTask = {
  id: 1,
  name: 'Test Task',
  namespace: 'namespace',
  version: '0.1.0',
};

const mockTaskRun1 = {
  id: 'c11e717a-d5ac-4655-80c7-06946d266264',
  state: 'FINISHED',
  project: 42,
  taskRunId: 'c11e717a-d5ac-4655-80c7-06946d266264',
};

const mockTaskRun2 = {
  id: '5f41ca2c-9b68-49fe-8f16-4e8005eb6893',
  state: 'RUNNING',
  project: 42,
  taskRunId: '5f41ca2c-9b68-49fe-8f16-4e8005eb6893',
};

const makeTaskRun = (overrides = {}) => ({
  id: '42',
  taskRunId: 'c6e418dd-b315-49fe-8a02-ee8e4684ef61',
  project: '999',
  user: {username: 'admin', name: 'Admin User'},
  createdAt: '1775649424070',
  state: 'FINISHED',
  task: {name: 'Test App', version: '1.0.0', namespace: 'namespace'},
  inputs: null,
  outputs: null,
  isTerminalState: jest.fn(() => true),
  fetchInputs: jest.fn(),
  fetchOutputs: jest.fn(),
  delete: jest.fn(),
  ...overrides,
});

jest.mock('@/utils/appengine/task', () => ({
  fetchTaskRunStatus: jest.fn(() => Promise.resolve(mockTask))
}));

jest.mock('@/utils/appengine/task-run', () => {
  const STATES = {
    CREATED: 'CREATED',
    PROVISIONED: 'PROVISIONED',
    QUEUING: 'QUEUING',
    QUEUED: 'QUEUED',
    PENDING: 'PENDING',
    RUNNING: 'RUNNING',
    FAILED: 'FAILED',
    FINISHED: 'FINISHED',
  };

  const mockIsFinished = jest.fn(function () {
    return this.state === STATES.FINISHED;
  });

  const mockTaskRun = jest.fn().mockImplementation((resource) => ({
    ...resource,
    isFinished: mockIsFinished,
  }));

  mockTaskRun.fetchByProject = jest.fn(() => Promise.resolve([
    mockTaskRun1,
    mockTaskRun2,
  ]));

  Object.defineProperty(mockTaskRun, 'STATES', {
    get: () => STATES,
  });

  return {
    __esModule: true,
    default: mockTaskRun,
  };
});

describe('AppDashboardPage.vue', () => {
  const createWrapper = (options = {}) => shallowMount(
    AppDashboardPage,
    {
      mocks: {
        $t: (key) => key,
      },
      computed: {
        currentProject: () => ({id: '999'}),
      },
      stubs: {
        'b-table': true,
      },
      ...options,
    },
  );

  describe('initial data', () => {
    it('should start with currentPage set to 1', () => {
      const wrapper = createWrapper();

      expect(wrapper.vm.currentPage).toBe(1);
    });

    it('should start with perPage set to 10', () => {
      const wrapper = createWrapper();

      expect(wrapper.vm.perPage).toBe(10);
    });

    it('should start with empty list of taskRuns', () => {
      const wrapper = createWrapper();

      expect(wrapper.vm.taskRuns).toStrictEqual([]);
    });
  });

  describe('created lifecycle', () => {
    it('should populate taskRuns when created', async () => {
      const run = makeTaskRun();
      const taskRuns = [run];
      TaskRun.fetchByProject.mockResolvedValueOnce(taskRuns);

      const wrapper = createWrapper();
      await flushPromises();

      expect(TaskRun.fetchByProject).toHaveBeenCalledWith(run.project);
      expect(wrapper.vm.taskRuns).toHaveLength(taskRuns.length);
    });

    it('should sort taskRuns by created date', async () => {
      const older = makeTaskRun({id: 'run-1', taskRunId: 'run-1', createdAt: '1704067200000'});
      const newer = makeTaskRun({id: 'run-2', taskRunId: 'run-2', createdAt: '1717200000000'});

      TaskRun.fetchByProject.mockResolvedValue([newer, older]);

      Task.fetchTaskRunStatus.mockImplementation(async (_pid, taskRunId) => {
        const run = taskRunId === 'run-1' ? older : newer;
        return {id: run.id, taskRunId: run.taskRunId, state: run.state, createdAt: run.createdAt, task: run.task};
      });

      const wrapper = createWrapper();
      await flushPromises();

      const ids = wrapper.vm.taskRuns.map((r) => r.taskRunId);
      expect(ids).toStrictEqual([newer.id, older.id]);
    });
  });

  describe('opening task run detail', () => {
    it('should fetch inputs when inputs are null on details open', async () => {
      const run = makeTaskRun({inputs: null, outputs: []});
      const wrapper = createWrapper();

      await wrapper.vm.onDetailsOpen(run);

      expect(run.fetchInputs).toHaveBeenCalledTimes(1);
    });

    it('should fetch outputs when outputs are null on details open', async () => {
      const run = makeTaskRun({inputs: [], outputs: null});
      const wrapper = createWrapper();

      await wrapper.vm.onDetailsOpen(run);

      expect(run.fetchOutputs).toHaveBeenCalledTimes(1);
    });

    it('should not fetch inputs when inputs are already loaded on details open', async () => {
      const run = makeTaskRun({inputs: [{key: 'param1', value: 'val1'}], outputs: null});
      const wrapper = createWrapper();

      await wrapper.vm.onDetailsOpen(run);

      expect(run.fetchInputs).not.toHaveBeenCalled();
    });

    it('should not fetch outputs when outputs are already loaded on details open', async () => {
      const run = makeTaskRun({inputs: null, outputs: [{key: 'result', value: '42'}]});
      const wrapper = createWrapper();

      await wrapper.vm.onDetailsOpen(run);

      expect(run.fetchOutputs).not.toHaveBeenCalled();
    });
  });

  describe('formatDate', () => {
    it('should return formatted date string when given valid ISO date', () => {
      const wrapper = createWrapper();
      const date = '2026-04-09T08:21:23.90721';

      const result = wrapper.vm.formatDate(date);

      expect(typeof result).toBe('string');
      expect(result.length).toBeGreaterThan(0);
      expect(result).toBe('09 Apr 2026, 08:21');
    });
  });
});
