import { shallowMount } from '@vue/test-utils';
import Buefy from 'buefy';

import { Cytomine } from '@/api';
import eventBus from '@/utils/event-bus';

import MetadataPanel from '@/components/viewer/panels/MetadataPanel';

vi.mock('@/utils/event-bus', () => ({
  default: {
    on: vi.fn(),
    off: vi.fn(),
    emit: vi.fn(),
  }
}));

vi.mock('@/api', () => ({
  Cytomine: {
    instance: {
      api: {
        get: vi.fn(() => Promise.resolve({
          data: {
            collection: [
              { namespace: 'ns1', key: 'key1', value: 'value1' },
              { namespace: 'ns2', key: 'key2', value: 'value2' }
            ]
          }
        }))
      }
    }
  }
}));

vi.mock('@/utils/string-utils', () => ({
  getWildcardRegexp: vi.fn((str) => new RegExp(str, 'i'))
}));

describe('MetadataPanel.vue', () => {
  let wrapper;

  beforeEach(async () => {

    wrapper = shallowMount(MetadataPanel, {
      props: { index: '0' },
      global: {
        plugins: [Buefy],
        mocks: {
          $t: (message) => message,
          $store: {
            getters: {
              'currentProject/currentViewer': {
                images: [{ imageInstance: { id: 123 } }]
              }
            }
          }
        }
      }
    });

    await wrapper.vm.$nextTick();
  });

  it('should fetch metadata on creation', async () => {
    expect(Cytomine.instance.api.get).toHaveBeenCalledWith('imageinstance/123/metadata.json');
    expect(wrapper.vm.metadata.length).toBe(2);
  });

  it('should filter metadata based on search string', async () => {
    wrapper.setData({ searchString: 'key1' });

    await wrapper.vm.$nextTick();

    expect(wrapper.vm.filteredProps).toHaveLength(1);
    expect(wrapper.vm.filteredProps[0].key).toBe('key1');
  });

  it('should emit close event when closeMetadata is called', () => {
    wrapper.vm.closeMetadata();

    expect(eventBus.emit).toHaveBeenCalledWith('close-metadata');
  });
});
