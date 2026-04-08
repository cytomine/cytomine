<template>
  <div class="content-wrapper">
    <div class="panel">
      <p class="panel-heading">{{ $t('app-dashboard') }}</p>

      <section class="panel-block">
        <b-table :data="data">
          <template #default="{ row: run }">
            <b-table-column :label="$t('app-name')">
              <router-link :to="`/apps/${run.namespace}/${run.version}`">
                {{ run.name }}
              </router-link>
            </b-table-column>

            <b-table-column :label="$t('launched-by')">
              {{ run.user }}
            </b-table-column>

            <b-table-column :label="$t('execution-date')">
              {{ run.date }}
            </b-table-column>

            <b-table-column :label="$t('status')" centered>
              <span class="tag" :class="statusClass(run.status)">
                {{ run.status }}
              </span>
            </b-table-column>

            <b-table-column :label="$t('actions')" centered>
              <div class="buttons is-centered">
                <template
                  v-if="['created', 'provisioned', 'queuing', 'queued', 'running', 'pending'].includes(run.status.toLowerCase())">
                  <button class="button is-small is-danger is-light" @click="handleCancel(run)">
                    {{ $t('button-cancel') }}
                  </button>
                </template>

                <template v-else-if="['finished', 'failed'].includes(run.status.toLowerCase())">
                  <button class="button is-small is-info is-light" @click="handleViewLogs(run)">
                    {{ $t('view-logs') }}
                  </button>
                  <button class="button is-small is-danger is-light" @click="handleDelete(run)">
                    <b-icon icon="trash" class="has-text-white" />
                  </button>
                </template>
              </div>
            </b-table-column>
          </template>
        </b-table>
      </section>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AppDashboardPage',
  data() {
    return {
      data: [
        {id: 1, name: 'Stardist (1.0.0)', user: 'John Doe', date: '2016-10-15 13:43:27', status: 'Finished'},
        {id: 12, name: 'Stardist (1.0.0)', user: 'John Doe', date: '2016-10-15 13:43:28', status: 'Failed'},
        {id: 3, name: 'Stardist (1.0.0)', user: 'John Doe', date: '2016-10-15 13:43:29', status: 'Running'},
        {id: 4, name: 'Stardist (1.0.0)', user: 'John Doe', date: '2016-10-15 13:43:30', status: 'Pending'},
        {id: 5, name: 'Stardist (1.0.0)', user: 'John Doe', date: '2016-10-15 13:43:31', status: 'Queuing'},
        {id: 6, name: 'Stardist (1.0.0)', user: 'John Doe', date: '2016-10-15 13:43:32', status: 'Queued'},
        {id: 7, name: 'Stardist (1.0.0)', user: 'John Doe', date: '2016-10-15 13:43:33', status: 'Created'},
        {id: 8, name: 'Stardist (1.0.0)', user: 'John Doe', date: '2016-10-15 13:43:34', status: 'Provisioned'},
      ],
    };
  },
  methods: {
    statusClass(status) {
      const map = {
        created: 'is-light',
        provisioned: 'is-info is-light',
        queuing: 'is-warning is-light',
        queued: 'is-warning',
        running: 'is-primary',
        pending: 'is-warning is-light',
        failed: 'is-danger',
        finished: 'is-success',
      };
      return map[status.toLowerCase()] ?? 'is-light';
    },
    handleCancel(run) {
      this.$buefy.dialog.confirm({
        title: this.$t('confirm-cancellation'),
        message: this.$t('confirm-cancel-run'),
        type: 'is-danger',
        confirmText: this.$t('button-confirm'),
        cancelText: this.$t('button-cancel'),
        onConfirm: () => {},
      });
    },
    handleViewLogs(run) { /* ... */},
    handleDelete(run) {
      this.$buefy.dialog.confirm({
        title: this.$t('confirm-deletion'),
        message: this.$t('confirm-deletion-run'),
        type: 'is-danger',
        confirmText: this.$t('button-confirm'),
        cancelText: this.$t('button-cancel'),
        onConfirm: () => {},
      });
    },
  },
};
</script>
