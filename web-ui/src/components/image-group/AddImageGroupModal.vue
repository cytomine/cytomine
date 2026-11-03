<template>
  <form @submit.prevent="createImageGroup()">
    <cytomine-modal :active="active" :title="$t('create-image-group')" @close="$emit('update:active', false)">
      <field :form="form" name="name" :validators="nameRules" v-slot="{field, state}">
        <b-field :label="$t('name')" :type="{'is-danger': !!state.meta.errors.length}" :message="state.meta.errors[0]">
          <b-input name="name" :model-value="state.value" @update:model-value="field.handleChange" />
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

import { ImageGroup } from '@/api';
import { required, rules, validateForm } from '@/utils/form.js';
import CytomineModal from '@/components/utils/CytomineModal.vue';
import { get } from '@/utils/store-helpers';

export default {
  name: 'add-image-group-modal',
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
  computed: {
    project: get('currentProject/project'),
  },
  watch: {
    active(val) {
      if (val) {
        this.form.reset({ name: '' });
      }
    }
  },
  methods: {
    async createImageGroup() {
      if (!await validateForm(this.form)) {
        return;
      }

      try {
        let imageGroup = await new ImageGroup({
          name: this.form.state.values.name,
          project: this.project.id
        }).save();
        this.$notify({ type: 'success', text: this.$t('notif-success-image-group-creation') });
        this.$emit('update:active', false);
        this.$emit('newImageGroup', imageGroup);
      } catch (error) {
        console.log(error);
        this.$notify({ type: 'error', text: this.$t('notif-error-image-group-creation') });
      }
    }
  }
};
</script>
