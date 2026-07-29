<template>
<form @submit.prevent="createOntology()">
  <cytomine-modal :active="active" :title="$t('create-ontology')" @close="$emit('update:active', false)">
    <b-field :label="$t('name')" :type="{'is-danger': errors.has('name')}" :message="errors.first('name')">
      <b-input v-model="name" name="name" v-validate="'required'" />
    </b-field>

    <template #footer>
      <button class="button" type="button" @click="$emit('update:active', false)">
        {{$t('button-cancel')}}
      </button>
      <button class="button is-link" :disabled="errors.any()">
        {{$t('button-save')}}
      </button>
    </template>
  </cytomine-modal>
</form>
</template>

<script>
import {Ontology} from '@/api';
import CytomineModal from '@/components/utils/CytomineModal.vue';

export default {
  name: 'add-ontology-modal',
  props: {
    active: Boolean
  },
  components: {CytomineModal},
  $_veeValidate: {validator: 'new'},
  data() {
    return {
      name: ''
    };
  },
  watch: {
    active(val) {
      if (val) {
        this.name = '';
      }
    }
  },
  methods: {
    async createOntology() {
      let result = await this.$validator.validateAll();
      if (!result) {
        return;
      }

      try {
        let ontology = await new Ontology({name: this.name}).save();
        this.$notify({type: 'success', text: this.$t('notif-success-ontology-creation')});
        this.$emit('newOntology', ontology);
        this.$emit('update:active', false);
      } catch (error) {
        console.log(error);
        this.$notify({type: 'error', text: this.$t('notif-error-ontology-creation')});
      }
    }
  }
};
</script>
