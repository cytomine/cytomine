<template>
<cytomine-modal-card :title="$t('annotation-comments')" @close="$parent.close()">
  <div v-if="!comments || !comments.length">
    <em class="has-text-grey">
      {{$t('no-annotation-comments')}}
    </em>
    <hr>
  </div>
  <template v-else>
    <div class="comments-wrapper">
      <div v-for="comment in comments" :key="comment.id">
        <p class="comment-sender is-size-7">
          <strong>{{ comment.senderName }}</strong>
          <span class="has-text-grey">{{formatMomentDate(Number(comment.created), 'll LT')}}</span>
        </p>
        <p class="comment-content">{{comment.comment}}</p>
        <hr>
      </div>
    </div>
  </template>

  <div v-show="!addingComment" class="has-text-centered">
    <button class="button is-link" v-if="!currentUser.guestByNow" @click="addingComment = true">{{$t('button-add-comment')}}</button>
  </div>

  <div v-show="addingComment">
    <h2>{{$t('add-new-comment')}}</h2>
    <b-field>
      <b-radio v-model="sendToAllMembers" :native-value="true" :disabled="members.length == 0">
        {{$t('send-to-all-project-members')}}
      </b-radio>
    </b-field>
    <b-field>
      <b-radio v-model="sendToAllMembers" :native-value="false">
        {{$t('send-to-some-members')}}
      </b-radio>
    </b-field>
    <field v-if="!sendToAllMembers" :form="form" name="members" :validators="requiredRule" v-slot="{field, state}">
      <b-field :type="{'is-danger': !!state.meta.errors.length}" :message="state.meta.errors[0]">
        <domain-tag-input :model-value="state.value" @update:model-value="field.handleChange" :domains="members" placeholder="search-user" searchedProperty="fullName" displayedProperty="fullName" />
      </b-field>
    </field>
    <field :form="form" name="comment" :validators="requiredRule" v-slot="{field, state}">
      <b-field :type="{'is-danger': !!state.meta.errors.length}" :message="state.meta.errors[0]">
        <b-input :model-value="state.value" @update:model-value="field.handleChange" type="textarea" :placeholder="$t('enter-comment')" rows="2" />
      </b-field>
    </field>
    <p class="buttons is-right are-small">
      <button class="button" @click="addingComment = false" :disabled="loading">
        {{$t('button-cancel')}}
      </button>
      <button class="button is-link" :class="{'is-loading': loading}"
        :disabled="loading || members.length == 0 || !isValid" @click="share()">
        {{$t('button-share')}}
      </button>
    </p>
  </div>
</cytomine-modal-card>
</template>

<script>
import { Field, useForm } from '@tanstack/vue-form';

import { get } from '@/utils/store-helpers';

import { AnnotationComment } from '@/api';
import { required, rules, validateForm } from '@/utils/form.js';
import DomainTagInput from '@/components/utils/DomainTagInput.vue';

import CytomineModalCard from '@/components/utils/CytomineModalCard.vue';
import { formatMomentDate } from '@/utils/date';

export default {
  name: 'annotation-comments-modal',
  components: {
    DomainTagInput,
    CytomineModalCard,
    Field
  },
  props: {
    annotation: Object,
    comments: Array
  },
  setup() {
    // The recipients field is only mounted when the author picked specific
    // members, and unmounting a `Field` deregisters it, so 'send to all' leaves
    // it out of validation.
    const form = useForm({ defaultValues: { members: [], comment: '' } });
    return {
      form,
      isValid: form.useStore(state => state.isValid),
      requiredRule: { onChange: rules(required) }
    };
  },
  data() {
    return {
      addingComment: false,
      sendToAllMembers: true,
      loading: false
    };
  },
  computed: {
    currentUser: get('currentUser/user'),
    allMembers: get('currentProject/members'),
    members() { // all project members except current user
      return this.allMembers.filter(member => member.id !== this.currentUser.id);
    },
    annotationURL() {
      let uri = `project/${this.annotation.project}/image/${this.annotation.image}/annotation/${this.annotation.id}`;
      return `${window.location.origin}/${uri}`;
    }
  },
  watch: {
    addingComment() {
      this.form.reset({ members: [], comment: '' });
      this.$nextTick(() => {
        this.sendToAllMembers = true;
      });
    }
  },
  methods: {
    formatMomentDate,
    async share() {
      if (!await validateForm(this.form)) {
        return;
      }

      this.loading = true;
      try {
        let sender = this.currentUser.fullName;
        let newComment = await new AnnotationComment({
          annotation: this.annotation,
          subject: `Cytomine: ${sender} commented an annotation`, // not translated because the content of the mail will be in english
          from: sender,
          receivers: (this.sendToAllMembers ? this.members : this.form.state.values.members).map(m => m.id),
          comment: this.form.state.values.comment,
          annotationURL: this.annotationURL,
          shareAnnotationURL: this.annotationURL + '?action=comments'
        }).save();
        this.$emit('addComment', newComment);
        this.$notify({ type: 'success', text: this.$t('notif-success-new-comment') });
        this.addingComment = false;
      } catch (error) {
        console.log(error);
        this.$notify({ type: 'error', text: this.$t('notif-error-new-comment') });
      }
      this.loading = false;
    },
  }
};
</script>

<style scoped>
:deep(.modal-card) {
  max-height: 80vh;
}

:deep(.modal-card-body) {
  display: flex;
  flex-direction: column;
}

.comments-wrapper {
  overflow: auto;
  margin-bottom: 1em;
  min-height: 5em;
}

.comment-sender strong {
  margin-right: 0.5em;
}

.comment-content {
  margin-top: 0.25em;
  margin-bottom: 0.5em;
}

:deep(textarea) {
  margin: 0.75em 0 0;
}

hr {
  margin: 0.75em 0 !important;
}

:deep(.dropdown-content) {
  max-height: 7em !important;
}
</style>
