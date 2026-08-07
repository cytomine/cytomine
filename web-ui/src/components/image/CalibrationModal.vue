<template>
<form @submit.prevent="setResolution()">
  <cytomine-modal :active="active" :title="$t('calibrate-image')" @close="$emit('update:active', false)">
    <b-message type="is-warning" has-icon icon-size="is-small">
      {{ $t('warning-change-applies-in-project-only') }}
    </b-message>

    <field :form="form" name="resolution" :validators="resolutionRules" v-slot="{field, state}">
      <b-field :label="$t('resolution')" :type="{'is-danger': !!state.meta.errors.length}" :message="state.meta.errors[0]">
        <b-field :type="{'is-danger': !!state.meta.errors.length}">
          <b-input :model-value="state.value" @update:model-value="field.handleChange" expanded />
          <b-select v-model="calibrationFactorX">
            <option :value="0.001"> {{ $t('nm-per-pixel') }}</option>
            <option :value="1">{{ $t('um-per-pixel') }}</option>
            <option :value="1000">{{ $t('mm-per-pixel') }}</option>
          </b-select>
        </b-field>
      </b-field>
    </field>

    <field v-if="image.depth > 1" :form="form" name="resolution-z" :validators="resolutionRules" v-slot="{field, state}">
      <b-field :label="$t('z-resolution')"
               :type="{'is-danger': !!state.meta.errors.length}" :message="state.meta.errors[0]">
        <b-field :type="{'is-danger': !!state.meta.errors.length}">
          <b-input :model-value="state.value" @update:model-value="field.handleChange" expanded />
          <b-select v-model="calibrationFactorZ">
            <option :value="0.001"> {{ $t('nm-per-slice') }}</option>
            <option :value="1">{{ $t('um-per-slice') }}</option>
            <option :value="1000">{{ $t('mm-per-slice') }}</option>
          </b-select>
        </b-field>
      </b-field>
    </field>

    <field v-if="image.duration > 1" :form="form" name="resolution-t" :validators="resolutionRules" v-slot="{field, state}">
      <b-field :label="$t('frame-rate')"
               :type="{'is-danger': !!state.meta.errors.length}" :message="state.meta.errors[0]">
        <b-field :type="{'is-danger': !!state.meta.errors.length}">
          <b-input :model-value="state.value" @update:model-value="field.handleChange" expanded />
          <p class="control">
            <span class="button is-static">{{$t('frame-per-second')}}</span>
          </p>
        </b-field>
      </b-field>
    </field>

    <template #footer>
      <button class="button" type="button" @click="$emit('update:active', false)">
        {{$t('button-cancel')}}
      </button>
      <button class="button is-link" :disabled="!isValid">
        {{$t('button-save')}}
      </button>
    </template>
  </cytomine-modal>
</form>
</template>

<script>
import { Field, useForm } from '@tanstack/vue-form';

import { decimal, positive, required, rules, validateForm } from '@/utils/form.js';
import CytomineModal from '@/components/utils/CytomineModal.vue';

export default {
  name: 'calibration-modal',
  props: {
    active: { type: Boolean },
    image: { type: Object }
  },
  components: { CytomineModal, Field },
  setup() {
    const form = useForm({
      defaultValues: { 'resolution': '', 'resolution-z': '', 'resolution-t': '' }
    });
    return {
      form,
      isValid: form.useStore(state => state.isValid),
      resolutionRules: { onChange: rules(required, decimal, positive) }
    };
  },
  data() {
    return {
      calibrationFactorX: 1,
      calibrationFactorZ: 1
    };
  },
  computed: {
    blindMode() {
      return this.$store.state.currentProject.project.blindMode;
    },
  },
  watch: {
    active(val) {
      if (val) {
        this.form.reset({
          'resolution': this.image.physicalSizeX,
          'resolution-z': this.image.physicalSizeZ,
          'resolution-t': this.image.fps
        });
      }
    }
  },
  methods: {
    async setResolution() {
      if (!await validateForm(this.form)) {
        return;
      }

      let values = this.form.state.values;
      let imageName = this.blindMode ? this.image.blindedName : this.image.instanceFilename;
      try {
        let updateImage = this.image.clone();
        updateImage.physicalSizeX = Number(values['resolution']) * this.calibrationFactorX;
        updateImage.physicalSizeY = Number(values['resolution']) * this.calibrationFactorX;
        if (this.image.depth > 1) {
          updateImage.physicalSizeZ = Number(values['resolution-z']) * this.calibrationFactorZ;
        }
        if (this.image.duration > 1) {
          updateImage.fps = Number(values['resolution-t']);
        }
        await updateImage.save();

        this.$emit('setResolution', {
          x: updateImage.physicalSizeX,
          y: updateImage.physicalSizeY,
          z: updateImage.physicalSizeZ,
          t: updateImage.fps
        });

        this.$notify({
          type: 'success',
          text: this.$t('notif-success-image-calibration', { imageName })
        });
      } catch (error) {
        console.log(error);
        this.$notify({
          type: 'error',
          text: this.$t('notif-error-image-calibration', { imageName })
        });
      }
      this.$emit('update:active', false);
    }
  }
};
</script>
