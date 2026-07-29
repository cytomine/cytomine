<template>
<div class="tile is-child box last-actions">
  <h2>{{$t('activity-logs')}}</h2>

  <b-select v-model="selectedUser">
    <option :value="null">{{$t('all-users-analyses')}}</option>
    <optgroup :label="$t('members')">
      <option v-for="member in members" :value="member.id" :key="member.id">{{member.fullName}}</option>
    </optgroup>
  </b-select>

  <activity-logs :idUser="selectedUser" :startDate="startDate" :endDate="endDate" :project="project" />
</div>
</template>

<script>
import {get} from '@/utils/store-helpers';

import ActivityLogs from '@/components/utils/ActivityLogs.vue';

export default {
  name: 'project-activity-logs',
  props: {
    startDate: Number,
    endDate: Number
  },
  components: {ActivityLogs},
  data() {
    return {
      selectedUser: null,
    };
  },
  computed: {
    project: get('currentProject/project'),
    members: get('currentProject/members'),
  },
  async created() {

  }
};
</script>

<style scoped>
.last-actions {
  height: 100%;
  display: flex;
  flex-direction: column;
  word-wrap: break-word;
}
</style>
