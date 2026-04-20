<template>
  <div class="drawer" :style="drawerStyle" @transitionend="handleTransitionEnd">
    <div class="drawer-handle-area">
      <div class="drawer-header">
        <slot name="header">
          <span class="drawer-title">{{ $t('app-engine.applications') }}</span>
        </slot>
        <b-button
          icon-pack="fas"
          :icon-left="isCollapsed ? 'chevron-up' : 'chevron-down'"
          @click.stop="toggleCollapse"
        />
      </div>
    </div>
 
    <div class="drawer-body" :style="bodyStyle">
      <div class="drawer-body-inner">
        <slot />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AppBottomDrawer',
  props: {
    value: {type: Boolean, default: false},
    defaultHeight: {type: String, default: '50vh'},
    collapsedHeight: {type: String, default: '60px'},
  },
  data() {
    return {
      isCollapsed: true,
      isDragging: false,
      dragStartY: 0,
      dragStartHeight: 0,
      currentHeight: 0,
    };
  },
  computed: {
    isOpen() {
      return this.value;
    },
    drawerHeight() {
      if (this.isCollapsed) {
        return this.collapsedHeight;
      }
      return this.currentHeight ? `${this.currentHeight}px` : this.defaultHeight;
    },
    drawerStyle() {
      return {
        height: this.drawerHeight,
        width: '100%',
      };
    },
    bodyStyle() {
      return {
        flex: 1,
        minHeight: 0,
        overflow: 'auto',
      };
    },
  },
  mounted() {
    const parsed = parseInt(this.defaultHeight);
    if (!isNaN(parsed) && this.defaultHeight.endsWith('px')) {
      this.currentHeight = parsed;
    } else {
      this.currentHeight = window.innerHeight * (parseInt(this.defaultHeight) / 100);
    }
  },
  methods: {
    close() {
      this.$emit('input', false);
      this.$emit('close');
    },
    open() {
      this.$emit('input', true);
      this.$emit('open');
    },
    toggleCollapse() {
      this.isCollapsed = !this.isCollapsed;
      this.$emit('collapse', this.isCollapsed);
      this.$nextTick(() => {
        this.$emit('resize');
      });
    },
    handleTransitionEnd(event) {
      if (!this.isDragging && event.propertyName === 'height') {
        this.$emit('resize');
      }
    },
  },
};
</script>

<style scoped>
.drawer {
  position: relative;
  width: 100%;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: height 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  max-height: 92vh;
}

.drawer-handle-area {
  flex-shrink: 0;
  user-select: none;
  padding: 10px 20px 0;
}

.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.drawer-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.drawer-body {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

.drawer-body-inner {
  padding: 16px 20px 24px;
}
</style>