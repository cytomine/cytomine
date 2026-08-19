<template>
<b-taginput
  :model-value="modelValue"
  @update:model-value="$emit('update:modelValue', $event)"
  :data="filteredDomains"
  autocomplete
  :open-on-focus="true"
  :append-to-body="true"
  :field="displayedProperty"
  :placeholder="$t(placeholder)"
  @typing="val => searchString = val"
  @add="searchString = ''"
  :allow-new="allowNew"
  :allow-duplicates="false"
/>
</template>

<script>
import { getWildcardRegexp } from '@/utils/string-utils';

export default {
  name: 'domain-tag-input',
  emits: ['update:modelValue'],
  props: {
    modelValue: Array,
    domains: Array,
    identifier: {
      type: String,
      default: 'id'
    },
    displayedProperty: {
      type: String,
      default: 'name'
    },
    searchedProperty: {
      type: String,
      default: 'name'
    },
    allowNew: {
      type: Boolean,
      default: false
    },
    placeholder: {
      type: String,
      default: 'search-placeholder'
    }
  },
  data() {
    return {
      searchString: ''
    };
  },
  computed: {
    filteredDomains() {
      let selectedIds = this.modelValue.map(v => v[this.identifier]);
      let filtered = this.domains.filter(user => !selectedIds.includes(user[this.identifier]));
      if (this.searchString === '') {
        return filtered;
      }

      let regexp = getWildcardRegexp(this.searchString);

      return filtered.filter(user => regexp.test(user[this.searchedProperty]));
    }
  }
};
</script>
<style scoped>
.taginput {
  width: 82% !important;
}
</style>
