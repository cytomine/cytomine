<template>
  <div>
    <b-loading class="small" :active="loading" :is-full-page="false" />

    <div v-if="!loading" class="annotation-content">
      <selectable-annotation
        v-for="annotation in annotations"
        :key="annotation.id"
        :annotation="annotation"
        :images="images"
        :is-selected="selectedAnnotationIds.includes(annotation.id)"
        @update:selected="updateSelection($event)"
      />
    </div>
  </div>
</template>

<script>
import {AnnotationCollection} from '@/api';
import {get} from '@/utils/store-helpers';
import SelectableAnnotation from '@/components/annotations/SelectableAnnotation';

export default {
  name: 'AnnotationMultiSelect',
  components: {
    SelectableAnnotation,
  },
  data() {
    return {
      loading: true,
      annotations: [],
      selectedAnnotationIds: [],
    };
  },
  computed: {
    project: get('currentProject/project'),
    imagesWrapper() {
      return this.$store.getters['currentProject/currentViewer'].images;
    },
    images() {
      return Object.values(this.imagesWrapper);
    },
    imageIds() {
      return this.images.map(image => image.imageInstance.id);
    },
  },
  methods: {
    updateSelection(annotationId) {
      if (this.selectedAnnotationIds.includes(annotationId)) {
        this.selectedAnnotationIds = this.selectedAnnotationIds.filter(id => id !== annotationId);
      } else {
        this.selectedAnnotationIds.push(annotationId);
      }
    },
    async fetchAnnotations() {
      const response = await new AnnotationCollection({
        project: this.project.id,
        image: this.imageIds,
        showWKT: true,
      }).fetchAll();

      this.annotations = response.array;
    },
  },
  watch: {
    selectedAnnotationIds(annotationIds) {
      this.$emit('input', annotationIds);
    }
  },
  async created() {
    await this.fetchAnnotations();
    this.loading = false;
  },
};
</script>

<style scoped>
.annotation-content {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}
</style>
