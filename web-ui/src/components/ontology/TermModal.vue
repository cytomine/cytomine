<template>
<form @submit.prevent="save()">
  <cytomine-modal-card :title="$t(term ? 'update-term' : 'create-term')" class="term-modal">
    <field :form="form" name="name" :validators="nameRules" v-slot="{field, state}">
      <b-field :label="$t('name')" :type="{'is-danger': !!state.meta.errors.length}" :message="state.meta.errors[0]">
        <b-input :model-value="state.value" @update:model-value="field.handleChange" />
      </b-field>
    </field>

    <sketch-picker v-model="color" :presetColors="presetColors" :disable-alpha="true" />

    <template #footer>
      <button class="button" type="button" @click="$emit('close')">
        {{$t('button-cancel')}}
      </button>
      <button class="button is-link" :disabled="!isValid">
        {{$t('button-save')}}
      </button>
    </template>
  </cytomine-modal-card>
</form>
</template>

<script>
import { Field, useForm } from '@tanstack/vue-form';
import { SketchPicker, tinycolor } from 'vue-color';

import { Term } from '@/api';
import { required, rules, validateForm } from '@/utils/form.js';
import CytomineModalCard from '@/components/utils/CytomineModalCard.vue';

export default {
  name: 'term-modal',
  props: {
    term: Object,
    ontology: Object
  },
  components: {
    SketchPicker,
    CytomineModalCard,
    Field
  },
  setup(props) {
    const form = useForm({ defaultValues: { name: props.term ? props.term.name : '' } });
    return {
      form,
      isValid: form.useStore(state => state.isValid),
      nameRules: { onChange: rules(required) }
    };
  },
  data() {
    return {
      color: null
    };
  },
  computed: {
    presetColors() {
      return [
        '#F44E3B',
        '#FB9E00',
        '#FCDC00',
        '#68BC00',
        '#16A5A5',
        '#009CE0',
        '#7B10D8',
        '#F06292',
        '#000',
        '#777',
        '#FFF'
      ];
    }
  },
  methods: {
    randomColor() {
      return '#' + (Math.random().toString(16) + '0000000').slice(2, 8);
    },
    async save() {
      if (!await validateForm(this.form)) {
        return;
      }

      if (this.term) {
        this.update();
      } else {
        this.create();
      }
    },
    async create() {
      try {
        let term = await new Term({
          name: this.form.state.values.name,
          color: tinycolor(this.color).toHexString(),
          ontology: this.ontology.id
        }).save();
        this.$notify({ type: 'success', text: this.$t('notif-success-term-creation') });
        this.$emit('newTerm', term);
        this.$emit('close');
      } catch (error) {
        console.log(error);
        this.$notify({ type: 'error', text: this.$t('notif-error-term-creation') });
      }
    },
    async update() {
      let term = new Term(this.term);
      term.color = tinycolor(this.color).toHexString();
      term.name = this.form.state.values.name;
      try {
        await term.save();
        this.$notify({ type: 'success', text: this.$t('notif-success-term-update') });
        this.$emit('updateTerm', term);
        this.$emit('close');
      } catch (error) {
        console.log(error);
        this.$notify({ type: 'error', text: this.$t('notif-error-term-update') });
      }
    }
  },
  created() {
    this.color = this.term ? this.term.color : this.randomColor();
  }
};
</script>

<style>
.term-modal .vc-sketch-picker {
  width: auto;
  box-shadow: 0 2px 3px rgba(10, 10, 10, 0.1), 0 0 0 1px rgba(10, 10, 10, 0.1);
}

.term-modal .vc-sketch-picker .active-color {
  box-shadow: inset 0 0 0 1px rgba(10, 10, 10, 0.1);
}

.term-modal .vc-sketch-picker .saturation {
  padding-bottom: 15vh;
}
</style>
