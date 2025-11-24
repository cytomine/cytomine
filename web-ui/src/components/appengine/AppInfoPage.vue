<template>
  <div class="content-wrapper">
    <b-loading v-if="loading" :is-full-page="false" :active="loading" />
    <div v-else class="panel">
      <div class="panel-heading">
        <b-button
          icon-pack="fa"
          icon-left="angle-left"
          @click="$router.push(task.host !== undefined ? '/apps/store' : '/apps')"
          :label="$t('go-back')"
        />
        <div class="panel-actions">
          <b-button
            class="is-link"
            icon-pack="fa"
            icon-left="download"
            @click="handleInstall"
            :label="$t('install')"
          />
          <b-dropdown
            v-if="task.host === undefined"
            aria-role="list"
            class="is-pulled-right"
            position="is-bottom-left"
          >
            <template #trigger>
              <b-icon icon="ellipsis-v" />
            </template>

            <b-dropdown-item aria-role="listitem" class="has-text-danger" @click="handleDelete">
              <b-icon icon="trash" class="has-text-danger" />
              <span>{{ $t('button-delete') }}</span>
            </b-dropdown-item>
          </b-dropdown>
        </div>
      </div>
      <div class="panel-block">
        <section class="media">
          <figure class="media-left">
            <div class="image logo">
              <img :src="task.imageUrl || 'https://bulma.io/assets/images/placeholders/1280x960.png'">
            </div>
          </figure>
          <div class="media-content app-content">
            <div class="content">
              <strong class="app-title">{{ task.name }}</strong>
              <br>
              <small v-for="(author, index) in task.authors" :key="index">
                {{ `- ${author.first_name} ${author.last_name}` }}
              </small>
            </div>
          </div>
        </section>

        <section class="metadata level">
          <div class="level-item has-text-centered">
            <div>
              <p class="heading">Date</p>
              <p class="title">{{ task.date || $t('unknown') }}</p>
            </div>
          </div>

          <div class="level-item has-text-centered">
            <div>
              <p class="heading">Version</p>
              <p class="title">{{ task.version }}</p>
            </div>
          </div>
        </section>

        <section>
          <b-collapse class="card" animation="slide">
            <template #trigger="props">
              <div class="card-header" role="button">
                <p class="card-header-title">
                  {{ $t("description") }}
                </p>
                <a class="card-header-icon">
                  <b-icon :icon="props.open ? 'menu-down' : 'menu-up'" />
                </a>
              </div>
            </template>

            <div class="card-content">
              <div class="content">
                {{ task.description || $t('no-description') }}
              </div>
            </div>
          </b-collapse>
        </section>
      </div>
    </div>
  </div>
</template>

<script>
import {deleteApp, installApp} from '@/utils/app';
import Task from '@/utils/appengine/task';

export default {
  name: 'AppInfoPage',
  data() {
    return {
      task: null,
      loading: true,
    };
  },
  methods: {
    async handleInstall() {
      await installApp(this.task, this.$notify, this.$t.bind(this));
    },
    handleDelete() {
      this.$buefy.dialog.confirm({
        title: this.$t('delete-task'),
        message: this.$t('delete-task-message'),
        type: 'is-danger',
        confirmText: this.$t('button-confirm'),
        cancelText: this.$t('button-cancel'),
        onConfirm: async () => {
          await deleteApp(this.task, this.$notify, this.$t.bind(this));
          this.$router.push('/apps');
        },
      });
    }
  },
  async created() {
    this.task = await Task.fetchNamespaceVersion(
      this.$route.params.namespace,
      this.$route.params.version,
      this.$route.query.host,
    );
    this.task.host = this.$route.query.host;
    this.loading = false;
  },
};
</script>

<style scoped>
/* ----- Upper Section (Logo + Update) ----- */
.logo {
  width: 17rem;
  height: 13rem;
  position: relative;
  overflow: hidden;
  /* images that overflow (ex. portrait) will need this one */
  border-radius: 15%;
}

img {
  position: absolute;
  width: 100%;
  top: 50%;
  -ms-transform: translateY(-50%);
  -webkit-transform: translateY(-50%);
  transform: translateY(-50%);
  /* for none overflowing imgs */
  border-radius: 15%;
}

.app-content {
  margin: 1%;
  padding-top: 2%;
}

.app-title {
  font-size: 1.9rem;
}

.panel-heading {
  display: flex;
  justify-content: space-between;
}

.panel-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.update-btn {
  margin: 3%;
  size: 2rem;
}

.metadata {
  margin-top: 5%;
}
</style>
