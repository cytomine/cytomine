<template>
<div class="ontology-tree" :class="{selector: allowSelection, draggable: allowDrag, editable: allowEdition}">
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
        <cytomine-term :term="node" />
      </div>

      <div class="tree-sidebar">
        <slot name="custom-sidebar" :term="node">
          <div v-if="allowEdition" class="buttons">
            <button class="button is-small" @click.stop="startTermUpdate(node)">
              <span class="icon is-small">
                <i class="fas fa-edit"></i>
              </span>
            </button>
            <button class="button is-small" @click.stop="confirmTermDeletion(node)" :data-delete-term="node.name">
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

  <div v-if="allowEdition || allowNew" class="add-term-container">
    <button class="button is-small" @click="startTermCreation()">{{$t('add-term')}}</button>
  </div>

</div>
</template>

<script>
import { Draggable, dragContext } from '@he-tree/vue';

Draggable.compatConfig = { MODE: 3 };  // TODO: remove when removing @vue/compat

import CytomineTerm from './CytomineTerm.vue';
import TermModal from './TermModal.vue';
import { Term } from '@/api';
import { getWildcardRegexp } from '@/utils/string-utils';

export default {
  name: 'ontology-tree',
  emits: ['update:selectedNodes', 'select', 'unselect', 'newTerm'],
  props: {
    ontology: { type: Object },
    additionalNodes: { type: Array, default: () => [] },
    startWithAdditionalNodes: { type: Boolean, default: false },
    searchString: { type: String, default: '' },
    selectedNodes: { type: Array, default: () => [] },
    allowSelection: { type: Boolean, default: true },
    multipleSelection: { type: Boolean, default: true },
    allowDrag: { type: Boolean, default: false },
    allowEdition: { type: Boolean, default: false },
    allowNew: { type: Boolean, default: false },
    hiddenNodes: { type: Array, default: () => [] }
  },
  components: {
    CytomineTerm,
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
    regexp() {
      return getWildcardRegexp(this.searchString);
    },
    noResult() {
      return this.treeNodes.every(node => node.hidden);
    }
  },
  watch: {
    ontology() {
      this.makeTree();
    },
    additionalNodes() {
      this.makeTree();
    },
    selectedNodes() {
      this.internalSelectedNodes = this.selectedNodes.slice();
    },
    regexp() {
      this.filter();
    },
    hiddenNodes() {
      this.filter();
    }
  },
  methods: {
    makeTree() {
      let nodes = (this.ontology && this.ontology.children) ? this.createSubTree(this.ontology.children.array.slice()) : [];
      let additionalNodes = this.createSubTree(this.additionalNodes.slice());
      this.treeNodes = this.startWithAdditionalNodes ? additionalNodes.concat(nodes) : nodes.concat(additionalNodes);

      this.filter();
      this.treeKey++;
    },

    createSubTree(terms) {
      return terms.map(term => this.createNode(term));
    },

    createNode(term) {
      return {
        id: term.id,
        name: term.name,
        color: term.color,
        parent: term.parent,
        ontology: (this.ontology) ? this.ontology.id : null,
        hidden: false,
        children: term.children && term.children.length > 0 ? this.createSubTree(term.children) : []
      };
    },

    filter() {
      this.applyToAllNodes(node => {
        let match = this.regexp.test(node.name) && !this.hiddenNodes.includes(node.id);
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
        if (this.internalSelectedNodes.includes(node.id)) {
          this.internalSelectedNodes = [];
        } else {
          this.internalSelectedNodes = [node.id];
        }
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
        this.$notify({ type: 'warn', text: this.$t('notif-warn-ontology-tree-order-not-persisted') });
        return;
      }

      try {
        await new Term({
          id: node.id,
          name: node.name,
          color: node.color,
          parent: node.parent,
          ontology: node.ontology
        }).changeParent(newParentId);
        node.parent = newParentId;
      } catch (error) {
        console.log(error);
        this.$notify({ type: 'error', text: this.$t('notif-error-ontology-tree-update') });
      }
    },

    startTermCreation() {
      this.editedNode = null;
      this.openModal();
    },
    createTerm(term) {
      this.$refs.tree.add(this.createNode(term));
      this.$emit('newTerm', term);
    },

    startTermUpdate(node) {
      this.editedNode = node;
      this.openModal();
    },
    updateTerm(term) {
      Object.assign(this.editedNode, { name: term.name, color: term.color });
    },

    openModal() {
      this.$buefy.modal.open({
        component: TermModal,
        props: {
          term: this.editedNode ? this.editedNode : null,
          ontology: this.ontology
        },
        events: {
          newTerm: this.createTerm,
          updateTerm: this.updateTerm
        },
        hasModalCard: true
      });
    },

    confirmTermDeletion(node) {
      this.$buefy.dialog.confirm({
        title: this.$t('confirm-deletion'),
        message: this.$t('confirm-deletion-term', { name: node.name }),
        type: 'is-danger',
        confirmText: this.$t('button-confirm'),
        cancelText: this.$t('button-cancel'),
        onConfirm: () => this.deleteTerm(node)
      });
    },
    async deleteTerm(node) {
      try {
        await Term.delete(node.id);
        this.$refs.tree.remove(this.$refs.tree.getStat(node));
      } catch (error) {
        console.log(error);
        this.$notify({ type: 'error', text: this.$t('notif-error-term-deletion') });
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
.add-term-container {
  text-align: center;
  margin-top: 0.5em;
  margin-bottom: 0.5em;
}
</style>


<style>
.ontology-tree {
  padding: 0 0 2px 0;
}

.ontology-tree .tree-node-item {
  display: flex;
  align-items: center;
  line-height: 2.2;
  font-size: 0.9rem;
}

.ontology-tree .tree-toggle-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  flex-shrink: 0;
}

.ontology-tree.selector .tree-toggle-wrap {
  width: 24px;
}

.ontology-tree .tree-toggle {
  cursor: pointer;
}

.ontology-tree .tree-checkbox {
  margin-right: 10px;
  color: rgba(0, 0, 0, 0.2);
  font-size: 1rem;
}

.ontology-tree.selector .tree-node-item:hover {
  background: rgba(0, 0, 0, 0.05);
}

.ontology-tree.selector .tree-node-item.is-selected {
  background: rgba(0, 0, 0, 0.05);
  font-weight: 600;
}

.ontology-tree .tree-node-item.is-selected .tree-checkbox {
  color: #61b2e8;
}

.ontology-tree.selector .tree-selector {
  cursor: pointer;
}

.ontology-tree .tree-selector {
  display: flex;
  align-items: center;
  flex-grow: 1;
  min-width: 0; /* to allow correct handling of overflow-wrap */
}

.ontology-tree .tree-selector:hover .tree-checkbox {
  color: #61b2e8;
}

.ontology-tree .no-result {
  margin-left: 20px;
  line-height: 1.5;
  font-size: 0.9rem;
}

.ontology-tree .buttons, .ontology-tree .button {
  margin-bottom: 0 !important;
}

.ontology-tree.editable .tree-sidebar {
  width: 100px;
  padding-left: 20px;
  flex-shrink: 0;
  display: flex;
  align-items: top;
}
</style>
