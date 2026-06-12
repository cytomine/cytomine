import axios from 'axios';

function ensureTrailingSlash(url) {
  return url.endsWith('/') ? url : url + '/';
}

export default class Cytomine {

  /**
   * @param {string} coreUrl Full URL for core API (e.g., http://core:8080/api/)
   * @param {string} iamUrl Full URL for IAM (e.g., http://iam:8080/iam/realms/cytomine/)
   * @param {function} [authorizationHeaderInterceptor] The Axios request interceptor for authorization header
   *
   * @returns {this} The singleton instance
   */
  constructor(coreUrl, iamUrl, authorizationHeaderInterceptor = null) {
    if (!Cytomine._instance) {
      this._coreUrl = ensureTrailingSlash(coreUrl);
      this._iamUrl = ensureTrailingSlash(iamUrl);

      const onRejectedResponseInterceptor = error => {
        error.message += ' - Response data: ' + JSON.stringify(error.response.data);
        return Promise.reject(error);
      };

      this.iam = axios.create({
        baseURL: this._iamUrl,
        withCredentials: true
      });
      if (authorizationHeaderInterceptor !== null) {
        this.iam.interceptors.request.use(authorizationHeaderInterceptor);
      }
      this.iam.interceptors.response.use(response => response, onRejectedResponseInterceptor);

      this.api = axios.create({
        baseURL: this._coreUrl,
        withCredentials: true
      });
      if (authorizationHeaderInterceptor !== null) {
        this.api.interceptors.request.use(authorizationHeaderInterceptor);
      }
      this.api.interceptors.response.use(response => response, onRejectedResponseInterceptor);
      this.lastCommand = null;

      Cytomine._instance = this;
    }

    return Cytomine._instance;
  }

  /**
   * @returns {this} The singleton instance
   */
  static get instance() {
    if (!Cytomine._instance) {
      throw new Error('No Cytomine instance was created.');
    }
    return Cytomine._instance;
  }

  /**
   * @returns {string} The core URL
   */
  get coreUrl() {
    return this._coreUrl;
  }

  /**
   * @returns {string} The IAM URL
   */
  get iamUrl() {
    return this._iamUrl;
  }

  /**
   * @returns {string} The host (protocol + hostname + port)
   */
  get host() {
    const url = new URL(this._coreUrl);
    return url.origin;
  }

  /**
   * @returns {string} The base path
   */
  get basePath() {
    const url = new URL(this._coreUrl);
    return url.pathname;
  }

  /**
   * Ping the server to get info
   *
   * @param {number}  [project]   The identifier of the active project
   * @returns {{alive, authenticated, version, serverURL, serverID, user}} The data returned by the server
   */
  async ping(project) {
    let {data} = await this.api.post('server/ping.json', {project});

    return data;
  }

  /**
   * Open an admin session
   * @returns {boolean} True if the current user is now connected as admin
   */
  async openAdminSession() {
    let {data} = await this.api.get('session/admin/open.json');
    return data.adminByNow;
  }

  /**
   * Close an admin session
   * @returns {boolean} True if the current user is no longer connected as admin
   */
  async closeAdminSession() {
    let {data} = await this.api.get('session/admin/close.json');
    return !data.adminByNow;
  }

  /**
   * Fetch the UI configuration for the current user
   * @param {number} [project] The identifier of the project to consider (if specified, in addition to the general UI
   *                           config, the UI config of the specified project will be returned)
   * @returns {Object} Set of properties describing which parts of the UI to display
   */
  async fetchUIConfigCurrentUser(project) {
    let params = {};
    if (project) {
      params.project = project;
    }

    let {data} = await this.api.get('custom-ui/config.json', {params});
    return data;
  }

  /**
   * Fetch a signature string for the current user
   * @param {string} method The request method action
   * @param {string} uri The request URI
   * @param {string} [queryString] The request query string
   * @param {string} [date] The request date
   * @param {string} [contentMD5] The request content MD5
   * @param {string} [contentType] The request content type
   * @returns {string} The generated signature
   */
  async fetchSignature({method, uri, queryString, date, contentMD5, contentType} = {}) {
    let params = {method, forwardURI: uri, queryString, date, 'content-MD5': contentMD5, 'content-type': contentType};

    let {data} = await this.api.get('signature.json', {params});
    return data.signature;
  }

  /**
   * Fetch total count of each model
   *
   * @returns {{users, projects, images, userAnnotations, terms, ontologies}}
   *          The total count for each model
   */
  async fetchTotalCounts() {
    let {data} = await this.api.get('stats/all.json');
    return data;
  }

  /**
   * Fetch stats of current activity
   *
   * @returns {{users, projects, mostActiveProject}} Stats related to current activity
   */
  async fetchCurrentStats() {
    let {data} = await this.api.get('stats/currentStats.json');
    return data;
  }

  /**
   * Fetch stats related to storage space
   *
   * @returns {{total, available, used, usedP}} Stats related to the storage
   */
  async fetchStorageStats() {
    let {data} = await this.api.get('stats/imageserver/total.json');
    return data;
  }

  /**
   * Undo a command
   *
   * @param {number} command The identifier of the command to undo ; if null, the last command will be undone
   *
   * @returns {Array<Object>} The collection of affected models
   */
  async undo(command = null) {
    let {data} = await this.api.get(`command/${command ? command + '/' : ''}undo.json`);
    return data.collection;
  }

  /**
   * Redo a command
   *
   * @param {number} command The identifier of the command to redo ; if null, the last undone command will be redone
   *
   * @returns {Array<Object>} The collection of affected models
   */
  async redo(command = null) {
    let {data} = await this.api.get(`command/${command ? command + '/' : ''}redo.json`);
    return data.collection;
  }
}
