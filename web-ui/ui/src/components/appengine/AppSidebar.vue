<template>
  <div class="sidebar-container">
    <div class="sidebar-wrapper" :class="{ expanded: expanded }" ref="sidebar">
      <nav class="sidebar">
        <h1 class="sidebar-title">{{ $t('app-engine.applications') }}</h1>

        <ul>
          <router-link tag="li" to="/apps" exact>
            <a>
              <i class="fas fa-code"></i>
              {{ $t('app-engine.tasks.installed') }}
            </a>
          </router-link>
          <router-link tag="li" to="/apps/store">
            <a>
              <i class="fas fa-store"></i>
              {{ $t('app-store') }}
            </a>
          </router-link>
        </ul>
        <ul class="bottom-menu">
          <router-link tag="li" to="/apps/configuration">
            <a>
              <i class="fas fa-cogs"></i>
              {{ $t('configuration') }}
            </a>
          </router-link>
        </ul>
      </nav>
    </div>

    <button class="arrow-sidebar" :class="{ expanded: expanded }" @click="expanded = !expanded">
      <i class="fas fa-chevron-right"></i>
    </button>
  </div>
</template>

<script>
import {sync} from '@/utils/store-helpers';

export default {
  name: 'AppSidebar',
  computed: {
    expanded: sync('currentUser/expandedSidebar'),
  },
};
</script>

<style lang="scss" scoped>
$background: #444;
$activeBackground: #4f4f4f;
$hoverBackground: #82aad8;
$color: #ccc;
$activeColor: #eee;
$hoverColor: white;
$arrowBackground: #484848;
$border: #383838;
$arrowColor: #888;

.arrow-sidebar {
  width: 1.5rem;
  border: none;
  border-left: 1px solid $border;
  background: $arrowBackground;
  justify-content: center;
  display: flex;
  align-items: center;
}

.arrow-sidebar i {
  color: $arrowColor;
  transition: transform 0.2s ease;
}

.arrow-sidebar.expanded i {
  transform: rotate(180deg);
}

.bottom-menu {
  position: absolute;
  bottom: 0;
  left: 0;
}

.fas {
  font-size: 1rem;
  width: 4rem;
  text-align: center;
  flex-shrink: 0;
}

.sidebar {
  width: 16rem;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
}

.sidebar-container {
  display: flex;
  height: 100%;
}

.sidebar-title {
  padding: 0.8em 0em 0.8em;
  width: 14.5rem !important;
  line-height: 1.8rem;
  text-align: center;
  color: $background;
  font-size: 1rem;
  margin: 0;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  text-align: center;
  font-weight: 600;
}

.sidebar-wrapper {
  display: flex;
  height: 100%;
  background: $background;
  overflow: hidden;
  transition: width .2s linear;
  width: 4rem;
}

.sidebar-wrapper.expanded {
  width: 16rem;
}

.sidebar-wrapper.expanded .sidebar-title {
  color: $activeColor !important;
}

a {
  position: relative;
  display: block;
  font-size: 0.85rem;
  height: 100%;
  font-weight: 600;
  color: $color;
  text-decoration: none;
  display: flex;
  align-items: center;
}

li {
  height: 3.5rem;
}

li a:hover {
  color: $hoverColor;
  background: $hoverBackground !important;
}

li.is-active a {
  box-shadow: inset 5px 0 0 $hoverBackground;
  color: $activeColor;
  background: $activeBackground;
}

ul {
  width: 100%;
}
</style>
