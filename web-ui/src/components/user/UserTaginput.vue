<template>
<b-taginput
  :model-value="modelValue"
  @update:model-value="$emit('update:modelValue', $event)"
  :data="filteredUsers"
  autocomplete
  :open-on-focus="true"
  field="fullName"
  :placeholder="$t('search-user')"
  @typing="val => searchString = val"
  @add="searchString = ''"
  :allow-duplicates="false"
/>
</template>

<script>
import {getWildcardRegexp} from '@/utils/string-utils';

export default {
  name: 'user-taginput',
  props: {
    modelValue: Array,
    users: Array
  },
  data() {
    return {
      searchString: ''
    };
  },
  computed: {
    filteredUsers() {
      let selectedIds = this.modelValue.map(v => v.id);
      let filtered = this.users.filter(user => !selectedIds.includes(user.id));
      if (this.searchString === '') {
        return filtered;
      }

      let regexp = getWildcardRegexp(this.searchString);
      return filtered.filter(user => regexp.test(user.fullName));
    }
  }
};
</script>
