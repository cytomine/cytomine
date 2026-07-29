<template>
<b-datepicker
  :class="styles"
  :value="value"
  @input="$emit('input', $event)"
  :placeholder="placeholder || this.$t('select-date')"
  :min-date="minDate" :max-date="maxDate"
  :month-names="moment.months()" :day-names="moment.weekdaysMin()"
  :date-formatter="date => moment(date).format('ll')" size="is-small"
  :position="position"
>
  <div v-if="resetButton" class="has-text-centered">
    <button class="button is-small is-link" :disabled="!value" @click="$emit('input', null)">
      {{$t('button-reset')}}
    </button>
  </div>
</b-datepicker>
</template>

<script>
import moment from 'moment';

export default {
  name: 'cytomine-date-picker',
  props: {
    value: Date,
    resetButton: {type: Boolean, default: true},
    maxDate: Date,
    minDate: Date,
    placeholder: String,
    styles: {type: Array, default: () => []}, // accept "multiselect", "bold-placeholder",
    position: {type: String, default: null}
  },
  computed: {
    moment() {
      return moment;
    }
  }
};
</script>

<style lang="scss">
.datepicker.multiselect .input {
  min-height: 40px;
  background-color: rgba(255, 255, 255, 0.75);
  border-radius: 5px;
  border: 1px solid #e8e8e8;
  font-size: 1rem;
  font-family: inherit;

  &::placeholder {
    color: #adadad;
    opacity: 1;
  }
}

.datepicker.bold-placeholder .input::placeholder {
  font-weight: 600;
  color: black !important;
}
</style>
