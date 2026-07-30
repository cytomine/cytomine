<template>
<div class="box error" v-if="!configUI['project-information-tab']">
  <h2> {{ $t('access-denied') }} </h2>
  <p>{{ $t('insufficient-permission') }}</p>
</div>
<div v-else class="content-wrapper">
  <div class="box">
    <project-details
      :project="project"
      :excluded-properties="excludedProperties"
      editable
      @update="updateProject"
      @delete="deleteProject()"
    />
  </div>
</div>
</template>

<script>
import { get } from '@/utils/store-helpers';

import ProjectDetails from './ProjectDetails.vue';
import { Project } from '@/api';
export default {
  name: 'project-information',
  components: { ProjectDetails },
  data() {
    return {
      excludedProperties: [
        'imagesPreview'
      ]
    };
  },
  computed: {
    project: get('currentProject/project'),
    configUI: get('currentProject/configUI')
  },
  methods: {
    updateProject(updatedProject) {
      this.$store.dispatch('currentProject/updateProject', updatedProject);
    },
    async deleteProject() {
      try {
        await Project.delete(this.project.id);
        this.$notify({
          type: 'success',
          text: this.$t('notif-success-project-deletion', { projectName: this.project.name })
        });
        this.$router.push('/projects');
      } catch (error) {
        console.log(error);
        this.$notify({
          type: 'error',
          text: this.$t('notif-error-project-deletion', { projectName: this.project.name })
        });
      }
    }
  },
  created() {
    this.$store.dispatch('currentProject/reloadProject');
  }
};
</script>
