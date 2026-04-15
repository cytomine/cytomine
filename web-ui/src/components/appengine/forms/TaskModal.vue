<template>
  <form>
    <cytomine-modal :active="active" :title="$t('app-engine.ae-run-task')" @close="$emit('update:active', false)">
      <template #footer>
        <b-button @click="$emit('update:active', false)">{{ $t('button-cancel') }}</b-button>
        <b-button class="is-link">{{ $t('app-engine.ae-run-task') }}</b-button>
      </template>
    </cytomine-modal>
  </form>
</template>

<script>
import CytomineModal from '@/components/utils/CytomineModal';

export default {
  name: 'TaskModal',
  props: {
    active: {type: Boolean, default: false},
  },
  components: {
    CytomineModal,
  },
};
</script>
