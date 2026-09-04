<template>
<div>
  <v-popover
    placement="right"
    :popover-inner-class="'color-selector'"
    :open.sync="showColorSelector"
    :delay="0"
  >
    <div
      class="color-preview"
      :style="{background: formattedColor}"
      :class="{'is-selected': showColorSelector, 'is-clickable': editableColor}"
      @click="openColorSelector"
    ></div>
    <template #popover v-if="showColorSelector">
      <sketch-picker
        :value="formattedColor"
        @input="setColor"
        :presetColors="presetColors"
        :disable-alpha="true"
      />
    </template>

  </v-popover>
  <a
    role="button"
    @click.stop="$emit('click')"
  >
    {{formattedName}}
  </a>

</div>
</template>

<script>
import _ from 'lodash';

import { Sketch } from 'vue-color';

export default {
  name: 'cytomine-channel',
  components: {
    'sketch-picker': Sketch,
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
      this.$emit('setColor', color.hex);
    }, 500, { leading: true }),
  }
};
</script>
<style>
.color-selector {
  padding: 0 !important;
}

.color-selector .vc-sketch {
  width: 180px;
}

.color-selector .vc-sketch-presets-color {
  width: 1em;
  height: 1em;
  margin: 0 0.25em 0.25em 0;
}

.color-selector .vc-sketch-presets-color:first-child {
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
