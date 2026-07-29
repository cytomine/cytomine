<template>
<form @submit.prevent="rename()">
  <cytomine-modal :title="title" :active="active" @close="close()">
    <b-field :label="$t('new-name')" :type="{'is-danger': errors.has('name')}" :message="errors.first('name')">
      <b-input v-model="newName" name="name" v-validate="'required'" />
    </b-field>
    <template #footer>
      <button class="button" type="button" @click="close()">
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
import CytomineModal from './CytomineModal.vue';

export default {
  name: 'rename-modal',
  extends: CytomineModal,
  props: {
    currentName: String,
    title: String
  },
  components: {CytomineModal},
  $_veeValidate: {validator: 'new'},
  data() {
    return {
      newName: ''
    };
  },
  watch: {
    active(active) {
      if (active) {
        this.newName = this.currentName;
      }
    }
  },
  methods: {
    async rename() {
      let result = await this.$validator.validateAll();
      if (!result) {
        return;
      }
      this.$emit('rename', this.newName);
      this.$emit('update:active', false);
    }
  },
  async created() {
    this.newName = this.currentName;
  }
};
</script>

<style scoped>
:deep(input[type=text]) {
  width: 26em;
}
</style>
