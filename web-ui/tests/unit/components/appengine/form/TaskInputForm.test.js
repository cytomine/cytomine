import {shallowMount} from '@vue/test-utils';

import TaskInputForm from '@/components/appengine/forms/TaskInputForm.vue';
import Task from '@/utils/appengine/task';
import {hasBinaryType} from '@/utils/app';

jest.mock('@/utils/appengine/task');
jest.mock('@/utils/app', () => ({
  hasBinaryType: jest.fn(),
}));

describe('TaskInputForm.vue', () => {
  const mockTask = {
    namespace: 'mock-namespace',
    version: '1.0.0',
    fetchOutputs: jest.fn(),
  };

  const createWrapper = (overrides = {}) => {
    return shallowMount(TaskInputForm, {
      propsData: {
        inputs: {},
        task: mockTask,
        ...overrides.propsData,
      },
      stubs: {
        'AppEngineField': true,
      },
    });
  };

  it('should sort task inputs when fetched on created', async () => {
    const expectedTask = {
      namespace: 'sort-namespace',
      version: '1.2.3',
      fetchOutputs: jest.fn(),
    };
    const expectedInputs = [
      {id: 1, name: 'inputB', type: {id: 'string'}, default: 'hello'},
      {id: 2, name: 'inputA', type: {id: 'number'}, default: 4.2},
    ];
    Task.fetchTaskInputs.mockResolvedValue(expectedInputs);
    const wrapper = createWrapper({
      propsData: {
        task: expectedTask,
      }
    });

    await wrapper.vm.$nextTick();
    await wrapper.vm.$nextTick();

    expect(Task.fetchTaskInputs).toHaveBeenCalledWith(expectedTask.namespace, expectedTask.version);

    const names = wrapper.vm.taskInputs.map(i => i.name);
    expect(names).toEqual(['inputA', 'inputB']);
  });

  it('should emit input with parsed default values', async () => {
    const expectedInputs = [
      {id: 1, name: 'inputB', type: {id: 'string'}, default: 'hello'},
      {id: 2, name: 'inputA', type: {id: 'number'}, default: 4.2},
    ];
    Task.fetchTaskInputs.mockResolvedValue(expectedInputs);
    const wrapper = createWrapper();

    await wrapper.vm.$nextTick();
    await wrapper.vm.$nextTick();

    const emitted = wrapper.emitted().input[0][0];

    for (const input of expectedInputs) {
      expect(emitted[input.name].value).toBe(input.default);
    }
  });

  it('should set hasBinaryData to false when no input is binary', async () => {
    const binaryInput = {id: 1, name: 'inputB', type: {id: 'image'}, default: null};
    const expectedInputs = [
      binaryInput,
      {id: 2, name: 'inputA', type: {id: 'number'}, default: 4.2},
    ];
    Task.fetchTaskInputs.mockResolvedValue(expectedInputs);
    hasBinaryType.mockImplementation((input) => input.name === binaryInput.name);

    const wrapper = createWrapper();

    await wrapper.vm.$nextTick();
    await wrapper.vm.$nextTick();

    expect(wrapper.vm.hasBinaryData).toBe(true);
  });

  it('should call resetForm when inputs prop becomes empty', async () => {
    const wrapper = createWrapper({
      propsData: {
        inputs: {some: 'value'},
      },
    });
    const spy = jest.spyOn(wrapper.vm, 'resetForm');

    await wrapper.setProps({inputs: {}});

    expect(spy).toHaveBeenCalled();
  });

  it('should fetch inputs when task changes', async () => {
    const wrapper = createWrapper();

    await wrapper.vm.$nextTick();

    const newTask = {
      namespace: 'new-ns',
      version: '2.0',
      fetchOutputs: jest.fn(),
    };

    await wrapper.setProps({task: newTask});

    expect(Task.fetchTaskInputs).toHaveBeenCalledWith(newTask.namespace, newTask.version);
  });

  it('should emit updated input value on change', async () => {
    const wrapper = createWrapper({
      propsData: {
        inputs: {
          test: {value: 'old'},
        },
      },
    });
    const newValue = 'new';

    wrapper.vm.onInputChange('test', newValue);

    const emitted = wrapper.emitted().input.pop()[0];
    expect(emitted.test.value).toBe(newValue);
  });
});
