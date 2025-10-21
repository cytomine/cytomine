<template>
  <form @submit.prevent="selectAnnotation()">
    <cytomine-modal :active="active" :title="$t('select-annotation')" @close="$emit('update:active', false)">
      <b-loading class="small" :active="loading" :is-full-page="false"/>

      <template v-if="!loading">
        <div class="annotation-content">
          <selectable-annotation
            v-for="annotation in annotations"
            :key="annotation.id"
            :annotation="annotation"
            :images="images"
            :is-selected="selectedAnnotation === annotation.id"
            :users="layersIds"
            @update:selected="selectedAnnotation = $event"
          />
        </div>

        <b-pagination
          :total="nbAnnotations"
          :current.sync="currentPage"
          :per-page="nbPerPage"
          size="is-small"
        />
      </template>

      <template #footer>
        <button class="button" type="button" @click="cancelAnnotation()">
          {{ $t('button-cancel') }}
        </button>
        <button class="button is-link">
          {{ $t('select') }}
        </button>
      </template>
    </cytomine-modal>
  </form>
</template>

<script>
import {AnnotationCollection} from '@/api';
import {get} from '@/utils/store-helpers';

import CytomineModal from '@/components/utils/CytomineModal';
import SelectableAnnotation from '@/components/annotations/SelectableAnnotation';

export default {
  name: 'AnnotationSelection',
  props: {
    active: {type: Boolean, default: false},
  },
  data() {
    return {
      annotations: [],
      nbAnnotations: 0,
      nbPerPage: 15,
      currentPage: 1,
      loading: true,
      selectedAnnotation: null,
    };
  },
  components: {
    CytomineModal,
    SelectableAnnotation,
  },
  computed: {
    project: get('currentProject/project'),
    collection() {
      return new AnnotationCollection({
        project: this.project.id,
        image: this.imageIds,
        terms: this.terms.map(term => term.id),
        showWKT: true,
        max: this.nbPerPage,
      });
    },
    imagesWrapper() {
      return this.$store.getters['currentProject/currentViewer'].images;
    },
    images() {
      return Object.values(this.imagesWrapper);
    },
    imageIds() {
      return this.images.map(image => image.imageInstance.id);
    },
    layers() {
      let layers = Object
        .values(this.imagesWrapper)
        .map(image => image.layers.selectedLayers)
        .flat();

      return Array.from(new Map(layers.map(item => [item.id, item])).values());
    },
    layersIds() {
      return this.layers.map(layer => layer.id);
    },
    terms() {
      return this.$store.getters['currentProject/terms'] || [];
    },
  },
  watch: {
    currentPage() {
      this.fetchAnnotationsPage();
    },
  },
  methods: {
    addAnnotationHandler(annotation) {
      if (this.imageIds.includes(annotation.image)) {
        this.fetchAnnotationsPage();
      }
    },
    cancelAnnotation() {
      this.selectedAnnotation = null;
      this.$emit('update:active', false);
    },
    selectAnnotation() {
      if (!this.selectedAnnotation) {
        this.$notify({type: 'error', text: this.$t('notif-error-annotation-select')});
        return;
      }

      this.$emit('select-annotation', this.selectedAnnotation);
      this.$emit('update:active', false);

      this.selectedAnnotation = null;
    },
    async fetchAnnotationsPage() {
      try {
        let data = await this.collection.fetchPage(this.currentPage - 1);
        this.annotations = data.array;
        this.nbAnnotations = data.totalNbItems;
      } catch (error) {
        console.log(error);
        this.nbAnnotations = 0;
      }
      this.loading = false;
    },
  },
  async created() {
    await this.fetchAnnotationsPage();
    this.loading = false;
  },
  async mounted() {
    this.$eventBus.$on('addAnnotation', this.addAnnotationHandler);
  },
  async beforeDestroy() {
    this.$eventBus.$off('addAnnotation', this.addAnnotationHandler);
  },
};
</script>

<style scoped>
::v-deep(.pagination-list li) {
  margin: 0;
}

::v-deep(.pagination li::marker) {
  content: none;
}

::v-deep ul.pagination-list {
  justify-content: flex-end;
  margin: auto;
}

.annotation-content {
  display: flex;
  flex-wrap: wrap;
}
</style>
