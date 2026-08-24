import { config } from '@vue/test-utils';
import { configureCompat } from 'vue';

import optOutBuefyFromVModelCompat from '@/utils/buefy-compat.js';

// Mirror the runtime compat configuration applied in src/main.js. Without it the
// tests would run against Vue 3 semantics while the app runs in Vue 2 mode.
configureCompat({ MODE: 2, COMPONENT_V_MODEL: false });

// Vue Test Utils v2 stopped rendering the default slot of stubbed components.
// The suite was written against v1, where it was rendered, and many assertions
// read content projected into a stubbed child.
config.global.renderStubDefaultSlot = true;

// Same opt-out src/main.js applies, so `v-model` on Buefy components behaves in
// the tests the way it does in the app.
optOutBuefyFromVModelCompat();
