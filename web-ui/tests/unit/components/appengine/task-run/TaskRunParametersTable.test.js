import {shallowMount} from '@vue/test-utils';

import TaskRunParametersTable from '@/components/appengine/task-run/TaskRunParametersTable.vue';

describe('TaskRunParametersTable.vue', () => {
  const createWrapper = (options = {}) => shallowMount(TaskRunParametersTable, {
    propsData: {
      parameters: [],
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
        // eslint-disable-next-line camelcase
        {param_name: 'test', type: 'STRING', value: 'test value'},
      ];
      const wrapper = createWrapper({propsData: {parameters}});

      const table = wrapper.findComponent({name: 'b-table'});
      expect(table.exists()).toBe(true);
      expect(table.props('data')).toEqual(parameters);
    });
  });

  describe('Parameter display', () => {
    it('should display STRING type parameters', () => {
      const parameters = [
        // eslint-disable-next-line camelcase
        {param_name: 'testParameter', type: 'STRING', value: 'test value'},
      ];
      const wrapper = createWrapper({propsData: {parameters}});

      expect(wrapper.text()).toContain(parameters[0].param_name);
      expect(wrapper.text()).toContain(parameters[0].type);
      expect(wrapper.text()).toContain(parameters[0].value);
    });

    it('should display NUMBER type parameters', () => {
      const parameters = [
        // eslint-disable-next-line camelcase
        {param_name: 'numParameter', type: 'NUMBER', value: 42.0},
      ];
      const wrapper = createWrapper({propsData: {parameters}});

      expect(wrapper.text()).toContain(parameters[0].param_name);
      expect(wrapper.text()).toContain(parameters[0].type);
      expect(wrapper.text()).toContain(String(parameters[0].value));
    });

    it('should show download button for FILE type', () => {
      const parameters = [
        // eslint-disable-next-line camelcase
        {param_name: 'fileParameter', type: 'FILE', value: new Uint8Array([1, 2, 3])},
      ];
      const wrapper = createWrapper({propsData: {parameters}});

      const buttons = wrapper.findAll('button');
      expect(buttons.length).toBeGreaterThan(0);
    });

    it('should show download button for IMAGE type', () => {
      const parameters = [
        // eslint-disable-next-line camelcase
        {param_name: 'imageParameter', type: 'IMAGE', value: new Uint8Array([1, 2, 3])},
      ];
      const wrapper = createWrapper({propsData: {parameters}});

      const buttons = wrapper.findAll('button');
      expect(buttons.length).toBeGreaterThan(0);
    });

    it('should show download button for GEOMETRY type', () => {
      const parameters = [
        {
          // eslint-disable-next-line camelcase
          param_name: 'geoParameter',
          type: 'GEOMETRY',
          value: '{"type":"Point","coordinates":[0,0]}',
        },
      ];
      const wrapper = createWrapper({propsData: {parameters}});

      const buttons = wrapper.findAll('button');
      expect(buttons.length).toBeGreaterThan(0);
    });

    it('should display no buttons when type is not file, image, or geometry', () => {
      const parameters = [
        // eslint-disable-next-line camelcase
        {param_name: 'numParameter', type: 'NUMBER', value: 42},
      ];
      const wrapper = createWrapper({propsData: {parameters}});

      const buttons = wrapper.findAll('button');
      expect(buttons.length).toBe(0);
    });
  });
});
