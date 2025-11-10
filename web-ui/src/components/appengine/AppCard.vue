<template>
  <div class="card">
    <router-link :to="{ path: `/apps/${app.namespace}/${app.version}`, query: { host: app.host } }">
      <div class="card-image img-rounded">
        <figure class="image is-animated is-5by3">
          <img :src="app.imageUrl || 'https://bulma.io/assets/images/placeholders/1280x960.png'"
            alt="Placeholder image">
        </figure>
      </div>
      <div class="card-content">
        <div class="media">
          <div class="media-content">
            <p class="title is-4 less-bottom">{{ app.name }}</p>
            <time datetime="">{{ app.date }}</time>
          </div>
          <div class="media-right">
            <p class="subtitle is-6">{{ app.version }}</p>
          </div>
        </div>

        <div class="content">
          {{ app.description }}
        </div>

        <footer class="card-footer">
          <b-button class="card-footer-item" v-if="installable" @click.prevent="handleInstall">
            {{ $t('install') }}
          </b-button>
          <a href="#" class="card-footer-item">{{ $t('button-more') }}</a>
        </footer>
      </div>
    </router-link>
  </div>
</template>

<script>
import {installApp} from '@/utils/app';

export default {
  name: 'AppCard',
  props: {
    app: {type: Object, required: true},
    installable: {type: Boolean, default: false},
  },
  methods: {
    async handleInstall() {
      installApp(this.app, this.$notify, this.$t.bind(this));
    },
  },
};
</script>

<style scoped>
.less-bottom {
  margin-bottom: 0.9rem !important;
}

.card {
  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2);
  border-radius: 10px;
  transition: .2s ease-out;
}

.card-footer button {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  margin: 0;
  border: none;
}

.rounded {
  border-radius: 10px;
}

/* On mouse-over, add a deeper shadow + Bounce */
.card:hover {
  box-shadow: 0 1px 100px 0 rgba(38, 63, 206, 0.3);
  transform: translate3d(0, -2px, 0);
}

/* Router-Link makes entire text blue so let's select everything except the links & make them black */
:not(a) {
  color: black;
}
</style>
