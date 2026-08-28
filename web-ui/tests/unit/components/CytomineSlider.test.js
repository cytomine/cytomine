import { shallowMount } from '@vue/test-utils';

import CytomineSlider from '@/components/form/CytomineSlider';

const MIN = 0;
const MAX = 100;

describe('CytomineSlider.vue', () => {
  const createWrapper = (propsData = {}) => shallowMount(CytomineSlider, {
    propsData: { min: MIN, max: MAX, ...propsData },
    stubs: {
      'b-input': true
    }
  });

  const dots = wrapper => wrapper.findAll('.cytomine-slider-dot');
  const tooltips = wrapper => wrapper.findAll('.cytomine-slider-tooltip');

  describe('rendering', () => {
    it('should render a single thumb for a scalar value', () => {
      const wrapper = createWrapper({ modelValue: 30 });

      expect(dots(wrapper).length).toBe(1);
      expect(tooltips(wrapper).at(0).text()).toBe('30');
    });

    it('should render two thumbs for an array value (range)', () => {
      const wrapper = createWrapper({ modelValue: [10, 80] });

      expect(dots(wrapper).length).toBe(2);
      expect(tooltips(wrapper).at(0).text()).toBe('10');
      expect(tooltips(wrapper).at(1).text()).toBe('80');
    });

    it('should expose accessibility attributes on each thumb', () => {
      const wrapper = createWrapper({ modelValue: [10, 80] });
      const dot = dots(wrapper).at(1);

      expect(dot.attributes('role')).toBe('slider');
      expect(dot.attributes('aria-valuemin')).toBe(String(MIN));
      expect(dot.attributes('aria-valuemax')).toBe(String(MAX));
      expect(dot.attributes('aria-valuenow')).toBe('80');
    });

    it('should not render tooltips when tooltip is disabled', () => {
      const wrapper = createWrapper({ modelValue: 30, tooltip: false });

      expect(tooltips(wrapper).length).toBe(0);
    });
  });

  describe('missing / invalid values (no NaN)', () => {
    it('should fall back to min for a missing scalar value', () => {
      const wrapper = createWrapper({ modelValue: undefined });

      expect(dots(wrapper).length).toBe(1);
      expect(tooltips(wrapper).at(0).text()).toBe(String(MIN));
      expect(wrapper.vm.valueArray).toEqual([MIN]);
    });

    it('should fill the upper bound with max when it is not yet loaded', () => {
      const wrapper = createWrapper({ modelValue: [MIN, undefined] });

      expect(dots(wrapper).length).toBe(2);
      expect(wrapper.vm.valueArray).toEqual([MIN, MAX]);
      expect(tooltips(wrapper).at(1).text()).toBe(String(MAX));
    });

    it('should default an empty range to [min, max]', () => {
      const min = 5;
      const max = 42;
      const wrapper = createWrapper({ modelValue: [undefined, undefined], min, max });

      expect(wrapper.vm.valueArray).toEqual([min, max]);
    });

    it('should never emit NaN positions in the process style', () => {
      const wrapper = createWrapper({ modelValue: [MIN, undefined] });

      expect(wrapper.vm.processStyle.left).toBe('0%');
      expect(wrapper.vm.processStyle.width).toBe('100%');
    });
  });

  describe('computed geometry', () => {
    it('should compute the process bar for a range', () => {
      const wrapper = createWrapper({ modelValue: [20, 60] });

      expect(wrapper.vm.processStyle).toEqual({ left: '20%', width: '40%' });
    });

    it('should compute the process bar for a scalar value from the start', () => {
      const wrapper = createWrapper({ modelValue: 40 });

      expect(wrapper.vm.processStyle).toEqual({ left: '0%', width: '40%' });
    });

    it('should guard against a zero range', () => {
      const bound = 50;
      const wrapper = createWrapper({ modelValue: bound, min: bound, max: bound });

      expect(wrapper.vm.percent(bound)).toBe(0);
    });

    it('should place single-value tooltip on the side away from the thumb', () => {
      expect(createWrapper({ modelValue: 90 }).vm.tooltipPlacement).toEqual(['left']);
      expect(createWrapper({ modelValue: 10 }).vm.tooltipPlacement).toEqual(['right']);
    });

    it('should place range tooltips on opposite sides by default', () => {
      const wrapper = createWrapper({ modelValue: [10, 80] });

      expect(wrapper.vm.tooltipPlacement).toEqual(['left', 'right']);
    });
  });

  describe('keyboard interaction', () => {
    it('should increment a scalar value on ArrowRight and emit update:modelValue', async () => {
      const wrapper = createWrapper({ modelValue: 30 });

      await dots(wrapper).at(0).trigger('keydown', { key: 'ArrowRight' });

      expect(wrapper.emitted('update:modelValue')).toBeTruthy();
      expect(wrapper.emitted('update:modelValue')[0]).toEqual([31]);
    });

    it('should decrement on ArrowDown', async () => {
      const wrapper = createWrapper({ modelValue: 30 });

      await dots(wrapper).at(0).trigger('keydown', { key: 'ArrowDown' });

      expect(wrapper.emitted('update:modelValue')[0]).toEqual([29]);
    });

    it('should jump to min on Home and max on End', async () => {
      const wrapper = createWrapper({ modelValue: 30 });

      await dots(wrapper).at(0).trigger('keydown', { key: 'Home' });
      await dots(wrapper).at(0).trigger('keydown', { key: 'End' });

      const emitted = wrapper.emitted('update:modelValue');
      expect(emitted[0]).toEqual([MIN]);
      expect(emitted[1]).toEqual([MAX]);
    });

    it('should respect the interval as the keyboard step', async () => {
      const wrapper = createWrapper({ modelValue: 30, interval: 5 });

      await dots(wrapper).at(0).trigger('keydown', { key: 'ArrowRight' });

      expect(wrapper.emitted('update:modelValue')[0]).toEqual([35]);
    });

    it('should clamp at the maximum bound', async () => {
      const wrapper = createWrapper({ modelValue: MAX });

      await dots(wrapper).at(0).trigger('keydown', { key: 'ArrowRight' });

      expect(wrapper.emitted('update:modelValue')[0]).toEqual([MAX]);
    });

    it('should ignore unrelated keys', async () => {
      const wrapper = createWrapper({ modelValue: 30 });

      await dots(wrapper).at(0).trigger('keydown', { key: 'a' });

      expect(wrapper.emitted('update:modelValue')).toBeFalsy();
    });
  });

  describe('range: thumbs cannot cross', () => {
    it('should clamp the lower thumb to the upper value', () => {
      const wrapper = createWrapper({ modelValue: [40, 60] });

      wrapper.vm.setDotValue(0, 90);

      expect(wrapper.vm.internalValue).toEqual([60, 60]);
    });

    it('should clamp the upper thumb to the lower value', () => {
      const wrapper = createWrapper({ modelValue: [40, 60] });

      wrapper.vm.setDotValue(1, 10);

      expect(wrapper.vm.internalValue).toEqual([40, 40]);
    });
  });

  describe('inline edition', () => {
    it('should emit a clamped scalar value on stopEdition', () => {
      const wrapper = createWrapper({ modelValue: 30 });

      wrapper.vm.startEdition(0);
      wrapper.vm.editedValue = MAX + 400;
      wrapper.vm.stopEdition(0);

      expect(wrapper.emitted('update:modelValue')[0]).toEqual([MAX]);
    });

    it('should reorder bounds when the edited value crosses the other thumb', () => {
      const wrapper = createWrapper({ modelValue: [10, 80] });

      wrapper.vm.startEdition(0);
      wrapper.vm.editedValue = 90;
      wrapper.vm.stopEdition(0);

      expect(wrapper.emitted('update:modelValue')[0][0]).toEqual([80, 90]);
    });

    it('should parse to an integer when integerOnly is set', () => {
      const wrapper = createWrapper({ modelValue: 30, integerOnly: true });

      wrapper.vm.startEdition(0);
      wrapper.vm.editedValue = '42.7';
      wrapper.vm.stopEdition(0);

      expect(wrapper.emitted('update:modelValue')[0]).toEqual([42]);
    });

    it('should keep decimals when integerOnly is false', () => {
      const wrapper = createWrapper({ modelValue: 30, integerOnly: false });

      wrapper.vm.startEdition(0);
      wrapper.vm.editedValue = '42.7';
      wrapper.vm.stopEdition(0);

      expect(wrapper.emitted('update:modelValue')[0]).toEqual([42.7]);
    });

    it('should ignore a non-numeric edit', () => {
      const wrapper = createWrapper({ modelValue: 30 });

      wrapper.vm.startEdition(0);
      wrapper.vm.editedValue = 'abc';
      wrapper.vm.stopEdition(0);

      expect(wrapper.emitted('update:modelValue')).toBeFalsy();
    });
  });

  describe('snapping', () => {
    it('should snap to the nearest integer step', () => {
      const wrapper = createWrapper({ modelValue: 0 });

      expect(wrapper.vm.snap(2.4)).toBe(2);
      expect(wrapper.vm.snap(2.6)).toBe(3);
    });

    it('should snap to the nearest interval multiple', () => {
      const wrapper = createWrapper({ modelValue: 0, interval: 5 });

      expect(wrapper.vm.snap(12)).toBe(10);
      expect(wrapper.vm.snap(13)).toBe(15);
    });

    it('should clamp snapped values within [min, max]', () => {
      const min = 10;
      const max = 20;
      const wrapper = createWrapper({ modelValue: min, min, max });

      expect(wrapper.vm.snap(min - 15)).toBe(min);
      expect(wrapper.vm.snap(max + 979)).toBe(max);
    });
  });

  describe('v-model contract (Vue 3: modelValue / update:modelValue)', () => {
    it('should reset internalValue when the modelValue prop changes', async () => {
      const wrapper = createWrapper({ modelValue: [10, 80] });

      await wrapper.setProps({ modelValue: [20, 90] });

      expect(wrapper.vm.internalValue).toEqual([20, 90]);
    });

    it('should emit the "update:modelValue" event (not input)', async () => {
      const wrapper = createWrapper({ modelValue: 30 });

      await dots(wrapper).at(0).trigger('keydown', { key: 'ArrowRight' });

      expect(wrapper.emitted('update:modelValue')).toBeTruthy();
      expect(wrapper.emitted('input')).toBeFalsy();
    });
  });
});
