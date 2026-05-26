import Cytomine from '../cytomine.js';
import Collection from './collection.js';
import Annotation from '../models/annotation.js';

export default class AnnotationCollection extends Collection {

  /** @inheritdoc */
  _initProperties() {
    this.showDefault = true;
    this.showBasic = null;
    this.showMeta = null;
    this.showWKT = null;
    this.showGIS = null;
    this.showTerm = null;
    this.showUser = null;
    this.showImage = null;
    this.showSlice = null;

    this.reviewed = null;
    this.notReviewedOnly = null;
    this.reviewUsers = null;

    this.project = null;
    this.image = null;
    this.images = null;

    this.slice = null;
    this.slices = null;

    this.user = null;
    this.users = null;

    this.kmeans = null;

    this.term = null;
    this.terms = null;
    this.suggestedTerm = null;
    this.noTerm = null;
    this.multipleTerm = null;

    this.bbox = null;
    this.bboxAnnotation = null;
    this.baseAnnotation = null;
    this.maxDistanceBaseAnnotation = null;

    this.afterThan = null;
    this.beforeThan = null;
  }

  /** @override */
  async _doFetch() {
    // in large projects, URL can become very long if performed with GET => use POST instead
    let {data} = await Cytomine.instance.api.post('annotation/search.json', this.getParameters());
    return data;
  }

  /**
   * Downloads the collection under the provided format
   * @param   {String} [format="pdf"] The format of the file to download ("csv", "xls" or "pdf")
   */
  download(format = 'pdf') {
    if (!this.project) {
      throw new Error('Cannot construct download if no project ID is provided.');
    }

    let paramsBody = {format};
    const paramFields = ['reviewed', 'terms', 'users', 'reviewUsers', 'images', 'noTerm', 'multipleTerms', 'afterThan', 'beforeThan'];
    paramFields.forEach(param => {
      if (this[param] !== null) {
        paramsBody[param] = this[param];
      }
    });

    Cytomine.instance.api.post(
      `project/${this.project}/annotation/download`,
      paramsBody,
      {responseType: 'blob'}
    ).then(response => {
      const cd = response.headers?.['content-disposition'];
      const filename = this.getFilenameFromContentDisposition(cd);
      this.triggerBlobDownload(response.data, filename);
    });
  }

  triggerBlobDownload(blob, filename) {
    const blobUrl = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = blobUrl;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();

    window.URL.revokeObjectURL(blobUrl);
  }

  getFilenameFromContentDisposition(contentDisposition) {
    const starMatch = contentDisposition.match(/filename\*\s*=\s*([^;]+)/i);
    if (starMatch) {
      const value = starMatch[1].trim();
      const unquoted = value.replace(/^"(.*)"$/, '$1');
      const parts = unquoted.split("''");
      if (parts.length === 2) {
        try {
          return decodeURIComponent(parts[1]);
        } catch {
          return parts[1];
        }
      }
      return unquoted;
    }

    // Basic filename=
    const match = contentDisposition.match(/filename\s*=\s*([^;]+)/i);
    if (!match) {
      return null;
    }

    return match[1].trim().replace(/^"(.*)"$/, '$1');
  }

  /**
   * Validate or reject all annotations belonging to the provided image and user layers
   *
   * @param {Boolean} accept      If true, all targetted annotations will be validated ; if false, they will all be rejected
   * @param {Number} image        The identifier of the image
   * @param {Array<Number>} users The identifiers of the users whose annotation layers must be accepted or rejected
   * @param {Number} task         The identifier of the Cytomine task to use
   */
  static async reviewAll({accept, image, users, task} = {}) {
    let uri = `imageinstance/${image}/annotation/review.json?users=${users.join(',')}&task=${task}`;
    if (accept) {
      await Cytomine.instance.api.post(uri);
    } else {
      await Cytomine.instance.api.delete(uri);
    }
  }

  /** @inheritdoc */
  static get model() {
    return Annotation;
  }

  /** @inheritdoc */
  static get allowedFilters() {
    return [null];
  }

  /** @inheritdoc */
  static get isSaveAllowed() {
    return true;
  }
}
