<template>
  <b-loading v-if="loading" :is-full-page="false" :active="loading" />

  <div class="app-container" v-else-if="appEngineEnabled">
    <AppSidebar />

    <div class="app-content">
      <div class="content-wrapper">
        <div class="panel">
          <p class="panel-heading">
            {{ $t('app-engine.tasks.name') }}
            <UploadAppButton btnFunc="upload" @taskUploadSuccess="handleTaskUploadSuccess" />
          </p>
          <section class="panel-block">
            <section id="lower-section-flex">
              <AppCard v-for="app in applications" :key="app.id" :appData="app" />
            </section>
          </section>
        </div>

        <div class="panel">
          <p class="panel-heading">{{ $t('app-store') }}</p>
          <section class="panel-block">
            <AppStoreList />
          </section>
        </div>
      </div>
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
import AppStoreList from '@/components/appengine/app-store/AppStoreList.vue';
import UploadAppButton from './UploadAppButton.vue';
import AppCard from './AppCard.vue';
import Task from '@/utils/appengine/task';
import constants from '@/utils/constants.js';

export default {
  name: 'AppPage',
  components: {
    AppCard,
    AppSidebar,
    AppStoreList,
    UploadAppButton,
  },
  data() {
    return {
      applications: [],
      loading: true,
      error: null,
      appEngineEnabled: constants.APPENGINE_ENABLED
    };
  },
  async created() {
    this.applications = await Task.fetchAll();
    this.loading = false;
  },
  methods: {
    async handleTaskUploadSuccess() {
      try {
        this.applications = await Task.fetchAll();
      } catch (error) {
        console.error('Error fetching tasks after upload:', error);
      }
    },
  },
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

.panel-block {
  padding-top: 0.8em;
}

.panel-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

#upper-section-flex {
  display: flex;
  flex-direction: row;
  /* Aligns items to the edges */
  justify-content: space-between;
  /* Aligns items vertically */
  align-items: center;
}

#upper-section-flex>* {
  padding: 1em;
}

#lower-section-flex {
  display: flex;
  flex-direction: row;
  gap: 1%;
  flex-wrap: wrap;
  flex-basis: 30%;
}

#lower-section-flex>* {
  flex-basis: 20%;
  margin: 1em;
}
</style>
