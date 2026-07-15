<template>
  <div class="content-wrapper">
    <div class="box">
      <h1>{{ $t('user-history') }}</h1>

      <b-table
        :data="commands"
        :loading="loading"
        paginated
        backend-pagination
        backend-sorting
        :total="total"
        :per-page="perPage"
        :current-page.sync="currentPage"
        :default-sort="[sortField, sortOrder]"
        pagination-size="is-small"
        @sort="onSort"
      >
        <template #default="{row}">
          <b-table-column field="created" :label="$t('date')" sortable>
            {{ formatDate(row.created) }}
          </b-table-column>

          <b-table-column :label="$t('operation')">
            <b-tag :type="operationTag(row)">{{ operationLabel(row) }}</b-tag>
          </b-table-column>

          <b-table-column :label="$t('domain')">
            {{ domainLabel(row) }}
          </b-table-column>

          <b-table-column :label="$t('description')">
            {{ description(row) }}
          </b-table-column>

          <b-table-column :label="$t('actions')" centered>
            <b-button
              size="is-small"
              icon-left="undo"
              :loading="undoing === row.id"
              @click="undo(row)"
            >
              {{ $t('undo') }}
            </b-button>
          </b-table-column>
        </template>

        <template #empty>
          <div class="content has-text-grey has-text-centered">
            {{ $t('no-result') }}
          </div>
        </template>

        <template #bottom-left>
          <b-select v-model="perPage" size="is-small">
            <option v-for="option in perPageOptions" :key="option" :value="option">
              {{ $t('count-per-page', {count: option}) }}
            </option>
          </b-select>
        </template>
      </b-table>
    </div>
  </div>
</template>

<script>
import {Cytomine} from '@/api';
import {formatDate} from '@/utils/date';

const OPERATION_TAGS = {
  INSERT: 'is-success',
  UPDATE: 'is-info',
  DELETE: 'is-danger',
  UNDO: 'is-warning',
};

export default {
  name: 'UserHistoryPage',
  data() {
    return {
      commands: [],
      total: 0,
      currentPage: 1,
      perPage: 20,
      perPageOptions: [10, 20, 50, 100],
      sortField: 'created',
      sortOrder: 'desc',
      loading: false,
      undoing: null,
    };
  },
  watch: {
    currentPage() {
      this.fetchCommands();
    },
    perPage() {
      if (this.currentPage === 1) {
        this.fetchCommands();
      } else {
        this.currentPage = 1;
      }
    },
  },
  methods: {
    async fetchCommands() {
      this.loading = true;
      try {
        const {data} = await Cytomine.instance.api.get(
          '/commands',
          {params: {page: this.currentPage - 1, size: this.perPage, sort: `${this.sortField},${this.sortOrder}`}},
        );
        this.commands = data.collection;
        this.total = data.size;
      } catch (error) {
        console.log(error);
        this.$notify({type: 'error', text: this.$t('unexpected-error-info-message')});
      }
      this.loading = false;
    },
    onSort(field, order) {
      this.sortField = field;
      this.sortOrder = order;
      if (this.currentPage === 1) {
        this.fetchCommands();
      } else {
        this.currentPage = 1;
      }
    },
    async undo(command) {
      this.undoing = command.id;
      try {
        await Cytomine.instance.api.post(`/commands/undo/${command.id}`);
        this.$notify({type: 'success', text: this.$t('notify-success-undo')});
        await this.fetchCommands();
      } catch (error) {
        console.log(error);
        this.$notify({type: 'error', text: this.$t('notify-error-undo')});
      }
      this.undoing = null;
    },
    parseType(commandType) {
      const tokens = commandType.replace(/_COMMAND$/, '').split('_');
      return {action: tokens[0], domain: tokens.slice(1)};
    },
    operationTag(command) {
      const {action} = this.parseType(command.commandRequest.commandType);
      return OPERATION_TAGS[action] || 'is-light';
    },
    operationLabel(command) {
      const {action} = this.parseType(command.commandRequest.commandType);
      return this.humanize([action]);
    },
    domainLabel(command) {
      const request = command.commandRequest;
      let {action, domain} = this.parseType(request.commandType);
      if (action === 'UNDO' && request.target) {
        domain = this.parseType(request.target.commandType).domain;
      }
      return this.humanize(domain);
    },
    description(command) {
      const request = command.commandRequest;
      const payload = request.after || request.before
        || (request.target && (request.target.after || request.target.before))
        || {};
      return payload.name || payload.originalFilename || payload.filename || (payload.id ? `#${payload.id}` : '');
    },
    formatDate(date) {
      return formatDate(date, this.$i18n.locale);
    },
    humanize(tokens) {
      const sentence = tokens.join(' ').toLowerCase();
      return sentence.charAt(0).toUpperCase() + sentence.slice(1);
    },
  },
  created() {
    this.fetchCommands();
  },
};
</script>
