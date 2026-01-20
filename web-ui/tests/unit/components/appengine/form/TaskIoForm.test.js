import {shallowMount, createLocalVue} from '@vue/test-utils';
import Buefy from 'buefy';

import TaskIoForm from '@/components/appengine/forms/TaskIoForm.vue';
import Task from '@/utils/appengine/task';

jest.mock('@/api', () => ({
  Cytomine: {
    instance: {
      api: {
        get: jest.fn(),
      },
    },
  },
}));

jest.mock('@/utils/appengine/task');

jest.mock('@/utils/image-utils', () => ({
  isWebPSupported: jest.fn(() => true)
}));

describe('TaskIoForm.vue', () => {
  const localVue = createLocalVue();
  localVue.use(Buefy);

  const mockTask = {
    namespace: 'mock-namespace',
    version: '1.0.0',
  };

  const mockInputs = [
    {id: 1, name: 'input1', type: {id: 'string'}, default: 'hello'},
    {id: 2, name: 'input2', type: {id: 'number'}, default: 4.2},
  ];

  const createWrapper = (overrides = {}) => {
    return shallowMount(TaskIoForm, {
      propsData: {
        task: mockTask,
        projectId: 1,
        ...overrides.propsData,
      },
      mocks: {
        $t: (key) => key,
      },
    });
  };

  beforeEach(() => {
    Task.fetchTaskInputs.mockResolvedValue(mockInputs);
    Task.createTaskRun.mockResolvedValue({id: 123});
    Task.batchProvisionTask.mockResolvedValue();
    Task.runTask.mockResolvedValue({id: 123});
  });

  it('should render translated text', () => {
    const wrapper = createWrapper();

    const headers = wrapper.findAll('h3');
    expect(headers.length).toBe(1);
    expect(headers.at(0).text()).toBe('app-engine.ae-run-task');
    console.log(wrapper.html());

    const buttons = wrapper.findAll('b-button');
    expect(buttons.length).toBe(2);
    expect(buttons.at(0).text()).toBe('button-clear');
    expect(buttons.at(1).text()).toBe('app-engine.ae-run-task');
  });
});
