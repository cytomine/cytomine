<template>
  <div class="sidebar-wrapper">
    <nav class="sidebar">
      <h1 class="sidebar-title">{{ $t('app-engine.applications') }}</h1>

      <div class="sidebar-content">
        <div class="sidebar-main-tabs">
          <b-menu>
            <b-menu-list>
              <b-menu-item
                tag="router-link"
                to="/app-engine"
                icon="code"
                label="My Apps"
                :active="activeTab === 'apps'"
                @click="setActiveTab('apps')"
              />
              <b-menu-item
                tag="router-link"
                to="/app-engine"
                icon="store"
                label="App Store"
                :active="activeTab === 'store'"
                @click="setActiveTab('store')"
              />
            </b-menu-list>
          </b-menu>
        </div>

        <div class="sidebar-bottom">
          <b-menu>
            <b-menu-list>
              <b-menu-item 
                tag="router-link"
                to="/app-engine/configuration"
                icon="cog" 
                label="Configuration"
                :active="activeTab === 'configuration'"
                @click="setActiveTab('configuration')"
              />
            </b-menu-list>
          </b-menu>
        </div>
      </div>
    </nav>
  </div>
</template>

<script>
export default {
  name: 'AppSidebar',
  data() {
    return {
      activeTab: 'dashboard',
    };
  },
  methods: {
    setActiveTab(tab) {
      this.activeTab = tab;
      this.$emit('tab-changed', tab);
    }
  }
};
</script>

<style scoped>
.sidebar-wrapper {
  height: 100%;
  width: 250px;
  background-color: #363636;
  color: #fff;
  position: relative;
  box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
}

.sidebar {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 1rem;
}

.sidebar-title {
  font-size: 1.5rem;
  font-weight: bold;
  color: #fff;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #4a4a4a;
  text-align: center;
}

.sidebar-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  justify-content: space-between;
}

.sidebar-main-tabs {
  flex-grow: 1;
}

.sidebar-bottom {
  margin-top: auto;
  padding-top: 1rem;
  border-top: 1px solid #4a4a4a;
}

:deep(.menu-list a) {
  color: #dbdbdb !important;
  border-radius: 4px;
  padding: 0.75rem 1rem;
  margin-bottom: 0.5rem;
  transition: all 0.3s ease;
}

:deep(.menu-list a:hover) {
  background-color: #4a4a4a !important;
  color: #fff !important;
}

:deep(.menu-list a.is-active) {
  background-color: #3273dc !important;
  color: #fff !important;
}

:deep(.menu-list .icon) {
  margin-right: 0.5rem;
}
</style>
