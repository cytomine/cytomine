import {shallowMount} from '@vue/test-utils';

import AppBottomDrawer from '@/components/appengine/AppBottomDrawer.vue';
import Task from '@/utils/appengine/task';
import TaskRun from '@/utils/appengine/task-run';

jest.mock('@/utils/appengine/task', () => ({
  fetchAll: jest.fn(),
  fetchTaskRunStatus: jest.fn(),
  createTaskRun: jest.fn(),
  batchProvisionTask: jest.fn(),
  singleProvisionTask: jest.fn(),
  runTask: jest.fn(),
}));

jest.mock('@/utils/appengine/task-run', () => {
  const mockConstructor = jest.fn().mockImplementation((resource) => ({
    ...resource,
    fetch: jest.fn(),
    isTerminalState: jest.fn(() => false),
    project: null,
  }));

  mockConstructor.fetchByProject = jest.fn();

  return mockConstructor;
});

jest.mock('@/utils/app', () => ({
  formatTaskName: jest.fn(() => 'Mocked Task Run'),
}));

describe('AppBottomDrawer.vue', () => {
  const mockTasks = [
    {
      namespace: 'segmentation',
      version: '1.0.0',
      name: 'Segmentation Task',
      description: 'Mock task description',
    },
  ];

  const mockTaskRuns = [
    {
      taskRunId: 1,
      createdAt: '2024-01-01T10:00:00Z',
      project: {id: 99},
    },
  ];

  const createWrapper = ({data = {}, storeOverrides = {}} = {}) => {
    const mockStore = {
      getters: {
        'currentProject/project': {
          id: 99,
        },
        'currentProject/currentViewer': {
          activeImage: 0,
          images: [{imageInstance: {id: 777}}],
        },
        ...storeOverrides,
      },
    };

    return shallowMount(AppBottomDrawer, {
      mocks: {
        $buefy: {
          toast: {
            open: jest.fn(),
          },
        },
        $eventBus: {
          $emit: jest.fn(),
        },
        $router: {
          push: jest.fn(),
        },
        $store: mockStore,
        $t: (key) => key,
      },
      stubs: {
        'b-button': {
          template: '<button @click="$emit(\'click\')"><slot /></button>',
        },
        'b-select': {
          props: ['value'],
          template: '<select><slot /></select>',
        },
        TaskInputForm: true,
        TaskRunStateIcon: true,
      },
      computed: {
        currentProject() {
          return mockStore.getters['currentProject/project'];
        },
      },
      data() {
        return {
          isCollapsed: true,
          selectedTask: null,
          tasks: [],
          inputs: {},
          isRunning: false,
          allTaskRuns: [],
          trackedTaskRuns: [],
          ...data,
        };
      },
    });
  };

  beforeEach(() => {
    jest.clearAllMocks();
    Task.fetchAll.mockResolvedValue(mockTasks);
    TaskRun.fetchByProject.mockResolvedValue(mockTaskRuns);
  });

  it('should fetch tasks on created', async () => {
    const wrapper = createWrapper();

    await wrapper.vm.$nextTick();
    await wrapper.vm.$nextTick();

    expect(Task.fetchAll).toHaveBeenCalled();
    expect(wrapper.vm.tasks).toEqual(mockTasks);
  });

  it('should toggle collapse state and emit collapse event', async () => {
    const wrapper = createWrapper();

    expect(wrapper.vm.isCollapsed).toBe(true);

    wrapper.vm.toggleCollapse();
    await wrapper.vm.$nextTick();

    expect(wrapper.vm.isCollapsed).toBe(false);
    expect(wrapper.emitted().collapse).toEqual([[false]]);
    expect(wrapper.vm.$eventBus.$emit).toHaveBeenCalledWith('updateMapSize');
  });

  it('should reset inputs', () => {
    const wrapper = createWrapper({
      data: {
        inputs: {
          threshold: {
            type: 'number',
            value: 0.5,
          },
        },
      },
    });

    wrapper.vm.resetInputs();

    expect(wrapper.vm.inputs).toEqual({});
  });

  it('should build input provisions correctly', () => {
    const wrapper = createWrapper({
      data: {
        inputs: {
          threshold: {
            type: 'number',
            value: 0.8,
          },
          label: {
            type: 'string',
            value: 'tumour',
          },
        },
      },
    });

    expect(wrapper.vm.getInputProvisions()).toEqual([
      {
        // eslint-disable-next-line camelcase
        param_name: 'threshold',
        type: 'number',
        value: 0.8,
      },
      {
        // eslint-disable-next-line camelcase
        param_name: 'label',
        type: 'string',
        value: 'tumour',
      },
    ]);
  });

  it('should run task with batch provisions when there is no binary data', async () => {
    Task.createTaskRun.mockResolvedValue({
      id: 123,
    });

    Task.runTask.mockResolvedValue({
      resource: {
        id: 123,
        state: 'RUNNING',
      },
    });

    const wrapper = createWrapper({
      data: {
        selectedTask: mockTasks[0],
        inputs: {
          threshold: {
            type: 'number',
            value: 0.8,
          },
        },
      },
    });

    Object.defineProperty(wrapper.vm, 'hasBinaryData', {
      value: false,
      writable: true,
    });

    await wrapper.vm.runTask();

    expect(Task.createTaskRun).toHaveBeenCalledWith(
      99,
      'segmentation',
      '1.0.0',
      777,
    );

    expect(Task.batchProvisionTask).toHaveBeenCalledWith(
      99,
      123,
      [
        {
          // eslint-disable-next-line camelcase
          param_name: 'threshold',
          type: 'number',
          value: 0.8,
        },
      ],
    );

    expect(Task.runTask).toHaveBeenCalledWith(99, 123);
    expect(wrapper.vm.trackedTaskRuns.length).toBeTruthy();
    expect(wrapper.vm.inputs).toEqual({});
  });

  it('should uns task with file provisions when binary data is present', async () => {
    const file = new File(['content'], 'input.txt', {type: 'text/plain'});

    Task.createTaskRun.mockResolvedValue({id: 456});
    Task.singleProvisionTask.mockResolvedValue();
    Task.runTask.mockResolvedValue({
      resource: {
        id: 456,
        state: 'RUNNING',
      },
    });

    const wrapper = createWrapper({
      data: {
        selectedTask: mockTasks[0],
        inputs: {
          upload: {
            type: 'file',
            value: file,
          },
        },
      },
    });

    Object.defineProperty(wrapper.vm, 'hasBinaryData', {
      value: true,
      writable: true,
    });

    await wrapper.vm.runTask();

    expect(Task.singleProvisionTask).toHaveBeenCalledTimes(1);
    expect(Task.singleProvisionTask.mock.calls[0][0]).toBe(99);
    expect(Task.singleProvisionTask.mock.calls[0][1]).toBe(456);
    expect(Task.singleProvisionTask.mock.calls[0][2]).toBe('upload');

    expect(Task.runTask).toHaveBeenCalledWith(99, 456);
  });

  it('should show an error toast when task execution fails', async () => {
    Task.createTaskRun.mockRejectedValue({
      response: {
        data: {
          message: 'Server exploded',
        },
      },
    });

    const wrapper = createWrapper({
      data: {
        selectedTask: mockTasks[0],
      },
    });

    await wrapper.vm.runTask();

    expect(wrapper.vm.$buefy.toast.open).toHaveBeenCalledWith(
      expect.objectContaining({
        message: 'Error : Server exploded',
        type: 'is-danger',
      }),
    );

    expect(wrapper.vm.isRunning).toBe(false);
  });
});
