import {Cytomine} from '@/api';

export const UploadStatus = {
  CANCELLED: 'cancelled',
  COMPLETED: 'completed',
  ERROR: 'error',
  PENDING: 'pending',
  UPLOADING: 'uploading',
};

export const BINARY_TYPES = ['file', 'image'];

export async function deleteApp(app, notify, t) {
  try {
    const uri = `${app.namespace}/${app.version}`;
    await Cytomine.instance.api.delete(`/app-engine/tasks/${uri}`);

    notify({type: 'success', text: t('notify-success-app-deletion')});
  } catch (error) {
    console.error('Failed to delete app:', error);
    notify({type: 'error', text: t('notify-error-app-deletion')});
  }
}

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

export async function hasBinaryType(input) {
  const typeId = input.type.id === 'array' ? input.type.subType.id : input.type.id;
  return BINARY_TYPES.includes(typeId);
}
