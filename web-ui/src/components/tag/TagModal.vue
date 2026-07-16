<template>
<form @submit.prevent="save()">
  <cytomine-modal :active="active" :title="title" @close="$emit('update:active', false)">
    <b-field :label="$t('name')" :type="{'is-danger': errors.has('name')}" :message="errors.first('name')">
      <b-input v-model="internalTag['name']" name="name" v-validate="'required'" />
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
import {Cytomine} from '@/api';
import CytomineModal from '@/components/utils/CytomineModal';

export default {
  name: 'tag-modal',
  props: {
    active: Boolean,
    tag: Object
  },
  components: {CytomineModal},
  $_veeValidate: {validator: 'new'},
  data() {
    return {
      internalTag: {},
      displayErrors: false,
    };
  },
  computed: {
    editionMode() {
      return Boolean(this.tag);
    },
    title() {
      return this.$t(this.editionMode ? 'update-tag' : 'create-tag');
    },
  },
  watch: {
    active(val) {
      if (val) {
        this.internalTag = (this.tag) ? {...this.tag} : {};
        this.displayErrors = false;
      }
    }
  },
  methods: {
    async save() {
      let result = await this.$validator.validateAll();
      if (!result) {
        return;
      }

      let labelTranslation = this.editionMode ? 'update' : 'creation';

      try {
        const {data} = this.editionMode
          ? await Cytomine.instance.api.put(`/tag/${this.tag.id}.json`, {name: this.internalTag.name})
          : await Cytomine.instance.api.post('/tag.json', {name: this.internalTag.name});
        this.$notify({type: 'success', text: this.$t('notif-success-tag-' + labelTranslation)});
        this.$emit('update:active', false);
        this.$emit(this.editionMode ? 'updateTag' : 'addTag', data.data);
      } catch (error) {
        console.log(error);
        this.$notify({type: 'error', text: this.$t('notif-error-tag-' + labelTranslation)});
      }
    }
  }
};
</script>
