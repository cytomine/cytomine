<template>
  <div class="content-wrapper">
    <div class="panel">
      <p class="panel-heading">{{ $t('app-dashboard') }}</p>

      <section class="panel-block">
        <b-table :data="taskRuns" :current.sync="currentPage" :paginated="true" :per-page="perPage">
          <template #default="{ row: run }">
            <b-table-column :label="$t('app-name')">
              <router-link :to="`/apps/${run.task.namespace}/${run.task.version}`">
                {{ run.task.name }} ({{ run.task.version }})
              </router-link>
            </b-table-column>

            <b-table-column :label="$t('launched-by')">
              {{ run.user }}
            </b-table-column>

            <b-table-column :label="$t('execution-date')">
              {{ formatDate(run.createdAt) }}
            </b-table-column>

            <b-table-column :label="$t('status')" centered>
              <span class="tag" :class="stateClass(run.state)">
                {{ run.state }}
              </span>
            </b-table-column>

            <b-table-column :label="$t('actions')" centered>
              <div class="buttons is-centered">
                <template
                  v-if="['created', 'provisioned', 'queuing', 'queued', 'running', 'pending'].includes(run.state.toLowerCase())">
                  <button class="button is-small is-danger is-light" @click="handleCancel(run)">
                    {{ $t('button-cancel') }}
                  </button>
                </template>

                <template v-else-if="['finished', 'failed'].includes(run.state.toLowerCase())">
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
import Task from '@/utils/appengine/task';
import TaskRun from '@/utils/appengine/task-run';
import {get} from '@/utils/store-helpers';

export default {
  name: 'AppDashboardPage',
  data() {
    return {
      taskRuns: [],
      currentPage: 1,
      perPage: 10,
    };
  },
  computed: {
    currentProject: get('currentProject/project'),
  },
  methods: {
    async fetchTaskRuns() {
      let taskRuns = await TaskRun.fetchByProject(this.currentProject.id);
      taskRuns.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

      this.taskRuns = await Promise.all(
        taskRuns.map(async ({project, taskRunId, user}) => {
          let taskRun = await Task.fetchTaskRunStatus(this.currentProject.id, taskRunId);
          return new TaskRun({...taskRun, project, user});
        })
      );

      console.log(this.taskRuns, taskRuns);
    },
    formatDate(date) {
      return new Intl.DateTimeFormat(
        undefined,
        {
          day: '2-digit',
          month: 'short',
          year: 'numeric',
          hour: '2-digit',
          minute: '2-digit',
        }
      ).format(new Date(date));
    },
    stateClass(state) {
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
      return map[state.toLowerCase()] ?? 'is-light';
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
  async created() {
    await this.fetchTaskRuns();
  },
};
</script>
