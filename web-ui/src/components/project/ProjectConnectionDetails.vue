<template>
<table class="table">
  <b-loading :is-full-page="false" v-model="loading" />
  <tbody v-if="!loading">
    <tr>
      <td>{{$t('operating-system')}}</td>
      <td>{{connection.os}}</td>
    </tr>
    <tr>
      <td>{{$t('browser')}}</td>
      <td class="is-capitalized">
        {{connection.browser}} <span v-if="connection.browserVersion">{{connection.browserVersion}}</span>
      </td>
    </tr>
    <tr v-if="consultations">
      <td>{{$t('image-consultations')}}</td>
      <td>
        <div v-if="consultations.length > 0" class="columns is-multiline">
          <div class="column" v-for="consultation in consultations" :key="consultation.id">
            <image-preview :image="consultation" :blindMode="blindMode">
              <div class="details">
                <p>
                  <strong>{{$t('duration')}}:</strong>
                  {{ formatMomentDuration(consultation.time, 'humanize') }}
                  <br>
                  <strong>{{$t('annotation-creations')}}:</strong>
                  {{consultation.countCreatedAnnotations || 0}}
                </p>
              </div>
            </image-preview>
          </div>
        </div>
        <em v-else class="has-text-grey">{{$t('no-image-consultation')}}</em>
      </td>
    </tr>
  </tbody>
</table>
</template>

<script>
import { ImageConsultationCollection } from '@/api';
import ImagePreview from '@/components/image/ImagePreview.vue';
import { formatMomentDuration } from '@/utils/date';

export default {
  name: 'project-connection-details',
  components: { ImagePreview },
  props: {
    connection: Object
  },
  data() {
    return {
      loading: true,
      consultations: null
    };
  },
  computed: {
    blindMode() {
      return this.$store.state.currentProject.project.blindMode;
    }
  },
  methods: {
    formatMomentDuration
  },
  async created() {
    try {
      this.consultations = (await ImageConsultationCollection.fetchAll({ projectConnection: this.connection.id })).array;
    } catch (error) {
      console.log(error);
      this.$notify({ type: 'error', text: this.$t('error-failed-to-fetch-image-consultations') });
    }
    this.loading = false;
  }
};
</script>

<style scoped>
.table {
  background: none;
  position: relative;
}

td:first-child {
  white-space: nowrap;
  font-weight: 600;
}

td:last-child {
  width: 100%;
}

.column {
  min-width: 18em;
  max-width: 18em;
}

.details {
  font-size: 0.85em;
}
</style>
