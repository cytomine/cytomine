import { computed, isReactive } from 'vue';

import { createViewerContext } from '@/viewer-ol/context.js';

describe('viewer-ol/context', () => {
  it('should resolve a registered target by name', () => {
    const context = createViewerContext();
    const collection = { name: 'features' };

    context.register('select-target-0', collection);

    expect(context.resolve('select-target-0')).toBe(collection);
  });

  it('should not proxy the ol object it stores', () => {
    const context = createViewerContext();

    context.register('select-target-0', { name: 'features' });

    // Vue's deep proxies break ol objects and destroy rendering performance.
    expect(isReactive(context.resolve('select-target-0'))).toBe(false);
  });

  it('should return null for an unknown name', () => {
    expect(createViewerContext().resolve('select-target-0')).toBeNull();
  });

  it('should let a component that rendered first pick the target up', () => {
    const context = createViewerContext();
    const target = computed(() => context.resolve('select-target-0'));

    expect(target.value).toBeNull();

    context.register('select-target-0', { name: 'features' });

    expect(target.value).toEqual({ name: 'features' });
  });

  it('should forget a target on unregister', () => {
    const context = createViewerContext();
    context.register('select-target-0', { name: 'features' });

    context.unregister('select-target-0');

    expect(context.resolve('select-target-0')).toBeNull();
  });
});
