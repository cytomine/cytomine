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
        <span v-for="(track, index) in displayedSelectedTracks" :key="track.id">
          <cytomine-track :track="track" />
          <span class="comma" v-if="index < displayedSelectedTracks.length - 1">,</span>
        </span>
        <strong v-if="countNotDisplayed > 0">
          {{ $t("and-count-others", countNotDisplayed, {count: countNotDisplayed}) }}
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
    <track-tree
      :tracks="tracks"
      :additionalNodes="additionalNodes"
      :startWithAdditionalNodes="startWithAdditionalNodes"
      :selectedNodes="selectedNodes"
      :searchString="searchString"
      :multipleSelection="multiple"
      @select="handleSelection()"
      @update:selectedNodes="nodes => $emit('update:selectedNodes', nodes)"
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
    </track-tree>
  </div>
</div>
</template>

<script>
import TrackTree from './TrackTree.vue';
import CytomineTrack from './CytomineTrack.vue';

export default {
  name: 'track-tree-multiselect',
  components: {
    TrackTree,
    CytomineTrack
  },
  emits: ['update:selectedNodes'],
  props: {
    tracks: { type: Array },
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
    allTracks() {
      if (!this.tracks) {
        return [];
      }
      let tracks = this.additionalNodes.map(node => node);
      tracks.push(...this.tracks);
      return tracks;
    },
    allSelected() {
      return this.allTracks.length === this.selectedNodes.length;
    },
    displayedSelectedTracks() {
      let ids = this.selectedNodes.slice(0, this.maxNbDisplayed);
      return ids.map(id => this.allTracks.find(track => track.id === id));
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
      this.$emit('update:selectedNodes', this.allSelected ? [] : this.allTracks.map(track => track.id));
    },

    handleSelection() {
      if (!this.multiple) {
        this.activeSelector = false;
      }
    }
  }
};
</script>
