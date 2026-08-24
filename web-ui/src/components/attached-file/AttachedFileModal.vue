<template>
<cytomine-modal-card :title="$t('add-attached-file')">
  <b-field>
    <b-upload v-model="selectedFile" type="is-link" drag-drop>
      <section class="section">
        <div class="content has-text-centered">
          <template v-if="!selectedFile">
            <p><i class="fas fa-upload fa-3x"></i></p>
            <p>{{$t('choose-file-or-drop')}}</p>
          </template>
          <template v-else>
            <p class="filename is-size-4"><i class="fas fa-file"></i> {{selectedFile.name}}</p>
            <p class="has-text-grey is-size-6">{{$t('click-or-drop-new-file')}}</p>
          </template>
        </div>
      </section>
    </b-upload>
  </b-field>

  <Field :form="form" name="name" :validators="{ onChange: requiredValidator }" v-slot="{ field }">
    <b-field
      :label="$t('name')"
      :type="{'is-danger': field.state.meta.errors.length > 0}"
      :message="field.state.meta.errors[0]"
    >
      <b-input
        name="name"
        :disabled="!selectedFile"
        :model-value="field.state.value"
        @update:model-value="field.handleChange"
        @blur="field.handleBlur"
      />
    </b-field>
  </Field>

  <template #footer>
    <button class="button" @click="$parent.close()">
      {{$t('button-cancel')}}
    </button>
    <button class="button is-link" :disabled="!selectedFile || !canSubmit" @click="save()">
      {{$t('button-save')}}
    </button>
  </template>
</cytomine-modal-card>
</template>

<script>
import { Field, useForm } from '@tanstack/vue-form';

import { AttachedFile } from '@/api';
import CytomineModalCard from '@/components/utils/CytomineModalCard.vue';

export default {
  name: 'attached-file-modal',
  components: { CytomineModalCard, Field },
  props: {
    object: Object
  },
  setup() {
    const form = useForm({
      defaultValues: { name: '' }
    });
    const canSubmit = form.useStore(state => state.canSubmit);
    return { form, canSubmit };
  },
  data() {
    return {
      selectedFile: null
    };
  },
  watch: {
    selectedFile(file) {
      if (file) {
        this.form.setFieldValue('name', file.name);
      }
    }
  },
  methods: {
    requiredValidator({ value }) {
      return value && String(value).trim().length > 0 ? undefined : 'This field is required';
    },
    async save() {
      await this.form.validateAllFields('submit');
      if (!this.selectedFile || !this.form.state.isValid) {
        return;
      }

      try {
        let filename = this.form.state.values.name;
        let attached = await new AttachedFile({ file: this.selectedFile, filename }, this.object).save();
        this.$emit('addAttachedFile', attached);
        this.$notify({ type: 'success', text: this.$t('notif-success-attached-file-creation') });
        this.$parent.close();
      } catch (error) {
        console.log(error);
        this.$notify({ type: 'error', text: this.$t('notif-error-attached-file-creation') });
      }
    },
  }
};
</script>

<style scoped>
.filename {
  color: rgb(50, 115, 220);
}

.filename .fas {
  margin-right: 0.5em;
}

:deep(.upload-draggable) {
  margin-left: 10%;
  width: 80%;
}
</style>
