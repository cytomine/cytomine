<template>
<form @submit.prevent="addToImageGroup()">
  <cytomine-modal :active="active" :title="$t('add-to-image-group')" @close="$emit('update:active', false)">
    <b-loading :is-full-page="false" :active="loading" />
    <template v-if="!loading">
      <b-field :label="$t('image-group')">
        <b-radio v-model="imageGroup" native-value="NEW">
          {{$t('create-image-group')}}
        </b-radio>
      </b-field>
      <field v-if="imageGroup === 'NEW'" :form="form" name="name" :validators="requiredRule" v-slot="{field, state}">
        <b-field :type="{'is-danger': !!state.meta.errors.length}" :message="state.meta.errors[0]">
          <b-input :model-value="state.value" @update:model-value="field.handleChange" />
        </b-field>
      </field>
      <b-field>
        <b-radio v-model="imageGroup" native-value="EXISTING">
          {{$t('use-existing-image-group')}}
        </b-radio>
      </b-field>

      <field v-if="imageGroup === 'EXISTING'" :form="form" name="imageGroup" :validators="requiredRule" v-slot="{field, state}">
        <b-field :type="{'is-danger': !!state.meta.errors.length}" :message="state.meta.errors[0]">
          <b-select
              :model-value="state.value"
              @update:model-value="field.handleChange"
              :placeholder="$t('select-image-group')"
              expanded
          >
            <option v-for="group in imageGroups" :value="group.id" :key="group.id">
              {{group.name}}
            </option>
          </b-select>
        </b-field>
      </field>

    </template>



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

import CytomineModal from '@/components/utils/CytomineModal.vue';

import { ImageGroupCollection, ImageGroup, ImageGroupImageInstance } from '@/api';
import { required, rules, validateForm } from '@/utils/form.js';

export default {
  name: 'add-to-image-group-modal',
  props: {
    active: { type: Boolean },
    image: { type: Object }
  },
  components: { CytomineModal, Field },
  setup() {
    const form = useForm({ defaultValues: { name: '', imageGroup: null } });
    return {
      form,
      isValid: form.useStore(state => state.isValid),
      requiredRule: { onChange: rules(required) }
    };
  },
  data() {
    return {
      imageGroup: 'NEW',
      imageGroups: [],
      loading: true
    };
  },
  computed: {
    blindMode() {
      return this.$store.state.currentProject.project.blindMode;
    },
    imageNameNotif() {
      return this.blindMode ? this.image.blindedName : this.image.instanceFilename;
    },
  },
  watch: {
    active(val) {
      if (val) {
        this.form.reset({ name: '', imageGroup: null });
        this.imageGroup = 'NEW';
        this.fetchImageGroups(); // TODO: improve
      }
    }
  },
  methods: {
    async addToImageGroup() {
      if (!await validateForm(this.form)) {
        return;
      }

      try {
        let idImageGroup;
        if (this.imageGroup === 'NEW') {
          let imageGroup = await new ImageGroup({
            name: this.form.state.values.name,
            project: this.image.project
          }).save();
          idImageGroup = imageGroup.id;
        } else if (this.imageGroup === 'EXISTING') {
          idImageGroup = this.form.state.values.imageGroup;
        }

        let link = await new ImageGroupImageInstance({ image: this.image.id, group: idImageGroup }).save();
        this.$emit('addToImageGroup', link);
        this.$notify({ type: 'success', text: this.$t('notif-success-image-group-link-creation', { imageName: this.imageNameNotif }) });
      } catch (error) {
        console.log(error);
        this.$notify({ type: 'error', text: this.$t('notif-error-image-group-link-creation', { imageName: this.imageNameNotif }) });
      }
      this.$emit('update:active', false);
    },
    async fetchImageGroups() {
      try {
        this.imageGroups = (await ImageGroupCollection.fetchAll({
          filterKey: 'project',
          filterValue: this.image.project,
        })).array;
      } catch (error) {
        console.log(error);
        this.error = true;
      }
    }
  },
  async created() {
    await this.fetchImageGroups();
    this.loading = false;
  }
};
</script>

