<template>
<cytomine-modal :active="active" :title="$t('add-members-to-project')" @close="$emit('update:active', false)">
    <b-loading :is-full-page="false" :model-value="loading" class="small" />
    <template v-if="!loading">
      <b-field>
        <domain-tag-input v-model="selectedMembers" :domains="notMemberUsers" placeholder="search-user" displayedProperty="fullName" searchedProperty="fullName"/>
      </b-field>
    </template>

    <template #footer>
      <button class="button" @click="$emit('update:active', false)">
        {{$t('button-cancel')}}
      </button>
      <button class="button is-link" @click="addMembers">
        {{$t('button-add')}}
      </button>
    </template>
</cytomine-modal>
</template>

<script>
import { get } from '@/utils/store-helpers';

import { UserCollection } from '@/api';
import DomainTagInput from '@/components/utils/DomainTagInput.vue';
import CytomineModal from '@/components/utils/CytomineModal.vue';

export default {
  name: 'add-member-modal',
  props: {
    active: Boolean
  },
  components: {
    CytomineModal,
    DomainTagInput,
  },
  data() {
    return {
      loading: true,
      users: [],
      selectedMembers: []
    };
  },
  computed: {
    project: get('currentProject/project'),
    projectMembersIds() {
      let projectMembers = this.$store.state.currentProject.members;
      let newProjectMembers = projectMembers.concat(this.selectedMembers);
      return newProjectMembers.map(u => u.id);
    },
    notMemberUsers() {
      return this.users.filter(user => !this.projectMembersIds.includes(user.id));
    }
  },
  watch: {
    active() {
      this.selectedMembers = [];
    }
  },
  methods: {
    async addMembers() {
      try {
        await this.project.addUsers(this.selectedMembers.map(member => member.id));
        this.$emit('addMembers');
        this.$notify({ type: 'success', text: this.$t('notif-success-add-project-members') });
      } catch (error) {
        console.log(error);
        this.$notify({ type: 'error', text: this.$t('notif-error-add-project-members') });
      }

      this.$emit('update:active', false);
    }
  },
  async created() {
    this.users = (await UserCollection.fetchAll()).array;
    this.loading = false;
  }
};
</script>

<style scoped>
:deep(.modal-card), :deep(.modal-card-body) {
  overflow: visible !important;
  width: 60vw !important;
}
</style>
