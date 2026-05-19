<template>
  <b-table :data="parameters" narrowed>
    <template #default="props">
      <b-table-column field="name" :label="$t('app-engine.parameter.name')">
        {{ props.row.parameterName }}
      </b-table-column>

      <b-table-column field="type" :label="$t('app-engine.parameter.type')">
        {{ props.row.type }}
      </b-table-column>

      <b-table-column
        v-if="['FILE', 'IMAGE'].includes(props.row.type)"
        field="value"
        :label="$t('app-engine.parameter.value')"
      >
        <button class="button is-link is-small" @click="downloadFile(props.row)">{{ $t('button-download') }}</button>
      </b-table-column>

      <b-table-column
        v-else-if="props.row.type === 'GEOMETRY'"
        field="value"
        :label="$t('app-engine.parameter.value')"
      >
        <button class="button is-link is-small" @click="downloadGeometry(props.row)">{{ $t('button-download')}}</button>
      </b-table-column>

      <b-table-column v-else field="value" :label="$t('app-engine.parameter.value')">
        {{ props.row.value }}
      </b-table-column>
    </template>
  </b-table>
</template>

<script>
import {Cytomine} from '@/api';
import {updateToken} from '@/utils/token-utils';

export default {
  name: 'TaskRunParametersTable',
  props: {
    parameters: {type: Array, default: () => []},
    projectId: {type: Number, required: true},
    type: {type: String, required: true}
  },
  methods: {
    async download(data, name) {
      const cytomine = Cytomine.instance;
      const token = await updateToken();
      let url = `${cytomine.host}${cytomine.basePath}app-engine/project/${this.projectId}/task-runs/${data.taskRunId}/${this.type}/${name}?auth=${token}`;
      let link = document.createElement('a');
      link.href = url;
      link.download = name;
      link.style.display = 'none';

      document.body.appendChild(link);
      link.click();
    
      document.body.removeChild(link);
    },
    downloadFile(output) {
      this.download(output, output.parameterName);
    },
    downloadGeometry(output) {
      this.download(output,`${output.parameterName}.geojson`);
    },
  },
};
</script>
