import Cytomine from '../cytomine.js';
import Collection from './collection.js';
import Project from '../models/project.js';

export default class ProjectCollection extends Collection {

  /**
     * @returns {Object} The filters available server-side, as returned in the filters field of the last search performed
     *                   with this collection (null if no search was performed yet)
     */
  get availableFilters() {
    return this._availableFilters ?? null;
  }

  /** @inheritdoc */
  static get model() {
    return Project;
  }

  /** @inheritdoc */
  static get allowedFilters() {
    return [null, 'user', 'ontology'];
  }

  /**
   * @static Fetch last opened projects
   *
   * @param {number} [max=0]    The maximum number of items to retrieve
   * @param {number} [offset=0] The offset
   *
   * @returns {Array<{id: Number, date: String, opened: Boolean}>} The last opened projects
   */
  static async fetchLastOpened(max = 0, offset = 0) {
    let { data } = await Cytomine.instance.api.get(`project/method/lastopened.json?max=${max}&offset=${offset}`);
    return data.collection;
  }

  // HACK: remove (temporary hack due to lack of consistency in API endpoint)
  /** @inheritdoc */
  get uri() {
    if (this._filter.key === 'user' && this._filter.value && this.light) {
      return `user/${this._filter.value}/project/light.json`;
    } else {
      return super.uri;
    }
  }

  /** @inheritdoc */
  getParameters() {
    let params = super.getParameters();
    if (params.sort) {
      params.sort = `${params.sort},${params.order || 'asc'}`;
    }
    delete params.order;
    return params;
  }

  /**
   * @static Fetch the filters available for the projects search endpoint (values currently present server-side and
   * bounds of the countable attributes)
   *
   * @returns {Object} The available filters, as provided in the filters field of the projects search response
   */
  static async fetchAvailableFilters() {
    let { data } = await Cytomine.instance.api.post('projects/search', null, { params: { size: 1 } });
    return data.filters;
  }

  /** @inheritdoc */
  _initProperties() {
    this.light = null; // used iff filter on user ; if true, only id and name properties will be filled
    this.admin = null; // used iff filter on user ; if true, only returns the projects in which user is admin
    this.withLastActivity = null;
    this.withMembersCount = null;
    this.filters = null;
    this._availableFilters = null;
  }

  /** @inheritdoc */
  async _doFetch() {
    if (this._filter.key) {
      return super._doFetch();
    }
    let params = {};
    if (this.max > 0) {
      params.page = this._curPage;
      params.size = this.max;
    }
    if (this.sort) {
      params.sort = `${this.sort},${this.order || 'asc'}`;
    }
    let { data } = await Cytomine.instance.api.post('projects/search', this.filters ?? undefined, { params });
    this._availableFilters = data.filters;
    return data;
  }

}
