<template>
  <form>
    <cytomine-modal :active="active" :title="$t('app-engine.ae-run-task')" @close="close">
      <div class="card-content">
        <section class="content">
          <b-field :label="$t('app-engine.form.select-task')">
            <b-select v-model="selectedTask" :placeholder="$t('app-engine.no-task-selected')" expanded>
              <option v-for="task in tasks" :key="`${task.namespace}/${task.version}`" :value="task">
                {{ task.name }} ({{ task.version }})
              </option>
            </b-select>
          </b-field>
        </section>

        <section class="content" v-if="selectedTask">
          <task-io-form
            v-on:appengine:task:started="handleRunTask"
            :task="selectedTask"
            :project-id="currentProject.id"
          />
        </section>
      </div>
      <template #footer>
        <b-button @click="close">{{ $t('button-cancel') }}</b-button>
        <b-button class="is-link">{{ $t('app-engine.ae-run-task') }}</b-button>
      </template>
    </cytomine-modal>
  </form>
</template>

<script>
import CytomineModal from '@/components/utils/CytomineModal';
import TaskIoForm from '@/components/appengine/forms/TaskIoForm';
import TaskRun from '@/utils/appengine/task-run';
import {get} from '@/utils/store-helpers';

export default {
  name: 'TaskModal',
  props: {
    active: {type: Boolean, default: false},
    tasks: {type: Array, default: () => []},
  },
  components: {
    CytomineModal,
    TaskIoForm,
  },
  data() {
    return {
      selectedTask: null,
    };
  },
  computed: {
    currentProject: get('currentProject/project'),
  },
  methods: {
    close() {
      this.selectedTask = null;
      this.$emit('update:active', false);
    },
    async handleRunTask(event) {
      let taskRun = new TaskRun(event.resource);
      taskRun.project = this.currentProjectId;
      this.$emit('run-task', taskRun);
    },
  },
};
</script>
