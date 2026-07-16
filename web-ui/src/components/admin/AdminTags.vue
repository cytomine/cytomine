<template>
  <div>
    <b-message v-if="error" type="is-danger" has-icon icon-size="is-small">
      <h2> {{ $t('error') }} </h2>
      <p> {{ $t('unexpected-error-info-message') }} </p>
    </b-message>
    <template v-else>
      <div class="columns">
        <div class="column is-one-quarter">
          <b-input v-model="searchString" :placeholder="$t('search-placeholder')" type="search" icon="search" />
        </div>

        <div class="column is-one-half has-text-right-desktop">
          <button class="button is-link" @click="startTagCreation()">
            {{$t('button-new-tag')}}
          </button>
        </div>
      </div>

      <b-table
        :current-page.sync="currentPage"
        :data="filteredTags"
        :default-sort="[sortField, sortOrder]"
        :loading="loading"
        :per-page="perPage"
        :total="total"
        backend-pagination
        backend-sorting
        paginated
        pagination-size="is-small"
        @sort="onSort"
      >
        <template #default="{row: tag}">
          <b-table-column field="name" :label="$t('name')" sortable>
            {{ tag.name }}
          </b-table-column>

          <b-table-column field="creatorName" :label="$t('creator')" sortable>
            {{ tag.creatorName }}
          </b-table-column>

          <b-table-column field="created" :label="$t('created')" sortable>
            {{ formatDate(tag.created) }}
          </b-table-column>

          <b-table-column label=" " centered>
            <div class="buttons">
              <button class="button is-small is-link" @click="startTagEdition(tag)">
                {{ $t('button-edit') }}
              </button>
              <button class="button is-small is-danger" @click="deleteTagDialog(tag)">
                {{ $t('button-delete') }}
              </button>
            </div>
          </b-table-column>
        </template>

        <template #empty>
          <div class="content has-text-grey has-text-centered">
            <p>{{ $t('no-tag-fitting-criteria') }}</p>
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

      <tag-modal :active.sync="modal" :tag="editedTag" @addTag="addTag" @updateTag="updateTag" />
    </template>
  </div>
</template>

<script>
import {Cytomine} from '@/api';
import {formatDate} from '@/utils/date';
import {getWildcardRegexp} from '@/utils/string-utils';
import TagModal from '@/components/tag/TagModal';

export default {
  name: 'admin-tags',
  components: {
    TagModal
  },
  data() {
    return {
      loading: true,
      error: false,
      tags: [],
      total: 0,
      currentPage: 1,
      perPage: 25,
      perPageOptions: [10, 20, 50, 100],
      sortField: 'created',
      sortOrder: 'desc',
      searchString: '',
      addTagModal: false,
      modal: false,
      editedTag: null
    };
  },
  computed: {
    regexp() {
      return getWildcardRegexp(this.searchString);
    },
    filteredTags() {
      if (!this.searchString) {
        return this.tags;
      }

      return this.tags.filter(ts => this.regexp.test(ts.name));
    }
  },
  watch: {
    currentPage() {
      this.fetchTags();
    },
    perPage() {
      if (this.currentPage === 1) {
        this.fetchTags();
      } else {
        this.currentPage = 1;
      }
    },
  },
  methods: {
    formatDate(date) {
      return formatDate(date, this.$i18n.locale);
    },
    async fetchTags() {
      this.loading = true;
      try {
        const {data} = await Cytomine.instance.api.get(
          '/tag.json',
          {params: {page: this.currentPage - 1, size: this.perPage, sort: `${this.sortField},${this.sortOrder}`}},
        );
        this.tags = data.collection;
        this.total = data.size;
      } catch (error) {
        console.log(error);
        this.error = true;
        this.$notify({type: 'error', text: this.$t('notify-error-fetch-tag')});
      }
      this.loading = false;
    },
    onSort(field, order) {
      this.sortField = field;
      this.sortOrder = order;
      if (this.currentPage === 1) {
        this.fetchTags();
      } else {
        this.currentPage = 1;
      }
    },
    startTagCreation() {
      this.editedTag = null;
      this.modal = true;
    },
    addTag(tag) {
      this.tags.push(tag);
    },
    startTagEdition(tag) {
      this.editedTag = tag;
      this.modal = true;
    },
    updateTag(tag) {
      this.editedTag.populate(tag);
    },

    deleteTagDialog(tag) {
      this.$buefy.dialog.confirm({
        title: this.$t('delete'),
        message: this.$t('delete-tag-confirmation-message', {tagName: tag.name}),
        type: 'is-danger',
        confirmText: this.$t('button-confirm'),
        cancelText: this.$t('button-cancel'),
        onConfirm: () => this.deleteTag(tag)
      });
    },
    deleteTag(tag) {
      try {
        tag.delete();
        this.tags.splice(this.tags.indexOf(tag), 1);
        this.$notify({
          type: 'success',
          text: this.$t('notif-success-tag-delete', {tagName: tag.name})
        });
      } catch (error) {
        console.log(error);
        this.$notify({
          type: 'error',
          text: this.$t('notif-error-tag-delete', {tagName: this.currentTag.name})
        });
      }
    },
  },
  created() {
    this.fetchTags();
  }
};
</script>
