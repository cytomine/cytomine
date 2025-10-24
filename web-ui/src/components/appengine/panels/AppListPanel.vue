<template>
  <div class="panel">
    <p class="panel-heading">{{ $t('app-engine.tasks.installed') }}</p>

    <section class="panel-block lower-section-flex">
      <AppCard v-for="app in applications" :key="app.id" :app="app" />
    </section>
  </div>
</template>

<script>
import AppCard from '@/components/appengine/AppCard.vue';

export default {
  name: 'AppListPanel',
  components: {
    AppCard,
  },
  props: {
    applications: {type: Array, default: () => []},
  },
};
</script>

<style scoped>
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

.panel-block {
  padding-top: 0.8em;
}

.panel-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
