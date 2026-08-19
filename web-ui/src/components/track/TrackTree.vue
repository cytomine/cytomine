<template>
  <div class="track-tree" :class="{selector: allowSelection, draggable: allowDrag, editable: allowEdition}">
    <Draggable
      ref="tree"
      :key="treeKey"
      v-model="treeNodes"
      children-key="children"
      :indent="20"
      :default-open="true"
      :disable-drag="!allowDrag"
      @after-drop="onAfterDrop"
      v-slot="{ node, stat, indentStyle }"
    >
      <div v-if="!node.hidden" class="tree-node-item" :class="{'is-selected': isSelected(node)}" :style="indentStyle">
        <span class="tree-toggle-wrap" @click.stop="stat.open = !stat.open">
          <i
            v-if="stat.children.length > 0"
            :class="['tree-toggle', 'fas', stat.open ? 'fa-angle-down' : 'fa-angle-right']"
          ></i>
        </span>

        <div class="tree-selector" @click="toggleSelect(node)">
          <i class="tree-checkbox"
             v-if="allowSelection"
             :class="classNames(node)">
          </i>
          <cytomine-track :track="node" />
        </div>

        <div class="tree-sidebar">
          <slot name="custom-sidebar" :track="node">
            <div v-if="allowEdition" class="buttons">
              <button class="button is-small" @click.stop="startTrackUpdate(node)">
              <span class="icon is-small">
                <i class="fas fa-edit"></i>
              </span>
              </button>
              <button class="button is-small" @click.stop="confirmTrackDeletion(node)">
              <span class="icon is-small">
                <i class="far fa-trash-alt"></i>
              </span>
              </button>
            </div>
          </slot>
        </div>
      </div>
      <div v-else></div>
    </Draggable>

    <slot v-if="noResult" name="no-result">
      <em class="has-text-grey no-result">{{$t('no-result')}}</em>
    </slot>

    <div v-if="allowEdition || allowNew" class="add-track-container">
      <button class="button is-small" @click="startTrackCreation()">{{$t('add-track')}}</button>
    </div>

  </div>
</template>

<script>
import { Draggable, dragContext } from '@he-tree/vue';

import CytomineTrack from './CytomineTrack.vue';
import TrackModal from './TrackModal.vue';
import { Track } from '@/api';

export default {
  name: 'track-tree',
  emits: ['update:selectedNodes', 'select', 'unselect', 'newTrack', 'updatedTrack', 'deletedTrack'],
  props: {
    tracks: { type: Array },
    additionalNodes: { type: Array, default: () => [] },
    startWithAdditionalNodes: { type: Boolean, default: false },
    searchString: { type: String, default: '' },
    selectedNodes: { type: Array, default: () => [] },
    allowSelection: { type: Boolean, default: true },
    multipleSelection: { type: Boolean, default: true },
    allowDrag: { type: Boolean, default: false },
    allowEdition: { type: Boolean, default: false },
    allowNew: { type: Boolean, default: false },
    image: { type: Object, default: null } //Cannot be null if allowNew
  },
  components: {
    CytomineTrack,
    Draggable,
  },
  data() {
    return {
      treeNodes: [],
      treeKey: 0,
      internalSelectedNodes: [],
      editedNode: null
    };
  },
  computed: {
    lowCaseSearchString() {
      return this.searchString.toLowerCase();
    },
    noResult() {
      return this.treeNodes.every(node => node.hidden);
    }
  },
  watch: {
    tracks() {
      this.makeTree();
    },
    additionalNodes() {
      this.makeTree();
    },
    selectedNodes() {
      this.internalSelectedNodes = this.selectedNodes.slice();
    },
    lowCaseSearchString() {
      this.filter();
    }
  },
  methods: {
    makeTree() {
      if (!this.tracks) {
        this.treeNodes = [];
        this.treeKey++;
        return;
      }

      let nodes = this.createSubTree(this.tracks.slice());
      let additionalNodes = this.createSubTree(this.additionalNodes.slice());
      this.treeNodes = this.startWithAdditionalNodes ? additionalNodes.concat(nodes) : nodes.concat(additionalNodes);

      this.filter();
      this.treeKey++;
    },

    createSubTree(tracks) {
      return tracks.map(track => this.createNode(track));
    },

    createNode(track) {
      return {
        id: track.id,
        name: track.name,
        color: track.color,
        parent: track.parent,
        image: track.image,
        hidden: false,
        children: track.children && track.children.length > 0 ? this.createSubTree(track.children) : []
      };
    },

    filter() {
      let str = this.lowCaseSearchString;
      this.applyToAllNodes(node => {
        let match = node.name.toLowerCase().indexOf(str) >= 0;
        if (node.children && node.children.length > 0) {
          let matchInChildren = node.children.some(child => !child.hidden); // OK because applyToAllNodes performs bottom-up operations
          match = match || matchInChildren;
        }
        node.hidden = !match;
      });
      this.refreshExpansion();
    },

    refreshExpansion() {
      let tree = this.$refs.tree;
      if (!tree || !tree.statsFlat) {
        return;
      }
      tree.statsFlat.forEach(stat => {
        if (stat.children.length > 0) {
          stat.open = stat.children.some(child => !child.data.hidden);
        }
      });
    },

    isSelected(node) {
      return node.id != null && this.internalSelectedNodes.includes(node.id);
    },

    classNames(node) {
      let selected = this.isSelected(node);
      if (this.multipleSelection) {
        return selected ? ['fas', 'fa-check-square'] : ['far', 'fa-square'];
      } else {
        return selected ? ['fas', 'fa-dot-circle'] : ['far', 'fa-circle'];
      }
    },

    toggleSelect(node) {
      if (!this.allowSelection) {
        return;
      }

      if (this.multipleSelection) {
        let indexSelected = this.internalSelectedNodes.indexOf(node.id);
        if (indexSelected >= 0) {
          this.internalSelectedNodes.splice(indexSelected, 1);
          this.$emit('unselect', node.id);
        } else {
          this.internalSelectedNodes.push(node.id);
          this.$emit('select', node.id);
        }
      } else {
        this.internalSelectedNodes = [node.id];
        this.$emit('select', node.id);
      }
      this.$emit('update:selectedNodes', this.internalSelectedNodes);
    },

    applyToAllNodes(fct, nodes = this.treeNodes) {
      nodes.forEach(node => {
        if (node.children) {
          this.applyToAllNodes(fct, node.children);
        }
        fct(node);
      });
    },

    async onAfterDrop() {
      let dragNode = dragContext.dragNode;
      if (!dragNode) {
        return;
      }
      let node = dragNode.data;
      let newParentId = dragNode.parent ? dragNode.parent.data.id : null;

      if ((node.parent ?? null) === newParentId) {
        this.$notify({ type: 'warn', text: this.$t('notif-warn-track-tree-order-not-persisted') });
        return;
      }

      try {
        await new Track({
          id: node.id,
          name: node.name,
          color: node.color,
          parent: node.parent,
          image: node.image
        }).changeParent(newParentId);
        node.parent = newParentId;
      } catch (error) {
        console.log(error);
        this.$notify({ type: 'error', text: this.$t('notif-error-track-tree-update') });
      }
    },

    startTrackCreation() {
      this.editedNode = null;
      this.openModal();
    },
    createTrack(track) {
      this.$refs.tree.add(this.createNode(track));
      this.$emit('newTrack', track);
    },

    startTrackUpdate(node) {
      this.editedNode = node;
      this.openModal();
    },
    updateTrack(track) {
      Object.assign(this.editedNode, { name: track.name, color: track.color });
      this.$emit('updatedTrack', track);
    },

    openModal() {
      this.$buefy.modal.open({
        component: TrackModal,
        props: {
          track: this.editedNode ? this.editedNode : null,
          image: this.image
        },
        events: {
          newTrack: this.createTrack,
          updateTrack: this.updateTrack
        },
        hasModalCard: true
      });
    },

    confirmTrackDeletion(node) {
      this.$buefy.dialog.confirm({
        title: this.$t('confirm-deletion'),
        message: this.$t('confirm-deletion-track', { name: node.name }),
        type: 'is-danger',
        confirmText: this.$t('button-confirm'),
        cancelText: this.$t('button-cancel'),
        onConfirm: () => this.deleteTrack(node)
      });
    },
    async deleteTrack(node) {
      try {
        await Track.delete(node.id);
        this.$refs.tree.remove(this.$refs.tree.getStat(node));
        this.$emit('deletedTrack', node.id);
      } catch (error) {
        console.log(error);
        this.$notify({ type: 'error', text: this.$t('notif-error-track-deletion') });
      }
    }
  },
  created() {
    this.internalSelectedNodes = this.selectedNodes.slice();
    this.makeTree();
  }
};
</script>

<style scoped>
  .add-track-container {
    text-align: center;
    margin-top: 0.5em;
    margin-bottom: 0.5em;
  }
</style>


<style>
.track-tree {
  padding: 0 0 2px 0;
}

.track-tree .tree-node-item {
  display: flex;
  align-items: center;
  line-height: 2.2;
  font-size: 0.9rem;
}

.track-tree .tree-toggle-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  flex-shrink: 0;
}

.track-tree.selector .tree-toggle-wrap {
  width: 24px;
}

.track-tree .tree-toggle {
  cursor: pointer;
}

.track-tree .tree-checkbox {
  margin-right: 10px;
  color: rgba(0, 0, 0, 0.2);
  font-size: 1rem;
}

.track-tree.selector .tree-node-item:hover {
  background: rgba(0, 0, 0, 0.05);
}

.track-tree.selector .tree-node-item.is-selected {
  background: rgba(0, 0, 0, 0.05);
  font-weight: 600;
}

.track-tree .tree-node-item.is-selected .tree-checkbox {
  color: #61b2e8;
}

.track-tree .tree-selector {
  display: flex;
  align-items: center;
  flex-grow: 1;
  min-width: 0;
}

.track-tree.selector .tree-selector {
  cursor: pointer;
}

.track-tree .tree-selector:hover .tree-checkbox {
  color: #61b2e8;
}

.track-tree .no-result {
  margin-left: 20px;
  line-height: 1.5;
  font-size: 0.9rem;
}

.track-tree .buttons, .track-tree .button {
  margin-bottom: 0 !important;
}

.track-tree.editable .tree-sidebar {
  padding-left: 20px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}
</style>
