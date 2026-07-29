<template>
<div class="box error" v-if="!configUI['project-configuration-tab']">
  <h2> {{ $t('access-denied') }} </h2>
  <p>{{ $t('insufficient-permission') }}</p>
</div>
<div class="content-wrapper" v-else>
  <b-field class="radio-buttons-fullwidth">
    <b-radio-button v-model="activeTab" native-value="general" type="is-link">
      {{$t('general')}}
    </b-radio-button>

    <b-radio-button v-model="activeTab" native-value="members" type="is-link">
      {{$t('members')}}
    </b-radio-button>

    <b-radio-button v-model="activeTab" native-value="customUI" type="is-link">
      {{$t('custom-ui')}}
    </b-radio-button>

    <b-radio-button v-model="activeTab" native-value="imageFilters" type="is-link">
      {{$t('image-filters')}}
    </b-radio-button>
  </b-field>

  <div class="box tab-content">
    <keep-alive>
      <component :is="activeComponent" />
    </keep-alive>
  </div>
</div>
</template>

<script>
import { get } from '@/utils/store-helpers';

import GeneralConfiguration from './configuration-panels/GeneralConfiguration.vue';
import ProjectMembers from './configuration-panels/ProjectMembers.vue';
import CustomUIProject from './configuration-panels/CustomUIProject.vue';
import ProjectImageFilters from './configuration-panels/ProjectImageFilters.vue';

const defaultTab = 'general';

export default {
  name: 'project-configuration',
  data() {
    return {
      activeTab: defaultTab
    };
  },
  computed: {
    configUI: get('currentProject/configUI'),
    queriedTab() {
      return this.$route.query.tab;
    },
    activeComponent() {
      switch (this.activeTab) {
        case 'general':
          return GeneralConfiguration;
        case 'members':
          return ProjectMembers;
        case 'customUI':
          return CustomUIProject;
        case 'imageFilters':
          return ProjectImageFilters;
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
      this.activeTab = this.$route.query.tab || defaultTab;
      if (!this.activeComponent) {
        this.activeTab = defaultTab;
      }
    }
  },
  async created() {
    this.changeTab();
  }
};
</script>

<style scoped>
.tab-content {
  position: relative;
  min-height: 20em;
}
</style>
