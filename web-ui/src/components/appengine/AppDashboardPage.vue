<template>
  <div class="content-wrapper">
    <div class="panel">
      <p class="panel-heading">{{ $t('app-dashboard') }}</p>

      <section class="panel-block">
        <b-table
          ref="table"
          :current.sync="currentPage"
          :data="taskRuns"
          :paginated="true"
          :per-page="perPage"
          pagination-size="is-small"
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
                  <button v-if="currentUser.username == 'admin'" class="button is-small is-danger is-light" @click="handleDelete(run)">
                    <b-icon icon="trash" class="has-text-white" />
                  </button>
                </template>
              </div>
            </b-table-column>
          </template>

          <template #detail="{ row: run }">
            <b-tabs v-model="run._activeTab" type="is-boxed" size="is-small">

              <b-tab-item :label="`${$t('app-engine.inputs.title')} / ${$t('app-engine.outputs.title')}`">
                <div class="columns">
                  <div class="column">
                    <h2>{{ $t('app-engine.inputs.title') }}</h2>
                    <b-loading :is-full-page="false" :active="run.inputs === null" />
                    <task-run-parameters-table
                      v-if="run.inputs !== null"
                      :parameters="run.inputs"
                      :project-id="run.project"
                      type="input"
                    />
                  </div>
                  <div class="column">
                    <h2>{{ $t('app-engine.outputs.title') }}</h2>
                    <b-loading :is-full-page="false" :active="run.outputs === null" />
                    <task-run-parameters-table
                      v-if="run.outputs !== null"
                      :parameters="run.outputs"
                      :project-id="run.project"
                      type="outputs"
                    />
                  </div>
                </div>
              </b-tab-item>

              <b-tab-item :label="$t('logs')">
                <b-loading :is-full-page="false" :active="run.logs === null" />
                <pre v-if="run.logs !== null" class="logs">{{ run.logs }}</pre>
                <p v-else>{{ $t('no-log-to-display') }}</p>
              </b-tab-item>
            </b-tabs>
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
    currentUser: get('currentUser/user'),
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
      this.$set(taskRun, '_activeTab', taskRun._activeTab ?? 0);

      if (!taskRun.inputs) {
        await taskRun.fetchInputs();
      }

      if (!taskRun.outputs) {
        await taskRun.fetchOutputs();
      }

      if (!taskRun.logs) {
        await taskRun.fetchLogs();
      }
    },
    formatDate(date) {
      return new Intl.DateTimeFormat(
        this.$i18n.locale,
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
    handleCancel() {
      this.$buefy.dialog.confirm({
        title: this.$t('confirm-cancellation'),
        message: this.$t('confirm-cancel-run'),
        type: 'is-danger',
        confirmText: this.$t('button-confirm'),
        cancelText: this.$t('button-cancel'),
        onConfirm: () => {},
      });
    },
    async handleViewLogs(run) {
      this.$set(run, '_activeTab', 1);
      await this.onDetailsOpen(run);
      this.$refs.table.openDetailRow(run);
    },
    handleDelete(run) {
      this.$buefy.dialog.confirm({
        title: this.$t('confirm-deletion'),
        message: this.$t('confirm-deletion-run'),
        type: 'is-danger',
        confirmText: this.$t('button-confirm'),
        cancelText: this.$t('button-cancel'),
        onConfirm: () => {
          run.delete();
          this.taskRuns = this.taskRuns.filter(tr => tr.id !== run.id);
        },
      });
    },
  },
  async created() {
    await this.fetchTaskRuns();
  },
};
</script>

<style>
.logs {
  font-size: 0.75rem;
  padding: 1rem;
  border-radius: 4px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
