import {shallowMount} from '@vue/test-utils';

import AppDashboardPage from '@/components/appengine/AppDashboardPage.vue';
import TaskRun from '@/utils/appengine/task-run';
import {flushPromises} from '../../../utils';

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
      ...options,
    },
  );

  it('should populate taskRuns when created', async () => {
    const run = makeTaskRun({state: 'FINISHED'});
    const taskRuns = [run];
    TaskRun.fetchByProject.mockResolvedValueOnce(taskRuns);

    const wrapper = createWrapper();
    await flushPromises();

    expect(TaskRun.fetchByProject).toHaveBeenCalledWith(run.project);
    expect(wrapper.vm.taskRuns).toHaveLength(taskRuns.length);
  });
});
