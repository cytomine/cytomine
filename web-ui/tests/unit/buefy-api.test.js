import { nextTick } from 'vue';
import { mount } from '@vue/test-utils';
import Buefy from 'buefy';

/**
 * The Buefy 0.8 -> 3.0 component API changes this migration relies on, checked
 * against the real library rather than assumed.
 *
 * Every assertion here has a matching rewrite in `src/`, and most also assert
 * that the *old* spelling is now inert -- that is what makes these regressions
 * detectable, since a prop Buefy no longer declares fails silently.
 */

const mountWith = (template, options = {}) => mount(
  {
    template,
    ...options,
  },
  {
    global: {
      plugins: [Buefy],
      mocks: { $t: (key) => key },
    },
    attachTo: document.body,
  },
);

describe('Buefy 3 component API', () => {
  describe('b-loading', () => {
    it('shows the overlay for :model-value, and ignores the removed :active', () => {
      const shown = mountWith('<b-loading :model-value="true" :is-full-page="false" />');
      expect(shown.find('.loading-overlay').exists()).toBe(true);

      const hidden = mountWith('<b-loading :model-value="false" :is-full-page="false" />');
      expect(hidden.find('.loading-overlay').exists()).toBe(false);

      // The Vue 2 spelling: no longer a declared prop, so the overlay stays down.
      const legacy = mountWith('<b-loading :active="true" :is-full-page="false" />');
      expect(legacy.find('.loading-overlay').exists()).toBe(false);
    });
  });

  describe('b-modal', () => {
    it('opens for :model-value, and ignores the removed :active', () => {
      const opened = mountWith('<b-modal :model-value="true"><p class="body">hello</p></b-modal>');
      expect(document.body.textContent).toContain('hello');
      opened.unmount();

      document.body.innerHTML = '';
      mountWith('<b-modal :active="true"><p class="body">hello</p></b-modal>');
      expect(document.body.textContent).not.toContain('hello');
      document.body.innerHTML = '';
    });
  });

  describe('b-collapse', () => {
    it('opens for :model-value, and ignores the removed :open', () => {
      const opened = mountWith('<b-collapse :model-value="true"><p>content</p></b-collapse>');
      expect(opened.find('.collapse-content').isVisible()).toBe(true);

      const closed = mountWith('<b-collapse :model-value="false"><p>content</p></b-collapse>');
      expect(closed.find('.collapse-content').isVisible()).toBe(false);

      // `:open="false"` is not a prop any more, so it falls back to the
      // modelValue default, which is `true`: the panel would be open.
      const legacy = mountWith('<b-collapse :open="false"><p>content</p></b-collapse>');
      expect(legacy.find('.collapse-content').isVisible()).toBe(true);
    });

    it('still passes {open} to the #trigger slot', async () => {
      const wrapper = mountWith(`
        <b-collapse :model-value="false">
          <template #trigger="{open}">
            <button class="trigger">{{ open ? 'hide' : 'show' }}</button>
          </template>
          <p>content</p>
        </b-collapse>
      `);

      expect(wrapper.find('.trigger').text()).toBe('show');
      await wrapper.find('.collapse-trigger').trigger('click');
      expect(wrapper.find('.trigger').text()).toBe('hide');
    });
  });

  describe('b-table', () => {
    const TABLE = `
      <b-table :data="rows">
        <b-table-column v-slot="{row}" label="Name" field="name">
          <span class="cell">{{ row.name }}</span>
        </b-table-column>
      </b-table>
    `;
    const rows = () => ({ data: () => ({ rows: [{ name: 'first' }, { name: 'second' }] }) });

    it('renders each column body once per row, through the column own scoped slot', async () => {
      const wrapper = mountWith(TABLE, rows());
      // The columns register while the table renders its default slot, so the
      // rows only exist on the render after that.
      await nextTick();

      expect(wrapper.findAll('.cell').map((c) => c.text())).toEqual(['first', 'second']);
      expect(wrapper.find('th').text()).toContain('Name');
    });

    it('gives the Vue 2 style column body no row, though the column itself still registers', async () => {
      const seen = [];
      const wrapper = mountWith(
        `
        <b-table :data="rows">
          <template #default="props">
            <b-table-column label="Name" field="name">
              <span class="cell">{{ record(props) }}</span>
            </b-table-column>
          </template>
        </b-table>
        `,
        {
          ...rows(),
          methods: {
            record(props) {
              seen.push(props && props.row);
              return '';
            },
          },
        },
      );
      await nextTick();

      // The column is created once, so the header still comes out right, which
      // is exactly why this failure is quiet: the table looks built.
      expect(wrapper.find('th').text()).toContain('Name');

      // But the cell body closes over the *table's* default slot scope, which
      // Buefy 3 never fills. Reading `props.row` there is what threw before
      // the rewrite; the cells render, empty.
      expect(seen).not.toHaveLength(0);
      expect(seen.every((row) => row === undefined)).toBe(true);
      expect(wrapper.findAll('.cell').every((c) => c.text() === '')).toBe(true);
    });
  });

  describe('the v-model contract, with @vue/compat in MODE 2', () => {
    // src/utils/buefy-compat.js stamps `compatConfig: {MODE: 3}` on every Buefy
    // component. Without it compat rewrites `modelValue` back to the Vue 2
    // `value` prop for every component in the tree, and `v-model` on a `<b-*>`
    // becomes a one-way binding that never updates.
    it('round-trips v-model on b-input', async () => {
      const wrapper = mountWith(
        '<b-input v-model="text" />',
        { data: () => ({ text: 'before' }) },
      );

      expect(wrapper.find('input').element.value).toBe('before');

      await wrapper.find('input').setValue('after');
      expect(wrapper.vm.text).toBe('after');
    });

    it('round-trips v-model on b-checkbox', async () => {
      const wrapper = mountWith(
        '<b-checkbox v-model="checked" />',
        { data: () => ({ checked: false }) },
      );

      await wrapper.find('input').setValue(true);
      expect(wrapper.vm.checked).toBe(true);
    });

    it('emits update:modelValue, not input, when the value changes', async () => {
      const events = { input: [], update: [] };
      const wrapper = mountWith(
        `<b-checkbox
          :model-value="false"
          @input="onInput"
          @update:model-value="onUpdate"
        />`,
        {
          methods: {
            onInput(payload) {
              events.input.push(payload);
            },
            onUpdate(payload) {
              events.update.push(payload);
            },
          },
        },
      );

      await wrapper.find('input').setValue(true);

      expect(events.update).toEqual([true]);
      // `@input` still fires, but it is the *native* DOM event falling through
      // to the inner control, not the value. That is why every `@input` on a
      // `<b-*>` in `src/` moved to `@update:model-value`.
      expect(events.input.every((payload) => payload instanceof Event)).toBe(true);
    });
  });

  describe('attribute fallthrough', () => {
    it('delivers @click on b-button to the rendered button', async () => {
      let clicks = 0;
      const wrapper = mountWith(
        '<b-button @click="onClick">go</b-button>',
        { methods: { onClick() {
          clicks += 1; 
        } } },
      );

      await wrapper.find('button').trigger('click');
      expect(clicks).toBe(1);
    });

    it('delivers undeclared attributes to the inner control of b-input', () => {
      const wrapper = mountWith('<b-input placeholder="search" disabled readonly />');
      const input = wrapper.find('input').element;

      expect(input.placeholder).toBe('search');
      expect(input.disabled).toBe(true);
      expect(input.readOnly).toBe(true);
    });

    it('delivers pagination-size through b-table to its pagination', async () => {
      const wrapper = mountWith(
        '<b-table :data="rows" paginated :per-page="1" pagination-size="is-small" />',
        { data: () => ({ rows: [{ name: 'a' }, { name: 'b' }] }) },
      );
      await nextTick();

      expect(wrapper.find('.pagination').classes()).toContain('is-small');
    });
  });
});
