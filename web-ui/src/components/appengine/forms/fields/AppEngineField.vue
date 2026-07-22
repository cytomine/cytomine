<template>
  <component v-model="input" :is="currentField" :parameter="parameter" @input="$emit('input', $event)"/>
</template>

<script>
import ArrayField from '@/components/appengine/forms/fields/ArrayField.vue';
import BooleanField from '@/components/appengine/forms/fields/BooleanField.vue';
import EnumerationField from '@/components/appengine/forms/fields/EnumerationField.vue';
import FileField from '@/components/appengine/forms/fields/FileField.vue';
import GeometryField from '@/components/appengine/forms/fields/GeometryField.vue';
import ImageField from '@/components/appengine/forms/fields/ImageField.vue';
import IntegerField from '@/components/appengine/forms/fields/IntegerField.vue';
import NumberField from '@/components/appengine/forms/fields/NumberField.vue';
import StringField from '@/components/appengine/forms/fields/StringField.vue';

export default {
  name: 'AppEngineField',
  components: {
    ArrayField,
    BooleanField,
    EnumerationField,
    FileField,
    GeometryField,
    ImageField,
    IntegerField,
    NumberField,
    StringField,
  },
  props: {
    parameter: {type: Object, required: true},
    value: {}
  },
  computed: {
    typeId() {
      return this.parameter.type.id;
    },
    input: {
      get() {
        return this.value;
      },
      set(value) {
        this.$emit('input', value);
      }
    },
    currentField() {
      switch (this.typeId) {
        case 'array':
          return ArrayField;
        case 'boolean':
          return BooleanField;
        case 'enumeration':
          return EnumerationField;
        case 'file':
          return FileField;
        case 'geometry':
          return GeometryField;
        case 'image':
          return ImageField;
        case 'integer':
          return IntegerField;
        case 'number':
          return NumberField;
        case 'string':
          return StringField;
        default:
          return null;
      }
    },
  }
};
</script>
