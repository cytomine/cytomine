<template>
  <div class="content-wrapper">
    <div class="panel">
      <div class="panel-heading">
        {{ $t('configuration') }}
        <button class="button is-link" @click="showModal = true">{{ $t('add-store') }}</button>
      </div>
      <section class="panel-block">
        <b-table :data="stores" v-if="stores.length > 0">
          <template #default="{ row: store }">
            <b-table-column label="ID" width="40">
              {{ store.id }}
            </b-table-column>

            <b-table-column label="Name" width="40">
              {{ store.name }}
            </b-table-column>

            <b-table-column label="Host" width="40">
              <a :href="store.host" rel="noopener" target="_blank">
                {{ store.host }}
              </a>
            </b-table-column>

            <b-table-column label="Default" width="40" centered>
              <i class="fas fa-check-square" v-if="store.default"></i>
              <i class="fas fa-times-circle" v-else></i>
            </b-table-column>

            <b-table-column label="" width="40">
              <div class="buttons is-right">
                <button class="button is-small is-danger" @click="handleDelete(store)">
                  {{ $t('button-delete') }}
                </button>
              </div>
            </b-table-column>
          </template>
        </b-table>

        <p v-else class="has-text-centered">
          {{ $t('no-app-store-registered') }}
        </p>
      </section>
    </div>

    <AppStoreAddModal :active.sync="showModal" @add-store="handleAdd($event)" />
  </div>
</template>

<script>
import {Cytomine} from 'cytomine-client';

import AppStoreAddModal from '@/components/appengine/AppStoreAddModal.vue';

export default {
  name: 'AppConfigurationPage',
  components: {
    AppStoreAddModal,
  },
  data() {
    return {
      showModal: false,
      stores: [],
    };
  },
  async created() {
    await this.fetchStores();
  },
  methods: {
    async fetchStores() {
      try {
        this.stores = (await Cytomine.instance.api.get('/stores')).data;
      } catch (error) {
        console.error('Failed to fetch stores:', error);
      }
    },
    async addStore(store) {
      try {
        return (await Cytomine.instance.api.post('/stores', store)).data;
      } catch (error) {
        console.error('Failed to add store:', error);
      }
    },
    async deleteStore(store) {
      try {
        await Cytomine.instance.api.delete(`/stores/${store.id}`);

        this.stores = this.stores.filter(s => s.id !== store.id);

        this.$notify({type: 'success', text: this.$t('notify-success-app-store-deletion')});
      } catch (error) {
        console.error('Failed to delete store:', error);
      }
    },
    async handleAdd(storeData) {
      const createdStore = await this.addStore(storeData);
      this.stores.push(createdStore);
    },
    handleDelete(store) {
      this.$buefy.dialog.confirm({
        title: this.$t('confirm-deletion'),
        message: this.$t('confirm-deletion-store'),
        type: 'is-danger',
        confirmText: this.$t('button-confirm'),
        cancelText: this.$t('button-cancel'),
        onConfirm: () => this.deleteStore(store),
      });
    },
  },
};
</script>

<style scoped>
.fas {
  font-size: 2rem;
}

.fas.fa-check-square {
  color: green;
}

.fas.fa-times-circle {
  color: red;
}

.panel-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
