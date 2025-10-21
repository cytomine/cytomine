<template>
  <div class="app-container" v-if="appEngineEnabled">
    <AppSidebar />

    <div class="app-content">
      <router-view />
    </div>
  </div>

  <div v-else>
    <b-message :title="$t('appengine-not-enabled-title')" type="is-info">
      {{ $t('appengine-not-enabled-description') }}
    </b-message>
  </div>
</template>

<script>
import AppSidebar from '@/components/appengine/AppSidebar.vue';
import constants from '@/utils/constants.js';
import {Cytomine} from '@/api';

export default {
  name: 'AppLayout',
  components: {
    AppSidebar,
  },
  data() {
    return {
      appEngineEnabled: constants.APPENGINE_ENABLED
    };
  },
  async created() {
    if (!this.appEngineEnabled) {
      return;
    }

    await this.fetchStores();
  },
  methods: {
    async fetchStores() {
      try {
        const stores = (await Cytomine.instance.api.get('/stores')).data;
        this.$store.commit('appStores/set', stores);
      } catch (error) {
        console.error('Failed to fetch stores:', error);
      }
    },
  }
};
</script>

<style scoped>
.app-container {
  display: flex;
  height: 100%;
  flex: 1;
  background: #d4d4d4;
  overflow-y: auto;
  position: relative;
}

.app-content {
  flex: 1;
  position: relative;
  overflow-y: auto;
}
</style>
