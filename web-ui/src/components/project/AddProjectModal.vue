<template>
<form @submit.prevent="createProject(); loading = true">
  <b-loading :model-value="loading" :is-full-page="false" />

  <template v-if="!loading">
    <cytomine-modal :active="active" :title="$t('create-project')" @close="$emit('update:active', false)">
      <field :form="form" name="name" :validators="requiredRule" v-slot="{field, state}">
        <b-field :label="$t('name')" :type="{ 'is-danger': !!state.meta.errors.length }" :message="state.meta.errors[0]">
          <b-input name="name" :model-value="state.value" @update:model-value="field.handleChange" />
        </b-field>
      </field>

      <b-field :label="$t('ontology')">
        <b-radio v-model="ontology" native-value="NEW">
          {{ $t('create-ontology-for-project') }}
        </b-radio>
      </b-field>
      <b-field>
        <b-radio v-model="ontology" native-value="EXISTING">
          {{ $t('use-existing-ontology') }}
        </b-radio>
      </b-field>
      <b-field>
        <b-radio v-model="ontology" native-value="NO">
          {{ $t('no-ontology') }}
        </b-radio>
      </b-field>

      <field v-if="ontology === 'EXISTING'" :form="form" name="ontology" :validators="requiredRule" v-slot="{field, state}">
        <b-field :type="{ 'is-danger': !!state.meta.errors.length }" :message="state.meta.errors[0]">
          <b-select
            size="is-small"
            :model-value="state.value"
            @update:model-value="field.handleChange"
            :placeholder="$t('select-ontology')"
          >
            <option v-for="option in ontologies" :value="option.id" :key="option.id">
              {{ option.name }}
            </option>
          </b-select>
        </b-field>
      </field>

      <template #footer>
        <button class="button" type="button" @click="$emit('update:active', false)">
          {{ $t('button-cancel') }}
        </button>
        <button class="button is-link" :disabled="!isValid">
          {{ $t('button-save') }}
        </button>
      </template>
    </cytomine-modal>
  </template>
</form>
</template>

<script>
import { Field, useForm } from '@tanstack/vue-form';

import { Project, Ontology } from '@/api';

import { required, rules, validateForm } from '@/utils/form.js';
import CytomineModal from '@/components/utils/CytomineModal.vue';

export default {
  name: 'add-project-modal',
  props: {
    active: Boolean,
    ontologies: Array
  },
  components: { CytomineModal, Field },
  setup() {
    // The ontology select is only mounted on the 'EXISTING' branch, and
    // unmounting a `Field` deregisters it, so the other two radio choices leave
    // it out of validation.
    const form = useForm({ defaultValues: { name: '', ontology: null } });
    return {
      form,
      isValid: form.useStore(state => state.isValid),
      requiredRule: { onChange: rules(required) }
    };
  },
  data() {
    return {
      loading: false,
      ontology: 'NEW',
    };
  },
  watch: {
    active(val) {
      if (val) {
        this.form.reset({ name: '', ontology: null });
        this.ontology = 'NEW';
      }
    }
  },
  methods: {
    async createProject() {
      if (!await validateForm(this.form)) {
        return;
      }

      let name = this.form.state.values.name;
      try {
        let idOntology;
        if (this.ontology === 'NEW') {
          let ontology = await new Ontology({ name }).save();
          idOntology = ontology.id;
        } else if (this.ontology === 'EXISTING') {
          idOntology = this.form.state.values.ontology;
        }

        let project = await new Project({ name, ontology: idOntology }).save();

        this.loading = false;
        this.$notify({ type: 'success', text: this.$t('notif-success-project-creation') });
        this.$emit('update:active', false);
        await this.$router.push(`/project/${project.id}/configuration`);
      } catch (error) {
        if (error.response.status === 409) {
          this.$notify({ type: 'error', text: this.$t('notif-error-project-already-exists') });
        } else {
          this.$notify({ type: 'error', text: this.$t('notif-error-project-creation') });
        }
      }
    }
  },
};
</script>
