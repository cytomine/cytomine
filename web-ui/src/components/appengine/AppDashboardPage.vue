<template>
  <div class="content-wrapper">
    <div class="panel">
      <p class="panel-heading">{{ $t('app-dashboard') }}</p>

      <section class="panel-block">
        <b-table
          :current.sync="currentPage"
          :data="taskRuns"
          :paginated="true"
          :per-page="perPage"
          detailed
          @details-open="onDetailsOpen"
        >
          <template #default="{ row: run }">
            <b-table-column :label="$t('app-name')">
              <router-link :to="`/apps/${run.task.namespace}/${run.task.version}`">
                {{ run.task.name }} ({{ run.task.version }})
              </router-link>
            </b-table-column>

            <b-table-column :label="$t('launched-by')" field="user.username" sortable>
              {{ run.user.username }}
            </b-table-column>

            <b-table-column :label="$t('execution-date')" field="createdAt" sortable>
              {{ formatDate(run.createdAt) }}
            </b-table-column>

            <b-table-column :label="$t('status')" field="state" centered sortable>
              <span class="tag" :class="stateClass(run.state)">
                {{ run.state }}
              </span>
            </b-table-column>

            <b-table-column :label="$t('actions')" centered>
              <div class="buttons is-centered">
                <template v-if="!run.isTerminalState()">
                  <button class="button is-small is-danger is-light" @click="handleCancel(run)">
                    {{ $t('button-cancel') }}
                  </button>
                </template>

                <template v-else>
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

          <template #detail="{ row: run }">
            <div class="columns">
              <div class="column">
                <h2>Inputs</h2>
                <b-loading :is-full-page="false" :active="run.inputs === null" />
                <task-run-parameters-table :parameters="run.inputs" :project-id="run.project" type="input"/>
              </div>
              <div class="column">
                <h2>Outputs</h2>
                <b-loading :is-full-page="false" :active="run.outputs === null" />
                <task-run-parameters-table :parameters="run.outputs" :project-id="run.project" type="outputs"/>
              </div>
            </div>
          </template>
        </b-table>
      </section>
    </div>
  </div>
</template>

<script>
import Task from '@/utils/appengine/task';
import TaskRun from '@/utils/appengine/task-run';
import TaskRunParametersTable from '@/components/appengine/task-run/TaskRunParametersTable';
import {get} from '@/utils/store-helpers';

export default {
  name: 'AppDashboardPage',
  components: {
    TaskRunParametersTable,
  },
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
    },
    async onDetailsOpen(taskRun) {
      if (!taskRun.inputs) {
        await taskRun.fetchInputs();
      }

      if (!taskRun.outputs) {
        await taskRun.fetchOutputs();
      }
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
        onConfirm: () => run.delete(),
      });
    },
  },
  async created() {
    await this.fetchTaskRuns();
  },
};
</script>
