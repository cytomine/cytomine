<template>
  <div class="content-wrapper">
    <div class="panel">
      <p class="panel-heading">{{ $t('app-dashboard') }}</p>

      <section class="panel-block">
        <b-table :data="data">
          <template #default="{ row: run }">
            <b-table-column :label="$t('app-name')">
              {{ run.name }}
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
                <button class="button is-small is-danger" @click="handleDelete(run)">
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
  },
};
</script>
