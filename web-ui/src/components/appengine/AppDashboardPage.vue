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
              {{ run.status }}
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
        {
          id: 1,
          name: 'Stardist (1.0.0)',
          user: 'John Doe',
          date: '2016-10-15 13:43:27',
          status: 'Finished',
          action: 'View logs',
        },
      ],
    };
  },
};
</script>
