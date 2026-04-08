<template>
  <div class="content-wrapper">
    <div class="panel">
      <p class="panel-heading">{{ $t('app-dashboard') }}</p>

      <section class="panel-block">
        <b-table :data="data" :columns="columns"></b-table>
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
          appName: 'Stardist (1.0.0)',
          user: 'John Doe',
          created: '2016-10-15 13:43:27',
          status: 'Finished',
          action: 'View logs',
        },
      ],
      columns: [
        {
          field: 'appName',
          label: 'App Name',
        },
        {
          field: 'user',
          label: 'Launched By',
        },
        {
          field: 'created',
          label: 'Execution Date',
        },
        {
          field: 'status',
          label: 'Status',
          centered: true,
        },
        {
          field: 'action',
          label: 'Action',
          centered: true,
        },
      ],
    };
  },
};
</script>
