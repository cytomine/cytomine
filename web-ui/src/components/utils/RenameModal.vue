<template>
<form @submit.prevent="rename()">
  <cytomine-modal :title="title" :active="active" @close="close()">
    <field :form="form" name="name" :validators="nameRules" v-slot="{field, state}">
      <b-field :label="$t('new-name')" :type="{'is-danger': !!state.meta.errors.length}" :message="state.meta.errors[0]">
        <b-input :model-value="state.value" @update:model-value="field.handleChange" />
      </b-field>
    </field>
    <template #footer>
      <button class="button" type="button" @click="close()">
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

import { required, rules, validateForm } from '@/utils/form.js';
import CytomineModal from './CytomineModal.vue';

export default {
  name: 'rename-modal',
  extends: CytomineModal,
  props: {
    currentName: String,
    title: String
  },
  components: { CytomineModal, Field },
  setup(props) {
    const form = useForm({ defaultValues: { name: props.currentName || '' } });
    return {
      form,
      isValid: form.useStore(state => state.isValid),
      nameRules: { onChange: rules(required) }
    };
  },
  watch: {
    active(active) {
      if (active) {
        this.form.reset({ name: this.currentName });
      }
    }
  },
  methods: {
    async rename() {
      if (!await validateForm(this.form)) {
        return;
      }
      this.$emit('rename', this.form.state.values.name);
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
