<template>
<div class="content-wrapper">
  <div class="box">
    <h2>{{$t('activity-logs')}}</h2>
    <activity-logs :idUser="currentUser.id" />
  </div>
</div>
</template>

<script>
import { get } from '@/utils/store-helpers';
import ActivityLogs from '@/components/utils/ActivityLogs.vue';

export default {
  name: 'user-activity',
  components: { ActivityLogs },
  computed: {
    currentUser: get('currentUser/user')
  }
};
</script>

<style scoped>
.content-wrapper {
  height: 100%;
}

.box {
  height: 100%;
  display: flex;
  flex-direction: column;
}
</style>
