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

  <field :form="form" name="name" :validators="nameRules" v-slot="{field, state}">
    <b-field :label="$t('name')" :type="{'is-danger': !!state.meta.errors.length}" :message="state.meta.errors[0]">
      <b-input :model-value="state.value" :disabled="!selectedFile" @update:model-value="field.handleChange" />
    </b-field>
  </field>

  <template #footer>
    <button class="button" @click="$parent.close()">
      {{$t('button-cancel')}}
    </button>
    <button class="button is-link" :disabled="!selectedFile || !isValid" @click="save()">
      {{$t('button-save')}}
    </button>
  </template>
</cytomine-modal-card>
</template>

<script>
import { Field, useForm } from '@tanstack/vue-form';

import { AttachedFile } from '@/api';
import { required, rules, validateForm } from '@/utils/form.js';
import CytomineModalCard from '@/components/utils/CytomineModalCard.vue';

export default {
  name: 'attached-file-modal',
  props: {
    object: Object
  },
  components: { CytomineModalCard, Field },
  setup() {
    const form = useForm({ defaultValues: { name: '' } });
    return {
      form,
      isValid: form.useStore(state => state.isValid),
      nameRules: { onChange: rules(required) }
    };
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
    async save() {
      if (!this.selectedFile || !await validateForm(this.form)) {
        return;
      }

      try {
        let attached = await new AttachedFile(
          { file: this.selectedFile, filename: this.form.state.values.name },
          this.object
        ).save();
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


