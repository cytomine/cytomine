<template>
  <form @submit.prevent="add">
    <cytomine-modal :active="active" :title="$t('add-store')" @close="cancel">
      <b-field :label="$t('name')">
        <b-input v-model="store.name" />
      </b-field>

      <b-field :label="$t('hostname')">
        <b-input v-model="store.host" />
      </b-field>

      <b-field :label="$t('default-store')">
        <b-switch v-model="store.defaultStore" class="switch">
          <template v-if="store.defaultStore">{{ $t('yes') }}</template>
          <template v-else>{{ $t('no') }}</template>
        </b-switch>
      </b-field>

      <template #footer>
        <button class="button" type="button" @click="cancel">
          {{ $t('button-cancel') }}
        </button>
        <button class="button is-link" :disabled="!isFormValid">
          {{ $t('button-add') }}
        </button>
      </template>
    </cytomine-modal>
  </form>
</template>

<script>
import CytomineModal from '@/components/utils/CytomineModal';

export default {
  name: 'AppStoreAddModal',
  components: {
    CytomineModal,
  },
  props: {
    active: {type: Boolean, default: false},
  },
  data() {
    return {
      store: {
        name: '',
        host: '',
        defaultStore: false,
      },
    };
  },
  computed: {
    isFormValid() {
      return this.store.name.trim() && this.store.host.trim();
    }
  },
  methods: {
    resetForm() {
      this.store = {
        name: '',
        host: '',
        defaultStore: false,
      };
    },
    add() {
      this.$emit('add-store', this.store);
      this.$emit('update:active', false);
      this.resetForm();

      this.$notify({type: 'success', text: this.$t('notify-success-app-store-addition')});
    },
    cancel() {
      this.$emit('update:active', false);
      this.resetForm();
    },
  },
};
</script>
