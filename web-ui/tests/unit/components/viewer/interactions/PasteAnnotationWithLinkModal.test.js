import {flushPromises, mount} from '@vue/test-utils';
import Buefy from 'buefy';

import PasteAnnotationWithLinkModal from '@/components/viewer/interactions/PasteAnnotationWithLinkModal.vue';

vi.mock('ol/format/WKT', () => ({__esModule: true, default: vi.fn()}));

vi.mock('@/api', () => ({
  ImageGroup: {
    fetch: vi.fn(async () => ({
      name: 'group',
      imageInstances: [{id: 1}, {id: 2}, {id: 3}],
    })),
  },
  ImageInstanceCollection: vi.fn(),
  AnnotationGroup: vi.fn(),
  AnnotationCollection: vi.fn(),
  Annotation: vi.fn(),
  AnnotationLink: vi.fn(),
}));

// Issue 10 replaced `@change.native` on the two "check all" boxes with
// `@update:model-value`, the event `b-checkbox` actually declares. Neither the
// build nor lint can tell whether the handler still runs.
describe('PasteAnnotationWithLinkModal.vue', () => {
  const createWrapper = async () => {
    const wrapper = mount(PasteAnnotationWithLinkModal, {
      props: {index: '0'},
      global: {
        plugins: [Buefy],
        stubs: {'annotation-links-preview': true, 'image-name': true},
        mocks: {
          $t: key => key,
          $store: {
            commit: vi.fn(),
            getters: {
              'currentUser/user': {id: 1},
              'currentProject/currentViewerModule': 'viewer/',
              'currentProject/imageModule': () => 'image/',
              'image/imageGroupId': 7,
              'currentProject/currentViewer': {
                copiedAnnot: {id: 100, image: 1, group: null, annotationLink: []},
                copiedAnnotImageInstance: {id: 1},
                images: {
                  0: {imageInstance: {id: 1}, imageGroupLink: {group: 7}, view: {}},
                  1: {imageInstance: {id: 2}, imageGroupLink: {group: 7}, view: {}},
                },
              },
            },
          },
        },
      },
    });
    await flushPromises();
    return wrapper;
  };

  // Image 2 is in the group and open in the viewer; image 3 is in the group
  // only. Image 1 holds the copied annotation, so it is not eligible.
  it('renders one "check all" box per section', async () => {
    const wrapper = await createWrapper();

    expect(wrapper.findAll('.field.header')).toHaveLength(2);
  });

  it('ticks every in-viewer image when its "check all" box is toggled', async () => {
    const wrapper = await createWrapper();

    await wrapper.findAll('.field.header input[type="checkbox"]')[0].setValue(true);

    expect(wrapper.vm.checkedInViewer).toEqual([2]);
  });

  it('ticks every not-in-viewer image when its "check all" box is toggled', async () => {
    const wrapper = await createWrapper();

    await wrapper.findAll('.field.header input[type="checkbox"]')[1].setValue(true);

    expect(wrapper.vm.checkedNotInViewer).toEqual([3]);
  });

  it('unticks them again when the box is toggled back', async () => {
    const wrapper = await createWrapper();

    const checkAll = wrapper.findAll('.field.header input[type="checkbox"]')[0];
    await checkAll.setValue(true);
    await checkAll.setValue(false);

    expect(wrapper.vm.checkedInViewer).toEqual([]);
  });
});
