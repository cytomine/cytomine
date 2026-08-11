<template>
<div class="multiselect"
  :class="{'multiselect--active': activeSelector}"
  v-click-outside="() => activeSelector = false"
>

  <div class="multiselect__select" @click="activeSelector = !activeSelector"></div>

  <div class="multiselect__tags" @click="activeSelector=true">
    <div v-show="!activeSelector" class="multiselect__tags-wrap">
      <strong v-if="allSelected"> {{$t('all')}} </strong>
      <template v-else>
        <span v-for="(term, index) in displayedSelectedTerms" :key="term.id">
          <cytomine-term :term="term" />
          <span class="comma" v-if="index < displayedSelectedTerms.length - 1">,</span>
        </span>
        <strong v-if="countNotDisplayed > 0">
          {{ $tc("and-count-others", countNotDisplayed, {count: countNotDisplayed}) }}
        </strong>
      </template>
    </div>
    <input
      v-show="activeSelector"
      type="text"
      class="multiselect__input"
      :placeholder="$t('select-options')"
      ref="input"
      v-model="searchString"
    >
  </div>

  <div class="multiselect__content-wrapper" v-show="activeSelector">
    <ul v-if="multiple">
      <li class="multiselect__element multiselect__select-all" @click="selectAll()">
        <span :class="['multiselect__option', allSelected ? 'multiselect__option--selected' : '']">
          {{$t('select-all')}}
        </span>
      </li>
    </ul>
    <ontology-tree
      :ontology="ontology"
      :additionalNodes="additionalNodes"
      :startWithAdditionalNodes="startWithAdditionalNodes"
      :selectedNodes="selectedNodes"
      :searchString="searchString"
      :multipleSelection="multiple"
      @select="handleSelection()"
      @setSelectedNodes="nodes => $emit('setSelectedNodes', nodes)"
    >
      <template #no-result>
        <ul class="multiselect__content">
          <li @click="selectAll()">
            <span class="multiselect__option">
              {{$t('no-result')}}
            </span>
          </li>
        </ul>
      </template>
    </ontology-tree>
  </div>
</div>
</template>

<script>
import OntologyTree from './OntologyTree.vue';
import CytomineTerm from './CytomineTerm.vue';
import { getAllTerms } from '@/utils/ontology-utils';

export default {
  name: 'ontology-tree-multiselect',
  components: {
    OntologyTree,
    CytomineTerm
  },
  model: {
    prop: 'selectedNodes',
    event: 'setSelectedNodes'
  },
  props: {
    ontology: { type: Object },
    additionalNodes: { type: Array, default: () => [] },
    startWithAdditionalNodes: { type: Boolean, default: false },
    selectedNodes: { type: Array, default: () => [] },
    multiple: { type: Boolean, default: true }
  },
  data() {
    return {
      activeSelector: false,
      searchString: '',
      maxNbDisplayed: 3
    };
  },
  computed: {
    allTerms() {
      if (!this.ontology) {
        return [];
      }
      let terms = this.additionalNodes.map(node => node);
      terms.push(...getAllTerms(this.ontology));
      return terms;
    },
    allSelected() {
      return this.allTerms.length === this.selectedNodes.length;
    },
    displayedSelectedTerms() {
      let ids = this.selectedNodes.slice(0, this.maxNbDisplayed);
      return ids.map(id => this.allTerms.find(term => term.id === id));
    },
    countNotDisplayed() {
      return this.selectedNodes.length - this.maxNbDisplayed;
    }
  },
  watch: {
    async activeSelector(val) {
      if (val) {
        await this.$nextTick();
        this.$refs.input.focus();
      }
    }
  },
  methods: {
    selectAll() {
      this.$emit('setSelectedNodes', this.allSelected ? [] : this.allTerms.map(term => term.id));
    },

    handleSelection() {
      if (!this.multiple) {
        this.activeSelector = false;
      }
    }
  }
};
</script>
