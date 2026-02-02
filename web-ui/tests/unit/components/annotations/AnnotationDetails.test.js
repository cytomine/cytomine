import {createLocalVue, shallowMount} from '@vue/test-utils';
import Buefy from 'buefy';
import VueRouter from 'vue-router';

import {AnnotationType} from '@/api';
import AnnotationDetails from '@/components/annotations/AnnotationDetails';

jest.mock('@/api', () => ({
  AnnotationType: {
    USER: 'USER',
    REVIEWED: 'REVIEWED',
  },
  AnnotationTerm: jest.fn().mockImplementation(() => ({
    save: jest.fn().mockResolvedValue({})
  })),
  AnnotationTrack: jest.fn().mockImplementation(() => ({
    save: jest.fn().mockResolvedValue({})
  })),
  AnnotationCommentCollection: {
    fetchAll: jest.fn().mockResolvedValue({array: []})
  },
  PropertyCollection: {
    fetchAll: jest.fn().mockResolvedValue({array: []})
  }
}));

const localVue = createLocalVue();
localVue.use(Buefy);
localVue.use(VueRouter);

localVue.directive('click-outside', {
  bind() {},
  unbind() {},
});
localVue.filter('moment', (value) => value);

const mockAnnotation = {
  id: 1,
  project: 100,
  image: 200,
  slice: 300,
  user: 1,
  type: AnnotationType.USER,
  area: 1500.5,
  areaUnit: 'µm²',
  perimeter: 150.3,
  perimeterUnit: 'µm',
  created: 1234567890000,
  location: 'POLYGON((0 0, 10 0, 10 10, 0 10, 0 0))',
  userByTerm: [
    {term: 10, user: [1]},
    {term: 11, user: [1]}
  ],
  annotationTrack: [
    {track: 20}
  ],
  url: 'http://example.com/annotation/1'
};

const mockStore = {
  state: {
    currentProject: {
      configUI: {
        'project-explore-annotation-geometry-info': true,
        'project-explore-annotation-description': true,
        'project-explore-annotation-terms': true,
        'project-explore-annotation-tracks': true,
        'project-explore-annotation-tags': true,
        'project-explore-annotation-properties': true,
        'project-explore-annotation-attached-files': true,
        'project-explore-annotation-linked-annotations': true,
        'project-explore-annotation-creation-info': true,
        'project-explore-annotation-comments': true,
      },
      ontology: {id: 1, name: 'Test Ontology'},
    },
    currentUser: {
      account: {isDeveloper: false},
      shortTermToken: 'test-token',
    }
  },
  getters: {
    'currentProject/canEditAnnot': () => true,
  },
  dispatch: jest.fn(),
};

const mockUsers = [
  {id: 123, name: 'User Admin'},
  {id: 124, name: 'Test user'},
];

describe('AnnotationDetails.vue', () => {

  const createWrapper = () => {
    return shallowMount(AnnotationDetails, {
      localVue,
      propsData: {
        annotation: mockAnnotation,
        terms: [
          {id: 10, name: 'Cell'},
          {id: 11, name: 'Nucleus'},
        ],
        tracks: [
          {id: 20, name: 'Track 1', image: 200},
        ],
        users: mockUsers,
        images: [
          {
            id: 200,
            instanceFilename: 'test-image.jpg',
            baseImage: 201,
            depth: 1,
            duration: 1,
            channels: 3,
            inReview: false,
          }
        ],
        slices: [
          {id: 300, channel: 0},
        ],
        profiles: [
          {image: 201},
        ],
        showImageInfo: false,
        showChannelInfo: false,
        showComments: false,
      },
      mocks: {
        $eventBus: {
          $emit: jest.fn(),
        },
        $notify: jest.fn(),
        $store: mockStore,
        $t: (key) => key,
      },
      stubs: {
        'attached-files': true,
        'annotation-links-preview': true,
        'b-tag': true,
        'b-field': true,
        'b-input': true,
        'channel-name': true,
        'cytomine-description': true,
        'cytomine-properties': true,
        'cytomine-tags': true,
        'cytomine-term': true,
        'cytomine-track': true,
        'image-name': true,
        'ontology-tree': true,
        'router-link': true,
        'track-tree': true,
      },
    });
  };

  it('should render mandatory fields', () => {
    const wrapper = createWrapper();

    expect(wrapper.exists()).toBe(true);
    expect(wrapper.text()).toContain('area');
    expect(wrapper.text()).toContain('perimeter');
    expect(wrapper.text()).toContain('description');
    expect(wrapper.text()).toContain('terms');
    expect(wrapper.text()).toContain('tags');
    expect(wrapper.text()).toContain('properties');
    expect(wrapper.text()).toContain('attached-files');
    expect(wrapper.text()).toContain('similar-annotations');
    expect(wrapper.text()).toContain('linked-annotations');
    expect(wrapper.text()).toContain('created-by');
    expect(wrapper.text()).toContain('created-on');
    expect(wrapper.text()).toContain('button-center-view-on-annot');
    expect(wrapper.text()).toContain('button-view-crop');
    expect(wrapper.text()).toContain('button-copy-url');
    expect(wrapper.text()).toContain('button-delete');
  });
});
