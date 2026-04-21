<template>
  <div class="drawer" :style="drawerStyle" @transitionend="handleTransitionEnd">
    <div class="drawer-handle-area">
      <div class="drawer-header">
        <span class="drawer-title">{{ $t('app-engine.applications') }}</span>
        <b-button
          icon-pack="fas"
          :icon-left="isCollapsed ? 'chevron-up' : 'chevron-down'"
          @click.stop="toggleCollapse"
        />
      </div>
    </div>

    <div class="drawer-body" :style="bodyStyle">
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
              <b-button v-if="selectedTask" @click="inputs = {}">
                {{ $t('button-clear') }}
              </b-button>
            </div>

            <TaskInputForm
              v-if="selectedTask"
              :inputs="inputs"
              :task="selectedTask"
              @input="inputs = $event"
            />

            {{ inputs }}

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
  props: {
    value: {type: Boolean, default: false},
    defaultHeight: {type: String, default: '50vh'},
    collapsedHeight: {type: String, default: '60px'},
  },
  data() {
    return {
      isCollapsed: true,
      isDragging: false,
      dragStartY: 0,
      dragStartHeight: 0,
      currentHeight: 0,

      selectedTask: null,
      tasks: [],
      allTaskRuns: [],
      trackedTaskRuns: [],
      inputs: {},

      isRunning: false,
      progress: 0,
      runStatus: 'Idle',
      progressLog: ['Waiting for analysis to start...'],
      progressTimer: null,
    };
  },
  computed: {
    currentProject: get('currentProject/project'),
    isOpen() {
      return this.value;
    },
    drawerHeight() {
      if (this.isCollapsed) {
        return this.collapsedHeight;
      }
      return this.currentHeight ? `${this.currentHeight}px` : this.defaultHeight;
    },
    drawerStyle() {
      return {
        height: this.drawerHeight,
        width: '100%',
      };
    },
    bodyStyle() {
      return {
        flex: 1,
        minHeight: 0,
        overflow: 'auto',
      };
    },
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
  beforeDestroy() {
    clearInterval(this.progressTimer);
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
    close() {
      this.$emit('input', false);
      this.$emit('close');
    },
    open() {
      this.$emit('input', true);
      this.$emit('open');
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
    handleTaskExecution() {

    },
    runTask() {
      clearInterval(this.progressTimer);

      this.isRunning = true;
      this.progress = 0;
      this.runStatus = 'Starting';
      this.progressLog = [
        `Launching ${this.selectedAppLabel}...`,
      ];

      this.progressTimer = setInterval(() => {
        if (this.progress >= 100) {
          clearInterval(this.progressTimer);
          this.isRunning = false;
          this.runStatus = 'Completed';
          this.progressLog.push('Analysis finished successfully.');
          return;
        }

        this.progress += 10;

        if (this.progress < 40) {
          this.runStatus = 'Preparing inputs';
        } else if (this.progress < 80) {
          this.runStatus = 'Running analysis';
        } else {
          this.runStatus = 'Finalizing';
        }

        this.progressLog.push(`${this.runStatus} (${this.progress}%)`);
      }, 700);
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
  max-height: 92vh;
}

.drawer-handle-area {
  flex-shrink: 0;
  user-select: none;
  padding: 10px 20px 0;
}

.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.drawer-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.drawer-body {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

.drawer-body-inner {
  padding: 16px 20px 24px;
}

.analysis-layout {
  display: grid;
  grid-template-columns: 260px 1fr 320px;
  gap: 20px;
  height: 100%;
}

.analysis-column {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  background: #fafafa;
  min-height: 0;
}

.column-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #1f2937;
}

.selected-app-card {
  margin-top: 16px;
  padding: 12px;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
}

.selected-app-card p {
  margin-top: 8px;
  font-size: 13px;
  color: #6b7280;
}

.inputs-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
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
