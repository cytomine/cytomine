<template>
  <div>
    <i
      class="fas fa-user-cog"
      :class="{disabled: !isManager, editable: editable}"
      :title="$t(label)"
      @click="$emit('toggleManager')"
    >
    </i>
    <i v-if="(!editable && isRepresentative)" class="superscript fas fa-flag"></i>
    <i v-else-if="(editable && isManager)"
       class="superscript fas fa-flag editable"
       :class="{disabled: !isRepresentative}"
       @click="$emit('toggleRepresentative')"
    ></i>
  </div>
</template>

<script>
export default {
  name: 'IconProjectMemberRole',
  props: {
    isManager: {type: Boolean, default: false},
    isRepresentative: {type: Boolean, default: false},
    editable: {type: Boolean, default: false}
  },
  computed: {
    label() {
      if (this.editable) {
        return '';
      }
      if (this.isRepresentative) {
        return 'representative-icon-label';
      }
      if (this.isManager) {
        return 'manager-icon-label';
      }
      return 'contributor-icon-label';
    }
  }
};
</script>

<style scoped>
  .fas.fa-user-cog {
    width: 20px;
    position: relative;
    text-align: center;
  }

  .superscript {
    font-size: 8px;
    position: relative;
    top: -8px;
    left: -8px;
    right: 0;
  }

  .fas.disabled, .disabled .fas {
    color: rgba(0, 0, 0, 0.1);
  }

  .editable:hover {
    color: #2778ad !important;
    cursor: pointer;
  }

  div {
    width: 30px;
    text-align: left;
    display: inline-block;
  }
</style>
