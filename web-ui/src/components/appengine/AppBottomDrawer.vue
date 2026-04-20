<template>
  <div class="drawer" :style="drawerStyle" @transitionend="handleTransitionEnd">
    <div
      class="drawer-handle-area"
      @mousedown="startDrag"
      @touchstart.prevent="startDrag"
    >
      <div class="drawer-handle-bar" />
      <div class="drawer-header">
        <slot name="header">
          <span class="drawer-title">{{ title }}</span>
        </slot>
        <button
          class="drawer-chevron-btn"
          @click.stop="toggleCollapse"
          :aria-label="isCollapsed ? 'Expand' : 'Collapse'"
        >
          <svg
            :style="{ transform: isCollapsed ? 'rotate(0deg)' : 'rotate(180deg)' }"
            width="20" height="20" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2.5"
            stroke-linecap="round" stroke-linejoin="round"
          >
            <polyline points="6 9 12 15 18 9" />
          </svg>
        </button>
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
    value: {
      type: Boolean,
      default: false,
    },
    title: {
      type: String,
      default: 'Drawer',
    },
    defaultHeight: {
      type: String,
      default: '50vh',
    },
    collapsedHeight: {
      type: String,
      default: '60px',
    },
  },

  data() {
    return {
      isCollapsed: false,
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

    startDrag(e) {
      this.isDragging = true;
      this.dragStartY = e.type === 'touchstart' ? e.touches[0].clientY : e.clientY;
      this.dragStartHeight = this.currentHeight;

      const onMove = (ev) => {
        if (!this.isDragging) {
          return;
        }
        const clientY = ev.type === 'touchmove' ? ev.touches[0].clientY : ev.clientY;
        const delta = this.dragStartY - clientY;
        const newHeight = Math.min(
          Math.max(this.dragStartHeight + delta, 80),
          window.innerHeight * 0.92
        );
        this.currentHeight = newHeight;
        this.$emit('resize');
        if (this.isCollapsed && delta > 20) {
          this.isCollapsed = false;
          this.$emit('collapse', this.isCollapsed);
        }
      };

      const onUp = () => {
        this.isDragging = false;
        document.removeEventListener('mousemove', onMove);
        document.removeEventListener('mouseup', onUp);
        document.removeEventListener('touchmove', onMove);
        document.removeEventListener('touchend', onUp);

        if (this.currentHeight < 100) {
          this.close();
        }
      };

      document.addEventListener('mousemove', onMove);
      document.addEventListener('mouseup', onUp);
      document.addEventListener('touchmove', onMove, {passive: true});
      document.addEventListener('touchend', onUp);
    },
  },
};
</script>

<style scoped>
.drawer-wrapper {
  position: fixed;
  inset: 0;
  z-index: 1000;
  pointer-events: none;
}

.drawer-backdrop {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  pointer-events: all;
}

.drawer {
  position: relative;
  width: 100%;
  background: #ffffff;
  border-radius: 20px 20px 0 0;
  box-shadow: 0 -6px 40px rgba(0, 0, 0, 0.18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  pointer-events: all;
  transition: height 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: height;
  max-height: 92vh;
}

.drawer-body {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

.drawer-handle-area {
  flex-shrink: 0;
  cursor: grab;
  user-select: none;
  padding: 10px 20px 0;
}

.drawer-handle-area:active {
  cursor: grabbing;
}

.drawer-handle {
  width: 40px;
  height: 4px;
  background: #d1d5db;
  border-radius: 2px;
  margin: 0 auto 12px;
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
  letter-spacing: -0.01em;
}

.drawer-collapse-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  color: #6b7280;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: background 0.15s, color 0.15s;
}

.drawer-collapse-btn:hover {
  background: #f3f4f6;
  color: #111827;
}

.drawer-collapse-btn svg {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Scrollable content */
.drawer-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px 24px;
  overscroll-behavior: contain;
}

/* ── Transitions ── */

/* Backdrop fade */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter,
.fade-leave-to {
  opacity: 0;
}

/* Drawer slide up */
.slide-up-enter-active {
  transition: transform 0.35s cubic-bezier(0.32, 0.72, 0, 1);
}

.slide-up-leave-active {
  transition: transform 0.25s cubic-bezier(0.4, 0, 1, 1);
}

.slide-up-enter,
.slide-up-leave-to {
  transform: translateY(100%);
}

/* Content collapse */
.collapse-enter-active,
.collapse-leave-active {
  transition: opacity 0.2s ease;
}

.collapse-enter,
.collapse-leave-to {
  opacity: 0;
}
</style>