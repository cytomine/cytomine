<template>
  <div>
    <AppEngineField
      v-for="input in taskInputs"
      v-model="inputs[input.name].value"
      :key="input.id"
      :parameter="input"
    />
  </div>
</template>

<script>
import Vue from 'vue';

import AppEngineField from '@/components/appengine/forms/fields/AppEngineField';
import Task from '@/utils/appengine/task';
import {hasBinaryType} from '@/utils/app';

export default {
  name: 'TaskInputForm',
  components: {
    AppEngineField,
  },
  props: {
    task: {type: Object, required: true},
  },
  data() {
    return {
      taskInputs: [],
      inputs: {},
      hasBinaryData: false,
    };
  },
  watch: {
    async task() {
      await this.fetchTaskInputs();
    }
  },
  methods: {
    async fetchTaskInputs() {
      let inputs = await Task.fetchTaskInputs(this.task.namespace, this.task.version);

      this.taskInputs = inputs.sort((a, b) => {
        return a.name < b.name ? -1 : (a.name === b.name ? 0 : 1);
      });

      this.resetForm();
    },
    resetForm() {
      this.inputs = {};

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
  },
  async created() {
    await this.fetchTaskInputs();
    await this.task.fetchOutputs();
  },
};
</script>
