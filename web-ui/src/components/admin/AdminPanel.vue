<template>
<div class="box error" v-if="!isAdmin">
  <h2> {{ $t('access-denied') }} </h2>
  <p>{{ $t('insufficient-permission') }}</p>
</div>
<div class="content-wrapper" v-else>
  <b-field class="radio-buttons-fullwidth">
    <b-radio-button v-model="activeTab" native-value="dashboard" type="is-link">
      {{$t('dashboard')}}
    </b-radio-button>

    <b-radio-button v-model="activeTab" native-value="users" type="is-link">
      {{$t('users')}}
    </b-radio-button>

    <b-radio-button v-model="activeTab" native-value="tags" type="is-link">
      {{$t('tags')}}
    </b-radio-button>
  </b-field>

  <div class="box">
    <keep-alive>
      <component :is="activeComponent" />
    </keep-alive>
  </div>
</div>
</template>

<script>
import { get } from '@/utils/store-helpers';
import { KeycloakRole } from '@/constants/UserRole.js';

import AdminDashboard from './AdminDashboard.vue';
import AdminUsers from './AdminUsers.vue';
import AdminTags from './AdminTags.vue';
const defaultTab = 'dashboard';

export default {
  name: 'admin-panel',
  data() {
    return {
      activeTab: 0,
      tabNames: [
        'dashboard',
        'users',
        'tags',
        'configuration'
      ]
    };
  },
  computed: {
    currentUser: get('currentUser/user'),
    queriedTab() {
      return this.$route.query.tab;
    },
    isAdmin() {
      return this.$keycloak.hasResourceRole(KeycloakRole.ADMIN);
    },
    activeComponent() {
      switch (this.activeTab) {
        case 'dashboard':
          return AdminDashboard;
        case 'users':
          return AdminUsers;
        case 'tags':
          return AdminTags;
      }
      throw new Error('Cannot load active tabs ' + this.activeTab);
    }
  },
  watch: {
    queriedTab() {
      this.changeTab();
    },
    activeTab() {
      if (this.activeTab !== defaultTab || this.queriedTab) {
        this.$router.push(`?tab=${this.activeTab}`);
      }
    }
  },
  methods: {
    changeTab() {
      this.activeTab = this.queriedTab || defaultTab;
      if (!this.activeComponent) {
        this.activeTab = defaultTab;
      }
    }
  },
  created() {
    this.changeTab();
  }
};
</script>

<style scoped>
.box {
  position: relative;
  min-height: 20em;
}
</style>
