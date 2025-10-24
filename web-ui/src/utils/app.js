import {Cytomine} from '@/api';

export const UploadStatus = {
  PENDING: 'pending',
  UPLOADING: 'uploading',
  COMPLETED: 'completed',
  ERROR: 'errorr',
  CANCELLED: 'cancelled',
};

export async function installApp(app, notify, t) {
  try {
    const uri = `${app.namespace}/${app.version}`;
    await Cytomine.instance.api.post(`/app-engine/tasks/${uri}/install`);

    notify({type: 'success', text: t('notify-success-app-installation')});
    return true;
  } catch (error) {
    console.error('Failed to install app:', error);
    notify({type: 'error', text: t('notify-error-app-installation')});
    return false;
  }
}
