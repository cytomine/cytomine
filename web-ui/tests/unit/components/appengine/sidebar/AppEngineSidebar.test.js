import {createLocalVue, shallowMount} from '@vue/test-utils';
import Buefy from 'buefy';

import AppEngineSideBar from '@/components/appengine/sidebar/AppEngineSidebar.vue';

const mockTask1 = {
  id: 1,
  name: 'Task 1',
  namespace: 'namespace1',
  version: '0.1.0',
};

const mockTask2 = {
  id: 2,
  name: 'Task 2',
  namespace: 'namespace2',
  version: '0.5.0',
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

const mockFetchInputs = jest.fn(() => Promise.resolve());
const mockFetchOutputs = jest.fn(() => Promise.resolve());

jest.mock('@/api', () => ({
  Cytomine: {
    instance: {
      api: {
        get: jest.fn(),
      },
    },
  },
}));

jest.mock('@/utils/image-utils', () => ({
  isWebPSupported: jest.fn(() => true)
}));

jest.mock('@/utils/appengine/task', () => ({
  fetchAll: jest.fn(() => Promise.resolve([
    mockTask1,
    mockTask2,
  ])),
  fetchTaskRunStatus: jest.fn((_, taskRunId) =>
    Promise.resolve(taskRunId === mockTaskRun1.id ? mockTaskRun1 : mockTaskRun2)
  ),
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
    fetchInputs: mockFetchInputs,
    fetchOutputs: mockFetchOutputs,
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

describe('AppEngineSideBar.vue', () => {
  const fetchTasksSpy = jest.spyOn(AppEngineSideBar.methods, 'fetchTasks');
  const fetchTaskRunsSpy = jest.spyOn(AppEngineSideBar.methods, 'fetchTaskRuns');

  const localVue = createLocalVue();
  localVue.use(Buefy);

  let wrapper;

  beforeEach(async () => {
    fetchTasksSpy.mockClear();
    fetchTaskRunsSpy.mockClear();

    wrapper = shallowMount(AppEngineSideBar, {
      localVue,
      mocks: {
        $t: (key) => key
      },
      computed: {
        currentProject: () => ({id: 42}),
      },
    });
  });

  it('should render and show the task options', () => {
    const headers = wrapper.findAll('header');
    expect(headers.length).toBe(2);
    expect(headers.at(0).text()).toBe('app-engine.execute-a-task');
    expect(headers.at(1).text()).toBe('app-engine.previous-task-runs');

    const taskOptions = wrapper.findAll('option');
    expect(taskOptions.length).toBe(2);
    expect(taskOptions.at(0).text()).toBe(`${mockTask1.name} (${mockTask1.version})`);
    expect(taskOptions.at(1).text()).toBe(`${mockTask2.name} (${mockTask2.version})`);

    expect(wrapper.find('h5').text()).toBe('app-engine.runs.title');
  });

  it('should display the task name and description when a task is selected', async () => {
    await wrapper.setData({selectedTask: mockTask1});

    expect(wrapper.find('.content p').text()).toBe(`${mockTask1.name} (${mockTask1.version})`);

    const descriptionField = wrapper.find('.no-description');
    expect(descriptionField.exists()).toBe(true);
    expect(descriptionField.text()).toBe('app-engine.task.no-description');
  });

  it('should fetch data on component created', async () => {
    expect(fetchTasksSpy).toHaveBeenCalled();
    expect(fetchTasksSpy).toHaveBeenCalledTimes(1);
    expect(fetchTaskRunsSpy).toHaveBeenCalled();
    expect(fetchTaskRunsSpy).toHaveBeenCalledTimes(1);
  });

  it('should mark not finished runs as failed on component created', async () => {
    const expectedTaskRun2 = {...mockTaskRun2, state: 'FAILED'};

    expect(wrapper.vm.tasks).toStrictEqual([mockTask1, mockTask2]);
    expect(wrapper.vm.trackedTaskRuns).toMatchObject([mockTaskRun1, expectedTaskRun2]);
  });

  it('should update tracked task runs every 2 seconds', async () => {
    jest.useFakeTimers();
    jest.advanceTimersByTime(2000);

    expect(fetchTasksSpy).toHaveBeenCalled();
    expect(fetchTaskRunsSpy).toHaveBeenCalled();

    jest.useRealTimers();
  });

  it('should fetch inputs for all task runs', async () => {
    mockFetchInputs.mockClear();

    await wrapper.vm.fetchTaskRuns();

    expect(mockFetchInputs).toHaveBeenCalled();
    expect(mockFetchInputs).toHaveBeenCalledTimes(2);
  });

  it('should fetch outputs for finished task runs', async () => {
    mockFetchOutputs.mockClear();

    await wrapper.vm.fetchTaskRuns();

    expect(mockFetchOutputs).toHaveBeenCalled();
    expect(mockFetchOutputs).toHaveBeenCalledTimes(1);
  });
});
