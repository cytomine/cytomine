import {shallowMount} from '@vue/test-utils';

import LayersPanel from '@/components/viewer/panels/LayersPanel.vue';

const mockNotify = jest.fn();

jest.mock('@/api', () => ({
  Cytomine: {
    instance: {
      api: {
        get: jest.fn(() => Promise.resolve({
          data: [
            {'id': 142, 'name': 'task-run-f3e78101-d123-4c0c-8c05-4ca64232023c'},
            {'id': 283, 'name': 'task-run-f3e78101-d123-4c0c-8c05-4ca64232023c'},
          ]
        }))
      }
    }
  }
}));

describe('LayersPanel.vue', () => {
  const mockImageWrapper = {
    imageInstance: {id: 42, name: 'mock-image'},
    activeSlices: [
      {
        id: 1,
        fetchAnnotationsIndex: jest.fn(() =>
          Promise.resolve([
            {id: 'a1', user: 10, name: 'annotation-1'},
            {id: 'a2', user: 11, name: 'annotation-2'},
          ])
        ),
      },
      {
        id: 2,
        fetchAnnotationsIndex: jest.fn(() =>
          Promise.resolve([
            {id: 'b1', user: 10, name: 'annotation-3'},
            {id: 'b2', user: 12, name: 'annotation-4'},
          ])
        ),
      },
    ],
    activePanel: 'main',
    style: {
      layersOpacity: 0.7,
    },
    layers: {
      selectedLayers: [{id: 10, fullName: 'User A'}],
    },
    review: {
      reviewMode: false,
    },
    images: [],
  };

  const mockProject = {
    id: 999,
    name: 'Mock Project',
    clone: jest.fn(() => ({
      id: 1000,
      name: 'Cloned Project',
    })),
    fetchUserLayers: jest.fn(() => ({array: []})),
  };

  const createWrapper = () => {
    return shallowMount(LayersPanel, {
      propsData: {
        index: '0',
      },
      mocks: {
        $eventBus: {
          $on: jest.fn(),
          $off: jest.fn(),
        },
        $notify: mockNotify,
        $store: {
          commit: jest.fn(),
          getters: {
            'currentProject/canEditLayer': () => () => true,
            'currentProject/project': mockProject,
            'currentProject/currentViewer': {images: [mockImageWrapper]},
            'currentProject/imageModule': jest.fn(() => 'mock-module/'),
          },
          state: {
            currentProject: {
              project: mockProject,
            },
          },
        },
        $t: (key) => key,
      },
      stubs: {
        'b-checkbox': true,
        'b-field': true,
        'b-message': true,
        'b-select': {
          props: ['placeholder', 'value'],
          template: '<select><option disabled>{{ placeholder }}</option></select>',
        },
      },
    });
  };

  it('should render translated message', () => {
    const wrapper = createWrapper();

    expect(wrapper.text()).toContain('annotation-layers');
    expect(wrapper.text()).toContain('select-layer');
    expect(wrapper.text()).toContain('button-add');
    expect(wrapper.text()).toContain('layers-opacity');
  });
});
