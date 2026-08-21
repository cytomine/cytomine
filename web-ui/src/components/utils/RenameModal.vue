<template>
<form @submit.prevent="form.handleSubmit()">
  <cytomine-modal :title="title" :active="active" @close="close()">
    <Field :form="form" name="name" :validators="{ onChange: requiredValidator }" v-slot="{ field }">
      <b-field
        :label="$t('new-name')"
        :type="{'is-danger': field.state.meta.errors.length > 0}"
        :message="field.state.meta.errors[0]"
      >
        <b-input
          name="name"
          :model-value="field.state.value"
          @update:model-value="field.handleChange"
          @blur="field.handleBlur"
        />
      </b-field>
    </Field>
    <template #footer>
      <button class="button" type="button" @click="close()">
        {{$t('button-cancel')}}
      </button>
      <button class="button is-link" :disabled="!canSubmit">
        {{$t('button-save')}}
      </button>
    </template>
  </cytomine-modal>
</form>
</template>

<script>
import { Field, useForm } from '@tanstack/vue-form';

import CytomineModal from './CytomineModal.vue';

export default {
  name: 'rename-modal',
  components: { CytomineModal, Field },
  props: {
    active: Boolean,
    currentName: String,
    title: String
  },
  setup(props, { emit }) {
    const form = useForm({
      defaultValues: { name: props.currentName ?? '' },
      onSubmit: ({ value }) => {
        emit('rename', value.name);
        emit('update:active', false);
      }
    });
    const canSubmit = form.useStore(state => state.canSubmit);
    return { form, canSubmit };
  },
  watch: {
    active(active) {
      if (active) {
        this.form.setFieldValue('name', this.currentName);
      }
    }
  },
  methods: {
    requiredValidator({ value }) {
      return value && String(value).trim().length > 0 ? undefined : 'This field is required';
    },
    close() {
      this.$emit('close');
      this.$emit('update:active', false);
    }
  }
};
</script>

<style scoped>
:deep(input[type=text]) {
  width: 26em;
}
</style>
