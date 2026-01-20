import {shallowMount, createLocalVue} from '@vue/test-utils';
import Buefy from 'buefy';

import TaskIoForm from '@/components/appengine/forms/TaskIoForm.vue';
import Task from '@/utils/appengine/task';
import {flushPromises} from '../../../../utils';

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

  const mockOtherTask = {
    namespace: 'another-mocked-namespace',
    version: '0.2.3',
  };

  const mockOtherInputs = [
    {id: 3, name: 'other1', type: {id: 'string'}, default: 'default-value'},
    {id: 4, name: 'other2', type: {id: 'int'}, default: 42},
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
      stubs: {
        'b-button': {
          template: '<button><slot /></button>',
        },
        'b-field': true,
      },
    });
  };

  it('should render translated text', () => {
    const wrapper = createWrapper();

    const headers = wrapper.findAll('h3');
    expect(headers.length).toBe(1);
    expect(headers.at(0).text()).toBe('app-engine.ae-run-task');
    console.log(wrapper.html());

    const buttons = wrapper.findAll('button');
    expect(buttons.length).toBe(2);
    expect(buttons.at(0).text()).toBe('button-clear');
    expect(buttons.at(1).text()).toBe('app-engine.ae-run-task');
  });

  describe('task input form', () => {
    it('should initialise inputs with initial task', async () => {
      Task.fetchTaskInputs.mockResolvedValueOnce(mockInputs);

      const wrapper = createWrapper();

      await flushPromises();

      const expectedTaskInputs = mockInputs.reduce(
        (acc, {name, type, default: value}) => {
          acc[name] = {type, value};
          return acc;
        }, {});

      expect(wrapper.vm.taskInputs).toStrictEqual(mockInputs);
      expect(wrapper.vm.inputs).toStrictEqual(expectedTaskInputs);
    });

    it('should change inputs when task changes', async () => {
      Task.fetchTaskInputs.mockResolvedValueOnce(mockInputs);
      Task.fetchTaskInputs.mockResolvedValueOnce(mockOtherInputs);

      const wrapper = createWrapper();
      await flushPromises();

      await wrapper.setProps({task: mockOtherTask});
      await flushPromises();

      const expectedTaskInputs = mockOtherInputs.reduce(
        (acc, {name, type, default: value}) => {
          acc[name] = {type, value};
          return acc;
        }, {});

      expect(wrapper.vm.taskInputs).toStrictEqual(mockOtherInputs);
      expect(wrapper.vm.inputs).toStrictEqual(expectedTaskInputs);
    });
  });
});
