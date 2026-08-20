<template>
<form @submit.prevent="save()">
  <cytomine-modal :active="active" :title="title" @close="$emit('update:active', false)">
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

import { Cytomine } from '@/api';
import { required, rules, validateForm } from '@/utils/form.js';
import CytomineModal from '@/components/utils/CytomineModal.vue';

export default {
  name: 'tag-modal',
  props: {
    active: Boolean,
    tag: Object
  },
  components: { CytomineModal, Field },
  setup(props) {
    const form = useForm({ defaultValues: { name: props.tag ? props.tag.name : '' } });
    return {
      form,
      isValid: form.useStore(state => state.isValid),
      nameRules: { onChange: rules(required) }
    };
  },
  data() {
    return {
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
        this.form.reset({ name: this.tag ? this.tag.name : '' });
        this.displayErrors = false;
      }
    }
  },
  methods: {
    async save() {
      if (!await validateForm(this.form)) {
        return;
      }

      let name = this.form.state.values.name;
      let labelTranslation = this.editionMode ? 'update' : 'creation';

      try {
        const { data } = this.editionMode
          ? await Cytomine.instance.api.put(`/tag/${this.tag.id}.json`, { name })
          : await Cytomine.instance.api.post('/tag.json', { name });
        this.$notify({ type: 'success', text: this.$t('notif-success-tag-' + labelTranslation) });
        this.$emit('update:active', false);
        this.$emit(this.editionMode ? 'updateTag' : 'addTag', data.data);
      } catch (error) {
        console.log(error);
        this.$notify({ type: 'error', text: this.$t('notif-error-tag-' + labelTranslation) });
      }
    }
  }
};
</script>
