import { createRouter, createWebHistory } from 'vue-router';

import Account from '@/components/user/Account.vue';
import AdminPanel from '@/components/admin/AdminPanel.vue';
import AdvancedSearch from '@/components/search/AdvancedSearch.vue';
import AppConfigurationPage from '@/components/appengine/AppConfigurationPage.vue';
import AppDashboardPage from '@/components/appengine/AppDashboardPage.vue';
import AppInfoPage from '@/components/appengine/AppInfoPage.vue';
import AppLayout from '@/components/appengine/AppLayout.vue';
import AppLocalPage from '@/components/appengine/AppLocalPage.vue';
import AppStorePage from '@/components/appengine/AppStorePage.vue';
import CytomineProject from '@/components/project/CytomineProject.vue';
import CytomineStorage from '@/components/storage/CytomineStorage.vue';
import CytomineViewer from '@/components/viewer/CytomineViewer.vue';
import GlobalDashboard from '@/components/GlobalDashboard.vue';
import ImageInformation from '@/components/image/ImageInformation.vue';
import ListAnnotations from '@/components/annotations/ListAnnotations.vue';
import ListImageGroups from '@/components/image-group/ListImageGroups.vue';
import ListImages from '@/components/image/ListImages.vue';
import ListOntologies from '@/components/ontology/ListOntologies.vue';
import ListProjects from '@/components/project/ListProjects.vue';
import MemberActivityDetails from '@/components/project/activity/MemberActivityDetails.vue';
import PageNotFound from '@/components/PageNotFound.vue';
import ProjectActivity from '@/components/project/ProjectActivity.vue';
import ProjectConfiguration from '@/components/project/ProjectConfiguration.vue';
import ProjectHome from '@/components/project/ProjectHome.vue';
import ProjectInformation from '@/components/project/ProjectInformation.vue';
import UserActivity from '@/components/user/UserActivity.vue';
import UserHistoryPage from '@/components/user/UserHistoryPage.vue';

const routes = [
  {
    path: '/',
    component: GlobalDashboard,
  },
  {
    path: '/projects',
    component: ListProjects,
  },
  {
    path: '/storage',
    component: CytomineStorage,
  },
  {
    path: '/ontology/:idOntology?',
    component: ListOntologies,
  },
  {
    path: '/advanced-search/:searchString?',
    component: AdvancedSearch,
  },
  {
    path: '/account',
    component: Account,
  },
  {
    path: '/project/:idProject',
    component: CytomineProject,
    children: [
      {
        path: '',
        component: ProjectHome,
      },
      {
        path: 'images',
        component: ListImages,
      },
      {
        path: 'image-groups',
        component: ListImageGroups,
      },
      {
        path: 'image/:idImages',
        component: CytomineViewer,
      },
      {
        path: 'image/:idImages/slice/:idSlices',
        component: CytomineViewer,
      },
      {
        path: 'image/:idImage/information',
        component: ImageInformation,
      },
      {
        path: 'image/:idImages/annotation/:idAnnotation',
        component: CytomineViewer,
      },
      {
        path: 'image/:idImages/slice/:idSlices/annotation/:idAnnotation',
        component: CytomineViewer,
      },
      {
        path: 'annotations',
        component: ListAnnotations,
      },
      {
        name: 'app-dashboard',
        path: 'apps',
        component: AppDashboardPage,
      },
      {
        path: 'activity',
        component: ProjectActivity,
      },
      {
        path: 'activity/user/:idUser',
        component: MemberActivityDetails,
      },
      {
        path: 'information',
        component: ProjectInformation,
      },
      {
        path: 'configuration',
        component: ProjectConfiguration,
      },
      {
        path: ':pathMatch(.*)*',
        component: PageNotFound
      }
    ]
  },
  {
    path: '/activity',
    component: UserActivity,
  },
  {
    path: '/admin',
    component: AdminPanel,
  },
  {
    path: '/apps',
    component: AppLayout,
    children: [
      {
        path: '',
        component: AppLocalPage,
      },
      {
        path: 'configuration',
        component: AppConfigurationPage,
      },
      {
        path: 'store',
        component: AppStorePage,
      },
      {
        path: ':namespace/:version',
        component: AppInfoPage,
      },
    ],
  },
  {
    path: '/history',
    component: UserHistoryPage,
  },
  {
    path: '/:pathMatch(.*)*',
    component: PageNotFound,
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes: routes,
  linkActiveClass: 'is-active',
});

export default router;
