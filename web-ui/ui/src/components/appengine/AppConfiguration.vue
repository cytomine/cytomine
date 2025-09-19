<template>
  <div class="content-wrapper">
    <div class="panel">
      <p class="panel-heading">Configuration</p>
      <section class="panel-block">
        <b-table :data="data">
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
      </section>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'AppConfiguration',
  data() {
    return {
      data: [
        {
          id: 1,
          name: 'Cytomine Store',
          host: 'https://store.cytomine.org',
          default: true,
        },
        {
          id: 2,
          name: 'BIGPICTURE Store',
          host: 'https://store.bigpicture.org',
          default: false,
        },
      ],
    };
  },
  methods: {
    async deleteStore(store) {
      try {
        axios.delete(`/app-engine/v1/stores/${store.id}`);
      } catch (error) {
        console.error(error);
      }
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
</style>
