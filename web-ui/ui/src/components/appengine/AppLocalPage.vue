<template>
  <div class="content-wrapper">
    <b-loading :is-full-page="false" :active="loading" />

    <b-message v-if="error" type="is-danger" has-icon icon-size="is-small">
      {{ $t('failed-fetch-tasks') }}
    </b-message>

    <div v-else class="panel">
      <p class="panel-heading">
        {{ $t('app-engine.tasks.name') }}
        <UploadAppButton btnFunc="upload" @taskUploadSuccess="handleTaskUpload" />
      </p>
      <section class="panel-block">
        <section id="lower-section-flex">
          <AppCard v-for="app in applications" :key="app.id" :appData="app" />
        </section>
      </section>
    </div>
  </div>
</template>

<script>
import AppCard from '@/components/appengine/AppCard.vue';
import UploadAppButton from '@/components/appengine/UploadAppButton.vue';
import Task from '@/utils/appengine/task';

export default {
  name: 'AppLocalPage',
  components: {
    AppCard,
    UploadAppButton,
  },
  data() {
    return {
      applications: [],
      error: '',
      loading: true,
    };
  },
  async created() {
    try {
      this.applications = await Task.fetchAll();
    } catch (error) {
      this.error = error.message;
    } finally {
      this.loading = false;
    }
  },
  methods: {
    async handleTaskUpload() {
      try {
        this.applications = await Task.fetchAll();
      } catch (error) {
        console.error('Error fetching tasks after upload:', error);
        this.error = error.message;
      }
    },
  },
};
</script>

<style scoped>
.panel-block {
  padding-top: 0.8em;
}

.panel-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
