<template>
  <div>
    <b-loading class="small" :active="loading" :is-full-page="false"/>

    <div v-if="!loading" class="image-content">
      <selectable-image
        v-for="image in images"
        :key="image.id"
        :image="image"
        :is-selected="selectedImages.includes(image.id)"
        @update:selected="updateSelection($event)"
      />
    </div>
  </div>
</template>

<script>
import SelectableImage from '@/components/image/SelectableImage';
import {ImageInstanceCollection} from '@/api';
import {get} from '@/utils/store-helpers';

export default {
  name: 'ImageMultiSelect',
  components: {
    SelectableImage,
  },
  data() {
    return {
      images: [],
      loading: true,
      selectedImages: [],
    };
  },
  computed: {
    project: get('currentProject/project'),
  },
  methods: {
    updateSelection(annotation) {
      if (this.selectedImages.includes(annotation)) {
        this.selectedImages = this.selectedImages.filter(item => item !== annotation);
      } else {
        this.selectedImages.push(annotation);
      }
    },
    async fetchImages() {
      this.images = (await ImageInstanceCollection.fetchAll({
        filterKey: 'project',
        filterValue: this.project.id,
      })).array;
    }
  },
  watch: {
    selectedImages(images) {
      this.$emit('input', images);
    }
  },
  async created() {
    await this.fetchImages();
    this.loading = false;
  },
};
</script>

<style scoped>
.image-content {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}
</style>
