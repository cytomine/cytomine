<template>
  <cytomine-modal-card :title="$t('about-cytomine')" @close="$parent.close()">
    <template>
      <img class="logo" src="@/assets/logo.svg">

      <dl>
        <dt>{{ $t('version') }}</dt>
        <dd>{{ version || '?' }}</dd>

        <dt>{{ $t('description') }}</dt>
        <dd>{{ $t('cytomine-description') }}</dd>

        <dt>{{ $t('funding') }}</dt>
        <dd>
          <i18n path="cytomine-funding">
            <a place="link" href="https://uliege.cytomine.org/#fundings" target="_blank">
              https://uliege.cytomine.org/#fundings
            </a>
          </i18n>
        </dd>

        <dt>{{ $t('website') }}</dt>
        <dd>
          <i18n path="cytomine-website-info">
            <a place="websiteLink" href="https://uliege.cytomine.org/" target="_blank">
              https://uliege.cytomine.org/
            </a>
          </i18n>
        </dd>

        <dt>{{ $t('source-code') }}</dt>
        <dd>
          <i18n path="source-code-info">
            <a place="link" href="https://github.com/cytomine/cytomine" target="_blank">
              {{ $t('github-repository') }}
            </a>
          </i18n>
        </dd>

        <dt>{{ $t('documentation') }}</dt>
        <dd>
          <i18n path="documentation-info">
            <a place="docLink" href="https://doc.uliege.cytomine.org/" target="_blank">
              https://doc.uliege.cytomine.org/
            </a>
          </i18n>
        </dd>

        <dt>{{ $t('contact') }}</dt>
        <dd>
          <i18n path="contact-info">
            <a place="contactMail" href="mailto:uliege@cytomine.org">
              uliege@cytomine.org
            </a>
          </i18n>
        </dd>
      </dl>
    </template>
  </cytomine-modal-card>
</template>

<script>
import constants from '@/utils/constants.js';

import CytomineModalCard from '@/components/utils/CytomineModalCard';

export default {
  name: 'AboutCytomineModal',
  components: {
    CytomineModalCard,
  },
  data() {
    return {
      version: constants.CYTOMINE_COMMERCIAL_VERSION,
    };
  },
};
</script>

<style scoped>
.logo {
  display: block;
  margin: 0 auto;
  max-height: 10rem;
}

.modal-card,
.modal-card-body {
  width: 50vw;
  min-width: 700px;
  font-weight: normal;
}

dt {
  float: left;
  width: 12rem;
  text-align: right;
  padding-right: 1rem;
  clear: both;
  font-weight: 600;
}

dd {
  margin-left: 14rem;
  background-color: #fff;
  margin-bottom: 0.75rem;
}
</style>
