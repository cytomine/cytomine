import router from '@/routes.js';

vi.mock('vue-slider-component', () => ({
  __esModule: true,
  default: { name: 'vue-slider', render: () => null },
}));

vi.mock('ol-rescale-feature', () => ({ __esModule: true, default: class RescaleFeature {} }));

import AppInfoPage from '@/components/appengine/AppInfoPage.vue';
import AppLayout from '@/components/appengine/AppLayout.vue';
import AppLocalPage from '@/components/appengine/AppLocalPage.vue';
import AppStorePage from '@/components/appengine/AppStorePage.vue';
import CytomineProject from '@/components/project/CytomineProject.vue';
import CytomineViewer from '@/components/viewer/CytomineViewer.vue';
import UserActivity from '@/components/user/UserActivity.vue';
import ListOntologies from '@/components/ontology/ListOntologies.vue';
import PageNotFound from '@/components/PageNotFound.vue';

async function navigate(location) {
  await router.push(location);
  return router.currentRoute.value;
}

describe('routes.js', () => {
  describe('router options', () => {
    it('should keep the hash-mode URLs the app has always had', () => {
      expect(router.resolve('/projects').href).toBe('/projects');
    });

    it('should keep is-active as the active-link class', () => {
      expect(router.options.linkActiveClass).toBe('is-active');
    });
  });

  describe('catch-all routes', () => {
    it('should render the not-found page for an unknown path', async () => {
      const route = await navigate('/no-such-page');

      expect(route.matched.at(-1).components.default).toBe(PageNotFound);
    });

    it('should render the not-found page inside a project for an unknown child path', async () => {
      const route = await navigate('/project/42/no-such-tab');

      expect(route.matched.map(record => record.components.default))
        .toEqual([CytomineProject, PageNotFound]);
      expect(route.params.idProject).toBe('42');
    });
  });

  describe('optional parameters', () => {
    it('should match the ontology list without an ontology', async () => {
      const route = await navigate('/ontology');

      expect(route.matched.at(-1).components.default).toBe(ListOntologies);
      expect(route.params.idOntology).toBeFalsy();
    });

    it('should match the ontology list with an ontology', async () => {
      const route = await navigate('/ontology/5');

      expect(route.params.idOntology).toBe('5');
    });
  });

  describe('viewer routes', () => {
    it('should carry the dash-separated image and slice ids', async () => {
      const route = await navigate('/project/1/image/2-3/slice/4-5');

      expect(route.matched.at(-1).components.default).toBe(CytomineViewer);
      expect(route.params).toMatchObject({ idProject: '1', idImages: '2-3', idSlices: '4-5' });
    });

    it('should carry the annotation id', async () => {
      const route = await navigate('/project/1/image/2/annotation/3');

      expect(route.matched.at(-1).components.default).toBe(CytomineViewer);
      expect(route.params).toMatchObject({ idImages: '2', idAnnotation: '3' });
    });
  });

  describe('the /apps section', () => {
    it('should show the installed apps as the default child of /apps', async () => {
      const route = await navigate('/apps');

      expect(route.matched.map(record => record.components.default))
        .toEqual([AppLayout, AppLocalPage]);
    });

    it('should show the store', async () => {
      const route = await navigate('/apps/store');

      expect(route.matched.map(record => record.components.default))
        .toEqual([AppLayout, AppStorePage]);
    });

    it('should show a single app by namespace and version', async () => {
      const route = await navigate('/apps/com.cytomine.test/1.2.0');

      expect(route.matched.at(-1).components.default).toBe(AppInfoPage);
      expect(route.params).toMatchObject({ namespace: 'com.cytomine.test', version: '1.2.0' });
    });
  });

  it.each([
    ['/admin', '?tab=users', '/admin?tab=users'],
    ['/project/7/configuration', '?tab=members', '/project/7/configuration?tab=members'],
    ['/project/7/activity', '?tab=analysis', '/project/7/activity?tab=analysis'],
  ])('should keep %s when pushing %s', async (from, query, expected) => {
    await navigate(from);

    expect((await navigate(query)).fullPath).toBe(expected);
  });

  it('should show the user activity page', async () => {
    const route = await navigate('/activity');

    expect(route.matched.at(-1).components.default).toBe(UserActivity);
  });

  it.each(['/userdashboard', '/upload', '/tabs-images-42', '/activity-42-'])(
    'should no longer know the retired URL %s',
    async (path) => {
      expect((await navigate(path)).matched.at(-1).components.default).toBe(PageNotFound);
    }
  );
});
