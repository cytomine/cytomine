import Vue from 'vue';

import i18n from '@/lang.js';

Vue.use(i18n);

const LOCALES = ['en', 'fr', 'es', 'nl'];

function mountText(template) {
  const el = document.createElement('div');
  document.body.appendChild(el);
  return new Vue({ render: h => h({ template }) }).$mount(el);
}

function flatten(messages, prefix = '') {
  return Object.entries(messages).flatMap(([key, value]) => {
    const path = prefix ? `${prefix}.${key}` : key;
    return typeof value === 'object' && value !== null
      ? flatten(value, path)
      : [[path, value]];
  });
}

describe('lang.js', () => {
  let vm;

  beforeEach(() => {
    vm = mountText('<p></p>');
    vm.$i18n.locale = 'en';
  });

  it('should translate a plain key and interpolate named arguments', () => {
    expect(vm.$t('select-options')).toBe('Select options');
    expect(vm.$t('activity-of-user', { username: 'bob' })).toBe('Activity of bob');
  });

  it('should pick the plural form from the count argument', () => {
    const key = 'and-count-others';

    expect(vm.$t(key, 0, { count: 0 })).toBe('-');
    expect(vm.$t(key, 1, { count: 1 })).toBe('and one other');
    expect(vm.$t(key, 5, { count: 5 })).toBe('and 5 others');
  });

  it('should switch locale and fall back to the fallback locale', async () => {
    const wrapper = mountText('<p>{{ $t("select-options") }}</p>');
    expect(wrapper.$el.textContent).toBe('Select options');

    wrapper.$i18n.locale = 'fr';
    await wrapper.$nextTick();

    expect(wrapper.$el.textContent).toBe('Sélectionner les options');
    expect(wrapper.$i18n.fallbackLocale).toBe('en');
  });

  it('should return the key itself when it is missing', () => {
    expect(vm.$t('no-such-key-at-all')).toBe('no-such-key-at-all');
  });

  it.each(LOCALES)('should compile every %s message', (locale) => {
    vm.$i18n.locale = locale;
    const failures = [];

    for (const [key, value] of flatten(i18n.global.getLocaleMessage(locale))) {
      try {
        vm.$t(key);
        if (String(value).includes('|')) {
          [0, 1, 2, 5].forEach(count => vm.$t(key, count, { count }));
        }
      } catch (error) {
        failures.push(`${key}: ${error.message.split('\n')[0]}`);
      }
    }

    expect(failures).toEqual([]);
  });
});
