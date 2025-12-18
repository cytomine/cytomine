<template>
  <div class="container">
    <b-field class="field" label-position="on-border" expanded>
      <template #label>
        <span class="text-label">
          {{ parameter.display_name }}
        </span>
      </template>

      <b-button @click="selectImage = true">
        <i class="fas fa-image" />
      </b-button>

      <div class="value-container" v-if="value">
        Image {{ value }}
      </div>
    </b-field>

    <ImageSelection
      :active.sync="selectImage"
      :title="$t('select-target-image-for-output-geometry')"
      @select-image="onSelectImage"
    />
  </div>
</template>

<script>
import ImageSelection from '@/components/image/ImageSelection';

export default {
  name: 'OutputGeometryField',
  props: {
    parameter: {type: Object, required: true},
    value: {},
  },
  data() {
    return {
      selectImage: false,
    };
  },
  components: {
    ImageSelection,
  },
  methods: {
    onSelectImage(imageId) {
      this.$emit('input', imageId);
      this.selectImage = false;
    },
  },
};
</script>

<style scoped>
.container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.field {
  margin-left: auto;
  margin-right: auto;
}

.text-label {
  display: block;
  width: 100%;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.value-container {
  margin-top: 5px;
  margin-left: 10px;
}
</style>
