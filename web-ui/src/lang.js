import moment from 'moment';
import Vue from 'vue';
import VueI18n from 'vue-i18n';

import en from './locales/json/en.i18n.json';
import es from './locales/json/es.i18n.json';
import fr from './locales/json/fr.i18n.json';
import nl from './locales/json/nl.i18n.json';

Vue.use(VueI18n);

export default new VueI18n({
  locale: 'en',
  fallbackLocale: 'en',
  messages: {en, es, fr, nl}
});

export const changeLanguageMixin = {
  methods: {
    changeLanguage(language) {
      language = language || this.$i18n.fallbackLocale;
      let locale = language.toLowerCase();
      this.$i18n.locale = locale;
      moment.locale(locale);
    }
  }
};
