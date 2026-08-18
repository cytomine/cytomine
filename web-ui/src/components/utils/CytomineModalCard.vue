<template>
<div class="modal-card">
  <header class="modal-card-head">
    <p class="modal-card-title">{{title}}</p>
    <slot name="controls"></slot>
  </header>
  <section class="modal-card-body">
    <slot></slot>
  </section>
  <footer class="modal-card-foot" v-if="footer">
    <slot name="footer">
      <button class="button" type="button" @click="$emit('close')">
        {{$t('button-close')}}
      </button>
    </slot>
  </footer>
</div>
</template>

<script>
export default {
  name: 'cytomine-modal-card',
  props: {
    title: String,
    footer: { type: Boolean, default: true }
  }
};
</script>
