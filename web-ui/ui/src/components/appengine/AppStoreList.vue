<template>
  <div class="content-wrapper">
    <div class="panel">
      <p class="panel-heading">{{ $t('app-store') }}</p>
      <section class="panel-block lower-section-flex">
        <AppCard v-for="app in applications" :key="app.id" :appData="app" />
      </section>
    </div>
  </div>
</template>

<script>
import AppCard from '@/components/appengine/AppCard.vue';
import Task from '@/utils/appengine/task';

export default {
  name: 'AppStoreList',
  components: {
    AppCard,
  },
  data() {
    return {
      applications: [],
    };
  },
  async created() {
    this.applications = await Task.fetchAll();
    this.loading = false;
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

.lower-section-flex {
  display: flex;
  flex-direction: row;
  gap: 1%;
  flex-wrap: wrap;
  flex-basis: 30%;
}

.lower-section-flex>* {
  flex-basis: 20%;
  margin: 1em;
}
</style>
