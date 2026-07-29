<template>
  <picture>
    <source :srcset="webpSrc" type="image/webp">
    <source :srcset="jpegSrc" type="image/jpeg">
    <img :src="jpegSrc" alt="" class="image-thumbnail">
  </picture>
</template>

<script>
import {combineImageUrl, splitImageUrl} from '@/utils/image-utils';

export default {
  name: 'ImageThumbnail',
  props: {
    image: {type: Object, default: () => {}},
    macro: {type: Boolean, default: false}, // Ignored if URL is used
    url: {type: String, default: null}, // Cannot be null if image is null or empty
    size: {type: Number},
    extraParameters: {type: Object, default: () => {}}
  },
  computed: {
    jpegSrc() {
      return this.formatSrc('jpg');
    },
    webpSrc() {
      return this.formatSrc('webp');
    },
    splitURL() {
      if (this.url === null) {
        return [];
      }

      let urlParts = splitImageUrl(this.url);
      urlParts.params.set('maxSize', this.size.toString());
      if (this.extraParameters) {
        for (const [key, value] of Object.entries(this.extraParameters)) {
          urlParts.params.set(key, value.toString());
        }
      }
      return urlParts;
    }
  },
  methods: {
    combineURL({host, pathname, format, params}) {
      return combineImageUrl({host, pathname, format, params});
    },
    formatSrc(format) {
      if (this.url !== null) {
        return this.combineURL({format, ...this.splitURL});
      }
      if (this.macro) {
        return this.image.associatedImageURL('macro', this.size, format, this.extraParameters);
      }
      return this.image.thumbURL(this.size, format, this.extraParameters);
    }
  }
};
</script>

<style scoped>

</style>
