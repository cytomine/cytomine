<template>
  <div>
    <h3 class="subtitle">{{ $t('app-engine.ae-run-task') }}</h3>
    <section class="fields">
      <app-engine-field
        v-for="input in taskInputs"
        v-model="inputs[input.name].value"
        :key="input.id"
        :parameter="input"
      />
    </section>
    <section>
      <b-field class="buttons" grouped>
        <b-button type="is-primary" @click="resetForm">{{ $t('button-clear') }}</b-button>
        <b-button type="is-primary" @click="runTask">{{ $t('app-engine.ae-run-task') }}</b-button>
      </b-field>
    </section>
  </div>
</template>

<script>
import Vue from 'vue';

import AppEngineField from '@/components/appengine/forms/fields/AppEngineField';
import Task from '@/utils/appengine/task';
import {hasBinaryType} from '@/utils/app';

export default {
  name: 'task-io-form',
  components: {
    AppEngineField,
  },
  props: {
    projectId: {type: Number, required: true},
    task: {type: Object, required: true},
  },
  data() {
    return {
      taskInputs: [],
      inputs: {},
      hasBinaryData: false,
    };
  },
  computed: {
    activeImage() {
      let index = this.$store.getters['currentProject/currentViewer'].activeImage;
      return this.$store.getters['currentProject/currentViewer'].images[index].imageInstance;
    }
  },
  async created() {
    await this.fetchTaskInputs();
  },
  watch: {
    async task() {
      await this.fetchTaskInputs();
    }
  },
  methods: {
    async fetchTaskInputs() {
      let inputs = await Task.fetchTaskInputs(this.task.namespace, this.task.version);

      // sort inputs lexicographically
      this.taskInputs = inputs.sort((a, b) => {
        return a.name < b.name ? -1 : (a.name === b.name ? 0 : 1);
      });

      this.resetForm();
    },
    async runTask() {
      // create task run and provision
      try {
        let taskRun = await Task.createTaskRun(
          this.projectId,
          this.task.namespace,
          this.task.version,
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
              this.projectId,
              taskRun.id,
              provision.param_name,
              body,
            );
          }
        } else {
          await Task.batchProvisionTask(this.projectId, taskRun.id, this.getInputProvisions());
        }

        await Task.runTask(this.projectId, taskRun.id).then(async (taskRun) => {
          this.$buefy.toast.open({message: this.$t('app-engine.run.started'), type: 'is-success'});
          this.resetForm();
          this.$emit('appengine:task:started', taskRun);
        });
      } catch (e) {
        const serverError = e.response && e.response.data
          ? (e.response.data.message || e.response.data.errorCode)
          : e.message;
        this.$buefy.toast.open({message: `Error : ${serverError}`, type: 'is-danger', indefinite: true});
      }
    },
    getInputProvisions() {
      let provisions = [];
      for (let [paramName, value] of Object.entries(this.inputs)) {
        provisions.push({
          'param_name': paramName,
          'type': value.type,
          'value': value.value,
        });
      }
      return provisions;
    },
    resetForm() {
      const setDefaultValue = (input) => {
        const value = (() => {
          if (input.default === 'null') {
            return null;
          }

          switch (input.type.id) {
            case 'boolean':
              return input.default === 'true';
            case 'integer':
              return parseInt(input.default);
            case 'number':
              return parseFloat(input.default);
            default:
              return input.default;
          }
        })();

        Vue.set(this.inputs, input.name, {value, type: input.type});
      };

      for (let input of this.taskInputs) {
        setDefaultValue(input);
        if (hasBinaryType(input)) {
          this.hasBinaryData = true;
        }
      }
    }
  }
};
</script>

<style scoped>
.buttons {
  display: flex;
  justify-content: flex-end;
}

.button {
  margin-left: 5px;
}

.fields {
  padding-top: 6px;
}
</style>
