import Model from './model.js';

export default class Storage extends Model {
  /** @inheritdoc */
  static get callbackIdentifier() {
    return 'storage';
  }

  /** @inheritdoc */
  _initProperties() {
    super._initProperties();

    this.name = null;
    this.userId = null;
  }
}
