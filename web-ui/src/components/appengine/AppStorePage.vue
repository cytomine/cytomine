<template>
  <div class="content-wrapper">
    <div class="panel">
      <p class="panel-heading">{{ $t('app-store') }}</p>
      <section class="panel-block lower-section-flex">
        <AppCard v-for="app in applications" :key="app.id" :app="app" :installable="true" />
      </section>
    </div>
  </div>
</template>

<script>
import AppCard from '@/components/appengine/AppCard.vue';
import {Cytomine} from '@/api';

export default {
  name: 'AppStorePage',
  components: {
    AppCard,
  },
  data() {
    return {
      applications: [],
    };
  },
  async created() {
    this.stores.forEach(async (store) => {
      const {data} = await Cytomine.instance.api.get('/stores/tasks', {
        params: {host: encodeURIComponent(store.host)},
      });

      const tasks = data.map(task => ({
        ...task,
        host: store.host,
      }));

      this.applications = [
        ...this.applications,
        ...tasks,
      ];
    });
  },
  computed: {
    stores() {
      return this.$store.getters['appStores/stores'];
    },
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
