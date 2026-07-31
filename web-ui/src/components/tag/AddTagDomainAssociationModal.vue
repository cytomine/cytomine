<template>
<cytomine-modal-card :title="$t('associate-tags')" active>
  <b-loading :is-full-page="false" :model-value="loading" class="small" />
  <template v-if="!loading">
    <b-field>
      <domain-tag-input v-model="selectedTags" :domains="notAssociatedTags" placeholder="search-or-create-tag" allowNew />
    </b-field>
  </template>

  <template #footer>
    <button class="button" @click="$parent.close()">
      {{$t('button-cancel')}}
    </button>
    <button class="button is-link" @click="addAssociations">
      {{$t('button-add')}}
    </button>
  </template>
</cytomine-modal-card>
</template>

<script>

import { TagCollection } from '@/api';
import DomainTagInput from '@/components/utils/DomainTagInput.vue';
import CytomineModalCard from '@/components/utils/CytomineModalCard.vue';

export default {
  name: 'add-tag-modal',
  props: {
    associatedTags: Array
  },
  components: {
    CytomineModalCard,
    DomainTagInput,
  },
  data() {
    return {
      loading: true,
      tags: [],
      selectedTags: []
    };
  },
  computed: {
    notAssociatedTags() {
      return this.tags.filter(tag => !this.associatedTags.map(u => u.tag).includes(tag.id));
    }
  },
  methods: {
    async addAssociations() {
      this.$emit('addObjects', this.selectedTags);
      this.$parent.close();
    },
    async fetchTags() {
      this.tags = (await TagCollection.fetchAll()).array;
    },

  },
  async created() {
    this.fetchTags();
    this.loading = false;
  }
};
</script>

<style scoped>
:deep(.modal-card), :deep(.modal-card-body) {
  overflow: visible !important;
}

</style>
