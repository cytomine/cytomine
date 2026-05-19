import {shallowMount} from '@vue/test-utils';

import TaskRunParametersTable from '@/components/appengine/task-run/TaskRunParametersTable.vue';

describe('TaskRunParametersTable.vue', () => {
  const createWrapper = (options = {}) => shallowMount(TaskRunParametersTable, {
    propsData: {
      parameters: options.parameters || [],
      projectId: 42,
      type: 'input',
    },
    mocks: {
      $t: (key) => key,
    },
    stubs: {
      'b-table': {
        name: 'b-table',
        template: '<div><slot v-for="row in data" :row="row"></slot></div>',
        props: ['data'],
      },
      'b-table-column': {
        name: 'b-table-column',
        template: '<div><slot></slot></div>',
        props: ['field', 'label'],
      },
    },
    ...options,
  });

  describe('Rendering', () => {
    it('should render b-table with correct props', () => {
      const parameters = [
        {parameterName: 'test', type: 'STRING', value: 'test value'},
      ];
      const wrapper = createWrapper({parameters});

      const table = wrapper.findComponent({name: 'b-table'});
      expect(table.exists()).toBe(true);
      expect(table.props('data')).toEqual(parameters);
    });
  });

  describe('Parameter display', () => {
    it('should display STRING type parameters', () => {
      const parameters = [
        {parameterName: 'testParameter', type: 'STRING', value: 'test value'},
      ];
      const wrapper = createWrapper({parameters});

      expect(wrapper.text()).toContain(parameters[0].parameterName);
      expect(wrapper.text()).toContain(parameters[0].type);
      expect(wrapper.text()).toContain(parameters[0].value);
    });

    it('should display NUMBER type parameters', () => {
      const parameters = [
        {parameterName: 'numParameter', type: 'NUMBER', value: 42.0},
      ];
      const wrapper = createWrapper({parameters});

      expect(wrapper.text()).toContain(parameters[0].parameterName);
      expect(wrapper.text()).toContain(parameters[0].type);
      expect(wrapper.text()).toContain(String(parameters[0].value));
    });

    it('should show download button for FILE type', () => {
      const parameters = [
        {parameterName: 'fileParameter', type: 'FILE', value: new Uint8Array([1, 2, 3])},
      ];
      const wrapper = createWrapper({parameters});

      const buttons = wrapper.findAll('button');
      expect(buttons.length).toBeGreaterThan(0);
    });

    it('should show download button for IMAGE type', () => {
      const parameters = [
        {parameterName: 'imageParameter', type: 'IMAGE', value: new Uint8Array([1, 2, 3])},
      ];
      const wrapper = createWrapper({parameters});

      const buttons = wrapper.findAll('button');
      expect(buttons.length).toBeGreaterThan(0);
    });

    it('should show download button for GEOMETRY type', () => {
      const parameters = [
        {parameterName: 'geoParameter', type: 'GEOMETRY', value: '{"type":"Point","coordinates":[0,0]}'},
      ];
      const wrapper = createWrapper({parameters});

      const buttons = wrapper.findAll('button');
      expect(buttons.length).toBeGreaterThan(0);
    });

    it('should display no buttons when type is not file, image, or geometry', () => {
      const parameters = [
        {parameterName: 'numParameter', type: 'NUMBER', value: 42},
      ];
      const wrapper = createWrapper({parameters});

      const buttons = wrapper.findAll('button');
      expect(buttons.length).toBe(0);
    });
  });
});
