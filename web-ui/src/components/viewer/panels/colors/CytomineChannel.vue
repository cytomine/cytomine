<template>
<div>
  <VDropdown
    placement="right"
    popper-class="color-selector"
    v-model:shown="showColorSelector"
    :delay="0"
  >
    <div
      class="color-preview"
      :style="{background: formattedColor}"
      :class="{'is-selected': showColorSelector, 'is-clickable': editableColor}"
      @click="openColorSelector"
    ></div>
    <template #popper v-if="showColorSelector">
      <sketch-picker
        :model-value="formattedColor"
        @update:model-value="setColor"
        :presetColors="presetColors"
        :disable-alpha="true"
      />
    </template>

  </VDropdown>
  <a role="button" @click.stop="$emit('click')">
    {{formattedName}}
  </a>
</div>
</template>

<script>
import _ from 'lodash';

import { SketchPicker, tinycolor } from 'vue-color';

export default {
  name: 'cytomine-channel',
  components: {
    SketchPicker,
  },
  props: {
    name: String,
    color: String,
    defaultColor: String,

    channelIndex: Number,
    sampleIndex: Number,
    nbSamplesPerChannel: { type: Number, default: 1 },

    editableColor: { type: Boolean, default: false },
  },
  data() {
    return {
      showColorSelector: false
    };
  },
  computed: {
    rgbColors() {
      return [this.$t('red'), this.$t('green'), this.$t('blue')];
    },
    formattedName() {
      let name = this.name;
      if (name === null) {
        name = `${this.$t('channel-abbr')} ${this.channelIndex + 1}`;
      }
      if (this.nbSamplesPerChannel === 3) {
        name += ` (${this.rgbColors[this.sampleIndex]})`;
      }
      return name;
    },
    formattedColor() {
      if (this.color === null) {
        return '#ffffff';
      }
      return this.color;
    },
    presetColors() {
      let defaultColor = (this.defaultColor === null) ? '#ffffff' : this.defaultColor;
      return [
        defaultColor,
        '#FF0000',
        '#00FF00',
        '#0000FF',
        '#00FFFF',
        '#FFFF00',
        '#FF00FF',
        '#FFF'
      ];
    }
  },
  methods: {
    openColorSelector() {
      if (this.editableColor) {
        this.showColorSelector = !this.showColorSelector;
      }
    },
    setColor: _.debounce(function (color) {
      this.$emit('setColor', tinycolor(color).toHexString());
    }, 500, { leading: true }),
  }
};
</script>
<style>
.color-selector {
  padding: 0 !important;
}

.color-selector .vc-sketch-picker {
  width: 180px;
}

.color-selector .preset-color {
  width: 1em;
  height: 1em;
  margin: 0 0.25em 0.25em 0;
}

.color-selector .preset-color:first-child {
  width: 2.25em;
}
</style>
<style scoped>
.is-clickable {
  cursor: pointer !important;
}

.color-preview {
  width: 1em;
  height: 1em;
  display: inline-block;
  margin-right: 0.25em;
  border-radius: 0.25em;
  box-shadow: 0 0 1px #777;
  position: relative;
  top: 0.2em;
}

a[role="button"] {
  color: inherit;
}
</style>
