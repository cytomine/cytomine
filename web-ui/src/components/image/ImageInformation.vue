<template>
<div class="box error" v-if="permissionError || notFoundError">
  <h2> {{ $t(permissionError ? 'access-denied' : 'not-found') }} </h2>
  <p> {{ $t(permissionError ? 'insufficient-permission' : 'not-found-error') }} </p>
</div>
<div class="content-wrapper" v-else>
  <b-loading :is-full-page="false" v-model:active="loading" />
  <div class="box" v-if="!loading">
    <div class="box-title">
      <i18n path="detailed-image-information" tag="h1">
        <router-link place="imageName" :to="`/project/${image.project}/image/${image.id}`">
          <image-name :image="image" />
        </router-link>
      </i18n>
      <router-link place="imageName" :to="`/project/${image.project}/image/${image.id}`">
        <button class="button is-link">
          <span class="icon-text">
            <span class="icon">
              <i class="fas fa-image"></i>
            </span>
            <span class="is-align-content-baseline">
              {{ $t('open-in-viewer')}}
            </span>
          </span>
        </button>
      </router-link>
      <router-link place="imageName" :to="prevRoute">
        <button class="button is-link">
          <span class="icon-text">
            <span class="icon">
              <i class="fas fa-arrow-left"></i>
            </span>
            <span class="is-align-content-baseline">
              {{ $t('go-back')}}
            </span>
          </span>
        </button>
      </router-link>
    </div>

    <image-details
      v-if="image"
      :image="image"
      editable
      @setResolution="resolution => setResolution(resolution)"
      @setMagnification="magnification => image.magnification = magnification"
      @delete="deleteImage()"
    />
  </div>
</div>
</template>

<script>
import ImageName from './ImageName.vue';
import ImageDetails from './ImageDetails.vue';

import { ImageInstance } from '@/api';
import vendorFromFormat from '@/utils/vendor';

export default {
  name: 'image-information',
  components: {
    ImageName,
    ImageDetails
  },
  data() {
    return {
      loading: true,
      image: null,
      permissionError: false,
      notFoundError: false,
      prevRoute: null
    };
  },
  // https://v3.router.vuejs.org/guide/advanced/navigation-guards.html#in-component-guards
  beforeRouteEnter(to, from, next) {
    next(vm => {
      vm.prevRoute = from;
    });
  },
  computed: {
    idImage() {
      return this.$route.params.idImage;
    }
  },
  watch: {
    idImage() {
      this.loadImage();
    }
  },
  methods: {
    async loadImage() {
      this.loading = true;
      this.permissionError = false;
      this.notFoundError = false;
      try {
        let image = await ImageInstance.fetch(this.idImage);
        image.vendor = vendorFromFormat(image.contentType);
        this.image = image;
      } catch (error) {
        console.log(error);
        if (error.response.status === 403) {
          this.permissionError = true;
        } else {
          this.notFoundError = true;
        }
      }
      this.loading = false;
    },
    deleteImage() {
      this.$router.push(`/project/${this.image.project}`);
    },
    setResolution(resolution) {
      this.image.physicalSizeX = resolution.x;
      this.image.physicalSizeY = resolution.y;
      this.image.physicalSizeZ = resolution.z;
      this.image.fps = resolution.t;
    }
  },
  created() {
    this.loadImage();
  }
};
</script>

<style scoped lang="scss">
  .box-title {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    gap: 10px;

    h1 {
      flex: auto
    }
  }
</style>
