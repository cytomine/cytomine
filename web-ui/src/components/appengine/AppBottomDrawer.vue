<template>
  <div class="drawer" :class="{ collapsed: isCollapsed }" @transitionend="handleTransitionEnd">
    <div class="drawer-header">
      <span class="drawer-title">{{ $t('app-engine.applications') }}</span>
      <b-button
        icon-pack="fas"
        :icon-left="isCollapsed ? 'chevron-up' : 'chevron-down'"
        @click.stop="toggleCollapse"
      />
    </div>

    <div class="drawer-body">
      <div class="drawer-body-inner">
        <div class="analysis-layout">
          <div class="analysis-column selector-column">
            <h3 class="column-title">{{ $t("app-engine.execute-a-task") }}</h3>

            <b-select v-model="selectedTask" expanded :placeholder="$t('app-engine.form.select-task')">
              <option v-for="task in tasks" :key="`${task.namespace}/${task.version}`" :value="task">
                {{ task.name }} ({{ task.version }})
              </option>
            </b-select>

            <div v-if="selectedTask" class="selected-app-card">
              <strong>{{ selectedTask.name }} ({{ selectedTask.version }})</strong>
              <p v-if="selectedTask.description && selectedTask.description.length > 0">
                {{ selectedTask.description }}
              </p>
              <p v-else class="no-description"><em>{{ $t('app-engine.task.no-description') }}</em></p>
            </div>
          </div>

          <div class="analysis-column">
            <div class="inputs-header">
              <h3 class="column-title">{{ $t('app-engine.inputs.title') }}</h3>
              <b-button v-if="selectedTask" @click="resetInputs">
                {{ $t('button-clear') }}
              </b-button>
            </div>

            <TaskInputForm
              v-if="selectedTask"
              :inputs="inputs"
              :task="selectedTask"
              @input="inputs = $event"
            />

            <b-button
              class="start-button"
              type="is-primary"
              size="is-medium"
              expanded
              :disabled="!selectedTask || isRunning"
              :loading="isRunning"
              @click="runTask"
            >
              {{ $t('app-engine.ae-run-task') }}
            </b-button>
          </div>

          <div class="analysis-column">
            <h3 class="column-title">{{ $t('run-status') }}</h3>

            <TaskRunTable :task-runs="allTaskRuns"/>

            <div class="has-text-right mt-3">
              <b-button type="is-text">{{ $t('app-engine.see-recent-runs') }}</b-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Task from '@/utils/appengine/task';
import TaskInputForm from '@/components/appengine/forms/TaskInputForm';
import TaskRun from '@/utils/appengine/task-run';
import TaskRunTable from '@/components/appengine/task-run/TaskRunTable';
import {get} from '@/utils/store-helpers';

export default {
  name: 'AppBottomDrawer',
  components: {
    TaskInputForm,
    TaskRunTable,
  },
  data() {
    return {
      isCollapsed: true,
      selectedTask: null,
      tasks: [],
      allTaskRuns: [],
      trackedTaskRuns: [],
      inputs: {},
      isRunning: false,
    };
  },
  computed: {
    currentProject: get('currentProject/project'),
    activeImage() {
      let index = this.$store.getters['currentProject/currentViewer'].activeImage;
      return this.$store.getters['currentProject/currentViewer'].images[index].imageInstance;
    }
  },
  async created() {
    await this.fetchTasks();
    await this.fetchTaskRuns();

    setInterval(async () => {
      for (let taskRun of this.trackedTaskRuns) {
        if (!taskRun.isTerminalState()) {
          await taskRun.fetch();
        }

        if (taskRun.isTerminalState() && this.getTask(taskRun).hasGeometryOutput()) {
          await taskRun.fetchOutputs();
          this.$eventBus.$emit('annotation-layers:refresh');
        }
      }

      this.trackedTaskRuns = this.trackedTaskRuns.filter(taskRun => !taskRun.isTerminalState());
    }, 2000);
  },
  mounted() {
    const parsed = parseInt(this.defaultHeight);
    if (!isNaN(parsed) && this.defaultHeight.endsWith('px')) {
      this.currentHeight = parsed;
    } else {
      this.currentHeight = window.innerHeight * (parseInt(this.defaultHeight) / 100);
    }
  },
  methods: {
    async fetchTasks() {
      this.tasks = await Task.fetchAll();
    },
    async fetchTaskRuns() {
      let taskRuns = await TaskRun.fetchByProject(this.currentProject.id);
      taskRuns.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

      this.allTaskRuns = await Promise.all(
        taskRuns.map(async ({project, taskRunId}) => {
          let taskRun = await Task.fetchTaskRunStatus(this.currentProject.id, taskRunId);
          return new TaskRun({...taskRun, project});
        })
      );
    },
    toggleCollapse() {
      this.isCollapsed = !this.isCollapsed;
      this.$emit('collapse', this.isCollapsed);
      this.$nextTick(() => {
        this.$eventBus.$emit('updateMapSize');
      });
    },
    handleTransitionEnd(event) {
      if (!this.isDragging && event.propertyName === 'height') {
        this.$eventBus.$emit('updateMapSize');
      }
    },
    resetInputs() {
      this.inputs = {};
    },
    getInputProvisions() {
      return Object.entries(this.inputs).map(([parameterName, {type, value}]) => ({
        'param_name': parameterName,
        type,
        value,
      }));
    },
    async runTask() {
      try {
        this.isRunning = true;

        let taskRun = await Task.createTaskRun(
          this.currentProject.id,
          this.selectedTask.namespace,
          this.selectedTask.version,
          this.activeImage.id,
        );

        if (this.hasBinaryData) {
          for (const provision of this.getInputProvisions()) {
            let body = provision;
            if (provision.type === 'file') {
              body = new FormData();
              body.append('file', provision.value, provision.value.name || 'uploaded-file');
            }
            await Task.singleProvisionTask(
              this.currentProject.id,
              taskRun.id,
              provision.param_name,
              body,
            );
          }
        } else {
          await Task.batchProvisionTask(this.currentProject.id, taskRun.id, this.getInputProvisions());
        }

        await Task.runTask(this.currentProject.id, taskRun.id).then(async (event) => {
          this.$buefy.toast.open({message: this.$t('app-engine.run.started'), type: 'is-success'});
          this.resetInputs();
          this.$emit('appengine:task:started', event);
        });
      } catch (e) {
        const serverError = e.response && e.response.data
          ? (e.response.data.message || e.response.data.errorCode)
          : e.message;
        this.$buefy.toast.open({message: `Error : ${serverError}`, type: 'is-danger', indefinite: true});
      }

      this.isRunning = false;
    },
  },
};
</script>

<style scoped>
.drawer {
  position: relative;
  width: 100%;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: height 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  max-height: 40vh;
}

.drawer.collapsed {
  height: 60px;
}

.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.625rem 1.25rem 0.75rem;
  border-bottom: 1px solid #f0f0f0;
}

.drawer-title {
  font-size: 1rem;
  font-weight: 600;
  color: #111827;
}

.drawer-body {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

.drawer-body-inner {
  padding: 1rem 1.25rem 1.5rem;
}

.analysis-layout {
  display: grid;
  grid-template-columns: 25rem 1fr 35rem;
  gap: 1.5rem;
  height: 100%;
}

.analysis-column {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 1rem;
  background: #fafafa;
  min-height: 0;
}

.column-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 1rem;
  color: #1f2937;
}

.selected-app-card {
  margin-top: 1rem;
  padding: 12px;
  border-radius: 0.5rem;
  background: #ffffff;
  border: 1px solid #e5e7eb;
}

.selected-app-card p {
  margin-top: 0.5rem;
  font-size: 13px;
  color: #6b7280;
}

.inputs-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.start-button {
  margin-top: 20px;
}

@media (max-width: 1024px) {
  .analysis-layout {
    grid-template-columns: 1fr;
  }
}
</style>
