<template>
<div></div>
</template>


<script>
import { get } from '@/utils/store-helpers';

export default {
  computed: {
    project: get('currentProject/project'),
    configUI: get('currentProject/configUI')
  },
  created() {
    if (this.configUI['project-images-tab']) {
      this.$router.replace(`/project/${this.project.id}/images`);
    } else {
      this.$router.replace(`/project/${this.project.id}/information`);
    }
  }
};
</script>
