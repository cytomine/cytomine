import { nextTick } from 'vue';
import { mount } from '@vue/test-utils';
import Buefy from 'buefy';

import TaskRunParametersTable from '@/components/appengine/task-run/TaskRunParametersTable.vue';

describe('TaskRunParametersTable.vue', () => {
  // Mounted against the real Buefy rather than stubs: the stubs this test used
  // to carry modelled Buefy 0.8's table contract (the table calling its default
  // slot once per row), which Buefy 3 no longer has.
  const createWrapper = async (options = {}) => {
    const wrapper = mount(TaskRunParametersTable, {
      props: {
        parameters: options.parameters || [],
        projectId: 42,
        type: 'input',
      },
      ...options,
      global: {
        plugins: [Buefy],
        mocks: {
          $t: (key) => key,
        },
      }
    });

    // Buefy 3 discovers the columns while rendering the table's default slot,
    // so the rows only exist on the render after that.
    await nextTick();

    return wrapper;
  };

  describe('Rendering', () => {
    it('should render b-table with correct props', async () => {
      const parameters = [
        { parameterName: 'test', type: 'STRING', value: 'test value' },
      ];
      const wrapper = await createWrapper({ parameters });

      const table = wrapper.findComponent({ name: 'b-table' });
      expect(table.exists()).toBe(true);
      expect(table.props('data')).toEqual(parameters);
    });
  });

  describe('Parameter display', () => {
    it('should display STRING type parameters', async () => {
      const parameters = [
        { parameterName: 'testParameter', type: 'STRING', value: 'test value' },
      ];
      const wrapper = await createWrapper({ parameters });

      expect(wrapper.text()).toContain(parameters[0].parameterName);
      expect(wrapper.text()).toContain(parameters[0].type);
      expect(wrapper.text()).toContain(parameters[0].value);
    });

    it('should display NUMBER type parameters', async () => {
      const parameters = [
        { parameterName: 'numParameter', type: 'NUMBER', value: 42.0 },
      ];
      const wrapper = await createWrapper({ parameters });

      expect(wrapper.text()).toContain(parameters[0].parameterName);
      expect(wrapper.text()).toContain(parameters[0].type);
      expect(wrapper.text()).toContain(String(parameters[0].value));
    });

    it('should show download button for FILE type', async () => {
      const parameters = [
        { parameterName: 'fileParameter', type: 'FILE', value: new Uint8Array([1, 2, 3]) },
      ];
      const wrapper = await createWrapper({ parameters });

      const buttons = wrapper.findAll('button');
      expect(buttons.length).toBeGreaterThan(0);
    });

    it('should show download button for IMAGE type', async () => {
      const parameters = [
        { parameterName: 'imageParameter', type: 'IMAGE', value: new Uint8Array([1, 2, 3]) },
      ];
      const wrapper = await createWrapper({ parameters });

      const buttons = wrapper.findAll('button');
      expect(buttons.length).toBeGreaterThan(0);
    });

    it('should show download button for GEOMETRY type', async () => {
      const parameters = [
        { parameterName: 'geoParameter', type: 'GEOMETRY', value: '{"type":"Point","coordinates":[0,0]}' },
      ];
      const wrapper = await createWrapper({ parameters });

      const buttons = wrapper.findAll('button');
      expect(buttons.length).toBeGreaterThan(0);
    });

    it('should display no buttons when type is not file, image, or geometry', async () => {
      const parameters = [
        { parameterName: 'numParameter', type: 'NUMBER', value: 42 },
      ];
      const wrapper = await createWrapper({ parameters });

      const buttons = wrapper.findAll('button');
      expect(buttons.length).toBe(0);
    });
  });
});
