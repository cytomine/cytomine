<template>
<form @submit.prevent="createProject()">
  <b-loading :active="loading && !task" :is-full-page="false" />

  <cytomine-modal :active="active" :title="$t('create-project')" @close="$emit('update:active', false)">
    <template v-if="!task">
      <b-field :label="$t('name')" :type="{ 'is-danger': errors.has('name') }" :message="errors.first('name')">
        <b-input v-model="name" name="name" v-validate="'required'" />
      </b-field>

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

      <template v-if="ontology === 'EXISTING'">
        <b-field :type="{ 'is-danger': errors.has('ontology') }" :message="errors.first('ontology')">
          <b-select
            size="is-small"
            v-model="selectedOntology"
            :placeholder="$t('select-ontology')"
            name="ontology"
            v-validate="'required'"
          >
            <option v-for="ontology in ontologies" :value="ontology.id" :key="ontology.id">
              {{ ontology.name }}
            </option>
          </b-select>
        </b-field>
      </template>
    </template>

    <cytomine-task v-else :task.sync="task" />

    <template #footer>
      <button class="button" type="button" @click="$emit('update:active', false)">
        {{ $t('button-cancel') }}
      </button>
      <button v-if="!task" class="button is-link" :disabled="errors.any()">
        {{ $t('button-save') }}
      </button>
    </template>
  </cytomine-modal>
</form>
</template>

<script>
import { Cytomine } from '@/api';

import CytomineModal from '@/components/utils/CytomineModal.vue';
import CytomineTask from '@/components/utils/CytomineTask.vue';

export default {
  name: 'create-project-from-metadata-modal',
  props: {
    active: Boolean,
    query: String,
    filters: Array,
    ontologies: {
      type: Array,
      default: () => []
    }
  },
  components: { CytomineModal, CytomineTask },
  $_veeValidate: { validator: 'new' },
  data() {
    return {
      loading: false,
      task: null,
      name: '',
      ontology: 'NEW',
      selectedOntology: null,
    };
  },
  watch: {
    active(val) {
      if (val) {
        this.name = '';
        this.ontology = 'NEW';
        this.selectedOntology = null;
        this.task = null;
      }
    },
    'task.progress'(progress) {
      if (progress === 100) {
        this.handleCompletion();
      }
    }
  },
  methods: {
    async createProject() {
      let result = await this.$validator.validateAll();
      if (!result) {
        return;
      }
      this.loading = true;

      try {
        let payload = {
          name: this.name,
          ontologyMode: this.ontology,
          query: this.query,
          filters: this.filters,
        };
        if (this.ontology === 'EXISTING') {
          payload.ontologyId = this.selectedOntology;
        }

        let { data } = await Cytomine.instance.api.post('project/from-search', payload);

        this.loading = false;
        this.task = data.task;
      } catch (error) {
        this.loading = false;
        if (error.response.status === 409) {
          this.$notify({ type: 'error', text: this.$t('notif-error-project-already-exists') });
        } else {
          this.$notify({ type: 'error', text: this.$t('notif-error-project-creation') });
        }
      }
    },
    handleCompletion() {
      this.$emit('update:active', false);

      let comments = (this.task && this.task.comments) || [];

      let error = comments.find(comment => /^\s*\d+%:Error:/.test(comment));
      if (error) {
        this.$notify({ type: 'error', text: this.$t('notif-error-project-from-search') });
        return;
      }

      let matched = comments.find(comment => /^\s*\d+%:Matched (\d+) images/.test(comment));
      let count = matched ? Number.parseInt(matched.match(/^\s*\d+%:Matched (\d+) images/)[1], 10) : null;

      if (count !== null && !Number.isNaN(count)) {
        this.$notify({ type: 'success', text: this.$t('notif-success-project-from-search-created', { count }) });
      } else {
        this.$notify({ type: 'success', text: this.$t('notif-success-project-from-search') });
      }
    }
  }
};
</script>