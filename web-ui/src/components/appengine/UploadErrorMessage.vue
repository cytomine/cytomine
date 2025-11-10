<template>
  <b-message :closable="false" :title="title" type="is-danger" has-icon>
    <ul>
      <li v-for="(error, index) in errors" :key="index">
        - {{ error.message }}
      </li>
    </ul>
  </b-message>
</template>

<script>
export default {
  name: 'UploadErrorMessage',
  props: {
    error: {type: Object, required: true},
  },
  computed: {
    title() {
      return this.error.message;
    },
    errors() {
      return this.error.details.errors;
    },
  },
};
</script>
