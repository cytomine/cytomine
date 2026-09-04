import Cytomine from '../cytomine.js';
import Model from './model.js';

export default class UploadedFile extends Model {
  /** @inheritdoc */
  static get callbackIdentifier() {
    return 'uploadedfile';
  }

  /** @inheritdoc */
  _initProperties() {
    super._initProperties();

    this.user = null;
    this.projects = null;
    this.storage = null;

    this.path = null;
    this.filename = null;
    this.originalFilename = null;
    this.ext = null;

    this.contentType = null;
    this.size = null;

    this.storageId = null;
    this.userId = null;

    this.parent = null;
    this.thumbnailUrl = null;

    this.status = null;
  }

  get downloadURL() {
    return `${Cytomine.instance.host}${Cytomine.instance.basePath}uploadedfile/${this.id}/download`;
  }
}
