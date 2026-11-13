<template>
<div class="content-wrapper lti-content-selection">
  <b-loading :is-full-page="false" :active.sync="loading" />

  <div class="box lti-box">
    <h2>Link Cytomine content to your course</h2>
    <p class="lti-intro">
      Select the Cytomine project (and optionally one of its images) you want to make available in your course.
      Students who open this course activity will automatically get access to the selected content as contributors.
    </p>

    <b-message v-if="error" type="is-danger" has-icon icon-size="is-small">
      {{error}}
    </b-message>

    <template v-if="!loading && !error">
      <template v-if="projects.length === 0">
        <p>
          You do not administer any project yet. Create a project in Cytomine first, then start the
          content selection again from your course.
        </p>
      </template>

      <template v-else>
        <!-- Step 1 — project -->
        <div class="lti-section">
          <h3>1. Project</h3>
          <div class="lti-options">
            <button
              v-for="project in projects"
              :key="project.id"
              type="button"
              class="lti-option"
              :class="{ 'is-selected': selectedProject && selectedProject.id === project.id }"
              @click="selectProject(project)"
            >
              <span class="lti-option-name">{{project.name}}</span>
              <span class="lti-option-hint">Project</span>
            </button>
          </div>
        </div>

        <!-- Step 2 — target content -->
        <div v-if="selectedProject" class="lti-section">
          <h3>2. Target content</h3>
          <div class="lti-radios">
            <label class="radio">
              <input type="radio" value="project" v-model="targetType" />
              Entire project
            </label>
            <label class="radio">
              <input type="radio" value="image" v-model="targetType" />
              One image
            </label>
          </div>

          <div v-if="targetType === 'image'" class="lti-images">
            <input
              v-if="images.length > 0"
              v-model="imageSearch"
              class="input"
              type="search"
              placeholder="Filter images..."
            />
            <b-loading :is-full-page="false" :active.sync="loadingImages" />

            <div v-if="!loadingImages" class="lti-options">
              <button
                v-for="image in filteredImages"
                :key="image.id"
                type="button"
                class="lti-option"
                :class="{ 'is-selected': selectedImage && selectedImage.id === image.id }"
                @click="selectedImage = image"
              >
                <span class="lti-option-name">{{imageDisplayName(image)}}</span>
                <span class="lti-option-hint">Image</span>
              </button>
              <p v-if="images.length === 0" class="lti-empty">This project does not contain any image.</p>
              <p v-else-if="filteredImages.length === 0" class="lti-empty">No image matches your filter.</p>
            </div>
            <p v-if="moreImagesAvailable" class="lti-hint">
              Only the first {{images.length}} of {{totalNbImages}} images are shown. Pick this project as a whole to
              give access to all of its current (and future) content.
            </p>
          </div>
        </div>

        <!-- Step 3 — confirmation -->
        <div v-if="selectedProject" class="lti-section">
          <h3>3. Confirm</h3>
          <table class="table is-fullwidth lti-summary">
            <tbody>
              <tr>
                <th>Activity title</th>
                <td>{{selectedTitle}}</td>
              </tr>
              <tr>
                <th>Students will open</th>
                <td>{{selectedUrl}}</td>
              </tr>
            </tbody>
          </table>

          <b-message v-if="mintError" type="is-danger" has-icon icon-size="is-small">
            Failed to prepare the content share: {{mintError}}
          </b-message>

          <button
            class="button is-link"
            :disabled="!selectionReady || submitting"
            :class="{ 'is-loading': submitting }"
            @click="confirm"
          >
            {{submitting ? 'Adding to your course...' : 'Add to course'}}
          </button>
        </div>
      </template>
    </template>
  </div>

  <!-- Hidden form auto-submitted to the IAM deep-linking return endpoint (no CORS preflight). -->
  <form ref="returnForm" :action="dlReturnUrl" method="post" enctype="application/x-www-form-urlencoded" accept-charset="UTF-8" class="is-hidden">
    <input type="hidden" name="ctx" :value="ctx" />
    <input type="hidden" name="url" :value="selectedUrl" />
    <input type="hidden" name="title" :value="selectedTitle" />
    <input type="hidden" name="text" :value="selectedText" />
    <input type="hidden" name="custom" :value="customParams" />
  </form>
</div>
</template>

<script>
import { Cytomine, ProjectCollection, ImageInstanceCollection } from '@/api';
import constants from '@/utils/constants';

const MAX_IMAGES = 100;

export default {
  name: 'lti-content-selection',

  data() {
    return {
      loading: true,
      error: null,
      ctx: null,
      dlReturnUrl: '',
      projects: [],
      selectedProject: null,
      loadingImages: false,
      images: [],
      imageSearch: '',
      selectedImage: null,
      targetType: 'project',
      totalNbImages: 0,
      joinToken: null,
      submitting: false,
      mintError: null
    };
  },

  computed: {
    currentUser() {
      return this.$store.state.currentUser.user;
    },

    filteredImages() {
      let query = this.imageSearch.trim().toLowerCase();
      if (!query) {
        return this.images;
      }
      return this.images.filter(image => this.imageDisplayName(image).toLowerCase().includes(query));
    },

    moreImagesAvailable() {
      return this.totalNbImages > this.images.length;
    },

    targetIsImage() {
      return this.targetType === 'image';
    },

    selectedUrl() {
      if (!this.selectedProject) {
        return '';
      }
      let url = `${window.location.origin}/project/${this.selectedProject.id}`;
      if (this.targetIsImage && this.selectedImage) {
        url += `/image/${this.selectedImage.id}`;
      }
      return url;
    },

    selectedTitle() {
      if (!this.selectedProject) {
        return 'Cytomine';
      }
      if (this.targetIsImage && this.selectedImage) {
        return `${this.selectedProject.name} — ${this.imageDisplayName(this.selectedImage)}`;
      }
      return this.selectedProject.name;
    },

    selectedText() {
      if (!this.selectedProject) {
        return 'Open content in Cytomine.';
      }
      if (this.targetIsImage && this.selectedImage) {
        return `Open the image "${this.imageDisplayName(this.selectedImage)}" from the project "${this.selectedProject.name}" in Cytomine.`;
      }
      return `Open the project "${this.selectedProject.name}" in Cytomine.`;
    },

    customParams() {
      // Keys are the wire format agreed with the IAM/LTI side (custom.cytomine_join_token on the launch), not JS identifiers.
      let custom = {};
      custom['cytomine_join_token'] = this.joinToken;
      custom['cytomine_project'] = this.selectedProject ? String(this.selectedProject.id) : null;
      if (this.targetIsImage && this.selectedImage) {
        custom['cytomine_image'] = String(this.selectedImage.id);
      }
      return JSON.stringify(custom);
    },

    selectionReady() {
      return !!this.selectedProject && (!this.targetIsImage || !!this.selectedImage);
    }
  },

  methods: {
    imageDisplayName(image) {
      return image.instanceFilename || image.originalFilename || image.filename || `Image ${image.id}`;
    },

    async selectProject(project) {
      if (this.selectedProject && this.selectedProject.id === project.id) {
        return;
      }
      this.selectedProject = project;
      this.selectedImage = null;
      this.targetType = 'project';
      this.images = [];
      this.imageSearch = '';
      this.totalNbImages = 0;
      this.loadingImages = true;
      try {
        let collection = new ImageInstanceCollection({
          nbPerPage: MAX_IMAGES,
          filterKey: 'project',
          filterValue: project.id
        });
        await collection.fetchPage(0);
        this.images = collection.array;
        this.totalNbImages = collection.totalNbItems || 0;
      } catch (error) {
        this.images = [];
        this.totalNbImages = 0;
      } finally {
        this.loadingImages = false;
      }
    },

    async confirm() {
      this.mintError = null;
      try {
        let { data } = await Cytomine.instance.api.put(`project/${this.selectedProject.id}/join-token.json`);
        this.joinToken = data.joinToken;
        if (!this.joinToken) {
          throw new Error('no join token returned by the server');
        }
        this.submitting = true;
        await this.$nextTick();
        this.$refs.returnForm.submit();
      } catch (error) {
        this.submitting = false;
        let message = error.response && error.response.data
          ? `${error.response.status}: ${error.response.data}`
          : error.message;
        this.mintError = message;
      }
    }
  },

  async created() {
    this.ctx = this.$route.query.ctx || '';

    // Build the IAM deep-linking return endpoint, tolerating IAM_URL with or without the /realms/cytomine suffix
    // (same normalization as src/keycloak.js).
    let iamBase = String(constants.IAM_URL || '').trim();
    if (iamBase.endsWith('/')) {
      iamBase = iamBase.slice(0, -1);
    }
    if (iamBase.endsWith('/realms/cytomine')) {
      iamBase = iamBase.slice(0, -'/realms/cytomine'.length);
    }
    this.dlReturnUrl = `${iamBase}/realms/cytomine/lti/dl-return`;

    if (!this.ctx) {
      this.error = 'This page can only be reached from your LMS to select Cytomine content. ' +
        'Start the content selection again from your course.';
      this.loading = false;
      return;
    }

    try {
      let collection = await ProjectCollection.fetchAll({
        filterKey: 'user',
        filterValue: this.currentUser.id,
        light: true,
        admin: true
      });
      this.projects = collection.array;
    } catch (error) {
      this.error = 'Failed to load the projects you administer. Please try again.';
    } finally {
      this.loading = false;
    }
  }
};
</script>

<style scoped>
.lti-box {
  max-width: 900px;
}

.lti-intro {
  margin-bottom: 1.5rem;
}

.lti-section {
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #dbdbdb;
}

.lti-section h3 {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
}

.lti-options {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.lti-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  padding: 0.5rem 0.75rem;
  border: 1px solid #dbdbdb;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
  text-align: left;
  font-family: inherit;
  font-size: 0.95rem;
}

.lti-option:hover {
  border-color: #3273dc;
}

.lti-option.is-selected {
  border-color: #3273dc;
  background: #f0f6ff;
}

.lti-option-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.lti-option-hint {
  flex-shrink: 0;
  font-size: 0.8rem;
  color: #7a7a7a;
  text-transform: uppercase;
}

.lti-radios {
  margin-bottom: 0.75rem;
}

.lti-images {
  position: relative;
}

.lti-images .input {
  margin-bottom: 0.5rem;
}

.lti-empty {
  color: #7a7a7a;
  font-style: italic;
}

.lti-hint {
  margin-top: 0.5rem;
  font-size: 0.85rem;
  color: #7a7a7a;
}

.lti-summary th {
  width: 12rem;
}
</style>