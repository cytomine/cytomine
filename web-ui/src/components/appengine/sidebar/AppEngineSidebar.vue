<template>
  <div class="whole-sidebar">
    <div class="card executor">
      <header class="card-header">
        <p class="card-header-title subtitle">
          <i class="fas fa-cogs"/> {{ $t("app-engine.execute-a-task") }}
        </p>
      </header>

      <div class="card-content">
        <section class="content">
          <b-field :label="$t('app-engine.form.select-task')">
            <b-select v-model="selectedTask" expanded>
              <option v-for="task in tasks" :key="`${task.namespace}/${task.version}`" :value="task">
                {{ task.name }} ({{ task.version }})
              </option>
            </b-select>
          </b-field>
        </section>

        <section class="content" v-if="selectedTask">
          <b-field :label="$t('app-engine.task.name')">
            <p>{{ selectedTask.name }} ({{ selectedTask.version }})</p>
          </b-field>
          <b-field :label="$t('description')">
            <p v-if="selectedTask.description && selectedTask.description.length > 0">
              {{ selectedTask.description }}
            </p>
            <p class="no-description"><em>{{ $t('app-engine.task.no-description') }}</em></p>
          </b-field>
        </section>

        <section class="content" v-if="selectedTask">
          <task-io-form
            v-on:appengine:task:started="catchTaskRunLaunch"
            :task="selectedTask"
            :project-id="currentProjectId"
          />
        </section>
      </div>
    </div>

    <div class="card runs">
      <header class="card-header">
        <p class="card-header-title subtitle">
          <i class="fas fa-clock"/> {{ $t("app-engine.previous-task-runs") }}
        </p>
      </header>

      <div class="card-content">
        <section class="content">
          <h5 class="subtitle">{{ $t('app-engine.runs.title') }}</h5>
        </section>
        <task-run-table :task-runs="trackedTaskRuns"/>
      </div>
    </div>
  </div>
</template>

<script>
import Task from '@/utils/appengine/task';
import TaskRun from '@/utils/appengine/task-run';
import TaskIoForm from '@/components/appengine/forms/TaskIoForm';
import TaskRunTable from '@/components/appengine/task-run/TaskRunTable';
import {get} from '@/utils/store-helpers';

export default {
  name: 'AppEngineSideBar',
  components: {
    TaskIoForm,
    TaskRunTable,
  },
  data() {
    return {
      selectedTask: null,
      tasks: [],
      trackedTaskRuns: []
    };
  },
  async created() {
    await this.fetchTasks();
    await this.fetchTaskRuns();

    setInterval(async () => {
      for (let taskRun of this.trackedTaskRuns) {
        if (!taskRun.isTerminalState()) {
          await taskRun.fetch();
        }

        if (taskRun.isFinished()) {
          if (!taskRun.outputs) {
            await taskRun.fetchOutputs();
          }

          if (taskRun.outputs.some(output => output.type === 'GEOMETRY')) {
            this.$eventBus.$emit('annotation-layers:refresh');
          }
        }
      }
    }, 2000);
  },
  computed: {
    currentProject: get('currentProject/project'),
    currentProjectId() {
      return this.currentProject.id;
    },
  },
  methods: {
    async catchTaskRunLaunch(event) {
      let taskRun = new TaskRun(event.resource);
      taskRun.project = this.currentProjectId;
      await taskRun.fetchInputs();
      this.trackedTaskRuns = [taskRun, ...this.trackedTaskRuns];
    },
    async fetchTasks() {
      this.tasks = await Task.fetchAll();
    },
    async fetchTaskRuns() {
      let taskRuns = await TaskRun.fetchByProject(this.currentProjectId);
      taskRuns.sort((a, b) => new Date(b.created_at) - new Date(a.created_at));

      this.trackedTaskRuns = await Promise.all(
        taskRuns.map(async ({project, taskRunId}) => {
          let taskRun = await Task.fetchTaskRunStatus(this.currentProjectId, taskRunId);
          return new TaskRun({...taskRun, project});
        })
      );

      await Promise.all(this.trackedTaskRuns.map(run => run.fetchInputs()));

      await Promise.all(
        this.trackedTaskRuns
          .filter(taskRun => taskRun.isFinished())
          .map(run => run.fetchOutputs())
      );

      // Mark all previous runs as failed if not finished
      this.trackedTaskRuns.forEach(taskRun => {
        if (!taskRun.isFinished()) {
          taskRun.state = TaskRun.STATES.FAILED;
        }
      });
    },
  },
};
</script>

<style lang="scss" scoped>
$background: #888;
$color: white;
$hoverBackground: #82aad8;
$hoverColor: white;
$border: #383838;

// ---- Header ----
.card-header {
  width: 100%;
  height: 3.5rem;
  background: $background;
}

.card-header-title {
  color: $color;
  position: relative;
  font-size: 0.85rem;
  height: 100%;
  text-decoration: none;
  display: flex;
  align-items: center;
}

.card-header-title:hover {
  color: $hoverColor;
}

.card-header:hover {
  color: $hoverColor;
  background: $hoverBackground !important;
}

// for icons

.card-header-icon {
  color: $color;
}

.fas, .far {
  font-size: 1.25rem;
  width: 4rem;
  text-align: center;
  flex-shrink: 0;
}

.whole-sidebar {
  background-color: white;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.executor, .runs {
  flex: 0 0 50%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;

  .card-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow-x: hidden;
    overflow-y: auto;
    padding: 1.5rem;
    min-height: 0;
  }
}

.runs {
  height: 50%;
}

.executor {
  height: 50%;
}
</style>
