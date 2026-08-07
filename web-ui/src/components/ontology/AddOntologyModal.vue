<template>
<form @submit.prevent="createOntology()">
  <cytomine-modal :active="active" :title="$t('create-ontology')" @close="$emit('update:active', false)">
    <field :form="form" name="name" :validators="nameRules" v-slot="{field, state}">
      <b-field :label="$t('name')" :type="{'is-danger': !!state.meta.errors.length}" :message="state.meta.errors[0]">
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

import { Ontology } from '@/api';
import { required, rules, validateForm } from '@/utils/form.js';
import CytomineModal from '@/components/utils/CytomineModal.vue';

export default {
  name: 'add-ontology-modal',
  props: {
    active: Boolean
  },
  components: { CytomineModal, Field },
  setup() {
    const form = useForm({ defaultValues: { name: '' } });
    return {
      form,
      isValid: form.useStore(state => state.isValid),
      nameRules: { onChange: rules(required) }
    };
  },
  watch: {
    active(val) {
      if (val) {
        this.form.reset({ name: '' });
      }
    }
  },
  methods: {
    async createOntology() {
      if (!await validateForm(this.form)) {
        return;
      }

      try {
        let ontology = await new Ontology({ name: this.form.state.values.name }).save();
        this.$notify({ type: 'success', text: this.$t('notif-success-ontology-creation') });
        this.$emit('newOntology', ontology);
        this.$emit('update:active', false);
      } catch (error) {
        console.log(error);
        this.$notify({ type: 'error', text: this.$t('notif-error-ontology-creation') });
      }
    }
  }
};
</script>
