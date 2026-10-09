<template>
  <div
    class="cytomine-slider"
    :class="[size, { 'has-tooltip-margin': tooltip && isArray && !smartTooltipPosition }]"
  >
    <div class="cytomine-slider-rail" @pointerdown="onRailDown">
      <div class="cytomine-slider-track" ref="track" :style="trackStyle">
      <div class="cytomine-slider-process" :style="processStyle"></div>

      <div
        v-for="(value, index) in valueArray"
        :key="index"
        class="cytomine-slider-dot"
        :class="{ 'is-dragging': dragging && dragIndex === index }"
        :style="dotStyle(index)"
        tabindex="0"
        role="slider"
        :aria-valuemin="min"
        :aria-valuemax="max"
        :aria-valuenow="value"
        @keydown="onKey(index, $event)"
      >
        <div class="cytomine-slider-handle"></div>

        <div
          v-if="tooltip"
          class="cytomine-slider-tooltip"
          :class="[`is-${tooltipPlacement[index]}`, size]"
          @pointerdown.stop
          @mousedown.stop
          @click.stop="startEdition(index)"
          @keyup.enter="stopEdition(index)"
        >
          <template v-if="indexEdited !== index">
            <slot name="default" :value="value">
              {{ Math.round(value * 1000) / 1000 }}
            </slot>
          </template>
          <b-input
            v-else
            type="text"
            v-model="editedValue"
            @vue:mounted="focus()"
            @blur="stopEdition(index)"
            :size="size"
          />
        </div>
      </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'cytomine-slider',
  emits: ['update:modelValue'],
  props: {
    modelValue: { type: null },
    min: { type: Number, default: 0 },
    max: { type: Number, default: 100 },
    interval: { type: Number },
    integerOnly: { type: Boolean, default: true },
    lazy: { type: Boolean, default: true },
    tooltip: { type: Boolean, default: true },
    size: { type: String, default: 'is-normal' },
    contained: { type: Boolean, default: false },
    smartTooltipPosition: { type: Boolean, default: false }
  },
  data() {
    return {
      indexEdited: null,
      editedValue: 0,
      internalValue: null,
      dragging: false,
      dragIndex: null,
    };
  },
  computed: {
    isArray() {
      return Array.isArray(this.modelValue);
    },
    step() {
      return this.interval || 1;
    },
    range() {
      return this.max - this.min;
    },
    middle() {
      return this.range / 2;
    },
    valueArray() {
      const value = (this.internalValue === null) ? this.modelValue : this.internalValue;
      const values = Array.isArray(value) ? value : [value];
      return values.map((v, i) => (typeof v === 'number' && !isNaN(v)) ? v : (i === 0 ? this.min : this.max));
    },
    processStyle() {
      const positions = this.valueArray.map(value => this.percent(value));
      if (this.isArray) {
        const low = Math.min(positions[0], positions[1]);
        const high = Math.max(positions[0], positions[1]);
        return { left: `${low}%`, width: `${high - low}%` };
      }
      return { left: '0%', width: `${positions[0]}%` };
    },
    tooltipPlacement() {
      if (this.isArray) {
        if (this.smartTooltipPosition) {
          const n = this.modelValue.length + 1;
          const values = (Array.isArray(this.internalValue)) ? this.internalValue : this.modelValue;
          return values.map((v, i) => (v >= this.range * (i + 1) / n) ? 'left' : 'right');
        }
        return this.modelValue.map((_, i) => (i === 0) ? 'left' : 'right');
      }

      const value = (this.internalValue === null) ? this.modelValue : this.internalValue;
      if (value >= this.middle) {
        return ['left'];
      }

      return ['right'];
    },
    dotSize() {
      switch (this.size) {
        case 'is-small':
          return 10.5;
        default:
          return 14;
      }
    },
    trackStyle() {
      if (!this.contained) {
        return null;
      }
      return { marginLeft: `${this.dotSize / 2}px`, marginRight: `${this.dotSize / 2}px` };
    }
  },
  watch: {
    modelValue() {
      this.internalValue = this.clone(this.modelValue);
    }
  },
  methods: {
    clone(value) {
      return Array.isArray(value) ? value.slice() : value;
    },
    percent(value) {
      return (this.range === 0) ? 0 : (value - this.min) / this.range * 100;
    },
    dotStyle(index) {
      return {
        left: `${this.percent(this.valueArray[index])}%`,
        width: `${this.dotSize}px`,
        height: `${this.dotSize}px`,
      };
    },
    snap(raw) {
      const clamped = Math.min(this.max, Math.max(this.min, raw));
      const steps = Math.round((clamped - this.min) / this.step);
      const decimals = (String(this.step).split('.')[1] || '').length;
      const value = Number((this.min + steps * this.step).toFixed(decimals));
      return Math.min(this.max, Math.max(this.min, value));
    },
    valueFromEvent(event) {
      const rect = this.$refs.track.getBoundingClientRect();
      const ratio = (rect.width <= 0) ? 0 : (event.clientX - rect.left) / rect.width;
      return this.snap(this.min + Math.min(1, Math.max(0, ratio)) * this.range);
    },
    nearestIndex(value) {
      if (!this.isArray) {
        return 0;
      }
      const [a, b] = this.valueArray;
      return Math.abs(a - value) <= Math.abs(b - value) ? 0 : 1;
    },
    setDotValue(index, value) {
      if (!this.isArray) {
        this.internalValue = value;
        return;
      }
      const values = this.valueArray.slice();
      // Prevent the two thumbs from crossing (equivalent to enable-cross: false).
      values[index] = (index === 0) ? Math.min(value, values[1]) : Math.max(value, values[0]);
      this.internalValue = values;
    },
    commit() {
      this.$emit('update:modelValue', this.clone(this.internalValue));
    },
    onRailDown(event) {
      if (this.indexEdited !== null) {
        return;
      }
      event.preventDefault();
      const value = this.valueFromEvent(event);
      this.dragIndex = this.nearestIndex(value);
      this.dragging = true;
      this.setDotValue(this.dragIndex, value);
      if (!this.lazy) {
        this.commit();
      }
      window.addEventListener('pointermove', this.onDrag);
      window.addEventListener('pointerup', this.endDrag);
    },
    onDrag(event) {
      this.setDotValue(this.dragIndex, this.valueFromEvent(event));
      if (!this.lazy) {
        this.commit();
      }
    },
    endDrag() {
      window.removeEventListener('pointermove', this.onDrag);
      window.removeEventListener('pointerup', this.endDrag);
      this.dragging = false;
      this.commit();
      this.dragIndex = null;
    },
    onKey(index, event) {
      let value;
      const current = this.valueArray[index];
      switch (event.key) {
        case 'ArrowLeft':
        case 'ArrowDown':
          value = current - this.step;
          break;
        case 'ArrowRight':
        case 'ArrowUp':
          value = current + this.step;
          break;
        case 'Home':
          value = this.min;
          break;
        case 'End':
          value = this.max;
          break;
        default:
          return;
      }
      event.preventDefault();
      this.setDotValue(index, this.snap(value));
      this.commit();
    },
    startEdition(index) {
      if (this.indexEdited !== index) {
        this.editedValue = this.isArray ? this.modelValue[index] : this.modelValue;
        this.indexEdited = index;
      }
    },
    stopEdition(index = 0) {
      if (this.indexEdited === index) {
        this.indexEdited = null;

        if (!this.editedValue || isNaN(this.editedValue)) {
          return; // if entered value is not a number, ignore
        }

        let parsedValue = this.integerOnly ? parseInt(this.editedValue) : Number(this.editedValue);
        parsedValue = Math.min(parsedValue, this.max);
        parsedValue = Math.max(parsedValue, this.min);

        if (this.isArray) {
          let newVal = this.modelValue.slice();
          newVal[index] = parsedValue;
          if (newVal[0] > newVal[1]) { // reorder bounds if needed
            newVal.reverse();
          }
          this.$emit('update:modelValue', newVal);
        } else {
          this.$emit('update:modelValue', parsedValue);
        }
      }
    },
    focus() {
      this.$nextTick(() => {
        const input = this.$el.querySelector('.cytomine-slider-tooltip input');
        if (input) {
          input.focus();
        }
      });
    }
  },
  created() {
    this.internalValue = this.clone(this.modelValue);
  },
  beforeUnmount() {
    window.removeEventListener('pointermove', this.onDrag);
    window.removeEventListener('pointerup', this.endDrag);
  }
};
</script>

<style lang="scss">
.cytomine-slider {
  position: relative;
  padding: 0.5rem 0;
  user-select: none;
  touch-action: none;

  &.has-tooltip-margin {
    margin-left: 4rem;
    margin-right: 6rem;
  }
}

.cytomine-slider-rail {
  position: relative;
  height: 6px;
  background: #dbdbdb;
  border-radius: 3px;
  cursor: pointer;

  .is-small & {
    height: 4px;
  }
}

.cytomine-slider-track {
  position: relative;
  height: 100%;
}

.cytomine-slider-process {
  position: absolute;
  top: 0;
  height: 100%;
  background: #3273dc;
  border-radius: inherit;
}

.cytomine-slider-dot {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  z-index: 3;
  outline: none;
}

.cytomine-slider-handle {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0.5px 0.5px 2px 1px rgba(0, 0, 0, 0.32);
  cursor: grab;

  .cytomine-slider-dot:focus & {
    box-shadow: 0 0 0 2px rgba(50, 115, 220, 0.35), 0.5px 0.5px 2px 1px rgba(0, 0, 0, 0.32);
  }

  .cytomine-slider-dot.is-dragging & {
    cursor: grabbing;
  }
}

.cytomine-slider-tooltip {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  padding: 0.15rem 0.45rem;
  background: #fff;
  border-radius: 5px;
  box-shadow: 0 0 3px rgba(10, 10, 10, 0.1), 0 0 0 1px rgba(10, 10, 10, 0.1);
  font-size: 0.9rem;
  line-height: 1.2;
  white-space: nowrap;
  cursor: text;
  z-index: 20;

  // Label sits beside the thumb (not above it); the side follows the placement.
  &.is-right {
    left: 100%;
    margin-left: 8px;
  }

  &.is-left {
    right: 100%;
    margin-right: 8px;
  }

  .control {
    font-size: inherit;
  }

  input {
    width: 4rem;
    height: 1.5rem;
    font-size: 0.8rem;
  }

  &.is-small {
    font-size: 0.7rem;
    padding: 1px 3px;
    min-width: 10px;
    border-radius: 3px;

    input {
      width: 2rem;
      height: 1rem;
      font-size: 0.6rem;
    }
  }
}
</style>
