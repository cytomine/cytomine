<template>
<form @submit.prevent="setMagnification()">
  <cytomine-modal :active="active" :title="$t('set-magnification')" @close="$emit('update:active', false)">
    <b-message type="is-warning" has-icon icon-size="is-small">
      {{ $t('warning-change-applies-in-project-only') }}
    </b-message>

    <field :form="form" name="magnification" :validators="magnificationRules" v-slot="{field, state}">
      <b-field :label="$t('magnification')" :type="{'is-danger': !!state.meta.errors.length}" :message="state.meta.errors[0]">
        <b-input :model-value="state.value" @update:model-value="field.handleChange" />
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

import { decimal, positive, rules, validateForm } from '@/utils/form.js';
import CytomineModal from '@/components/utils/CytomineModal.vue';

export default {
  name: 'magnification-modal',
  props: {
    active: { type: Boolean },
    image: { type: Object }
  },
  components: { CytomineModal, Field },
  setup() {
    const form = useForm({ defaultValues: { magnification: '' } });
    return {
      form,
      isValid: form.useStore(state => state.isValid),
      magnificationRules: { onChange: rules(decimal, positive) }
    };
  },
  computed: {
    blindMode() {
      return this.$store.state.currentProject.project.blindMode;
    }
  },
  watch: {
    active(val) {
      if (val) {
        this.form.reset({ magnification: this.image.magnification });
      }
    }
  },
  methods: {
    async setMagnification() {
      if (!await validateForm(this.form)) {
        return;
      }

      let imageName = this.blindMode ? this.image.blindedName : this.image.instanceFilename;
      try {
        let updateImage = this.image.clone();
        updateImage.magnification = this.form.state.values.magnification;
        await updateImage.save();

        this.$emit('setMagnification', updateImage.magnification);

        this.$notify({
          type: 'success',
          text: this.$t('notif-success-magnification-update', { imageName })
        });
      } catch (error) {
        console.log(error);
        this.$notify({
          type: 'error',
          text: this.$t('notif-error-magnification-update', { imageName })
        });
      }
      this.$emit('update:active', false);
    },
  }
};
</script>

