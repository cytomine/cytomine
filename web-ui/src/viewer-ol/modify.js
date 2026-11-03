/**
 * Replaces `src/vuelayers-suppl/modify-interaction/modify.js`.
 *
 * Dragging a corner of a rectangular annotation has to keep it rectangular: the
 * two neighbouring corners must follow, each on one axis only. ol's `Modify`
 * moves the grabbed vertex alone, so the two seams below are overridden.
 *
 * The old fork copied ol 5's whole `handleDownEvent`/`handleDragEvent` (234
 * lines of private internals). ol 6-10 split both into smaller methods, so this
 * version only overrides those two and delegates everything else — ring
 * closing, tracing, duplicate-change suppression — to `super`.
 */
import ModifyInteractionBase from 'ol/interaction/Modify';
import { equals as coordinatesEqual } from 'ol/coordinate';
import { getUid } from 'ol/util';
import { boundingExtent } from 'ol/extent';
import { isRectangle } from '@/utils/geometry-utils';

/**
 * The axis a rectangle drag segment is allowed to move along, by position in
 * `dragSegments_`:
 *
 * - `0`, `1`: the two segments meeting at the grabbed corner — moves freely.
 * - `2`, `3`: the corner sharing the grabbed corner's x — only x follows.
 * - `4`, `5`: the corner sharing the grabbed corner's y — only y follows.
 */
const FREE = undefined;
const X_ONLY = 0;
const Y_ONLY = 1;
const RECTANGLE_AXES = [FREE, FREE, X_ONLY, X_ONLY, Y_ONLY, Y_ONLY];

export default class ModifyInteraction extends ModifyInteractionBase {
  /**
   * @param {import('ol/coordinate').Coordinate} vertex Grabbed vertex.
   * @return {Array|null} Drag segments keeping the rectangle rectangular, or
   *     `null` when the grabbed vertex is not a rectangle corner.
   * @private
   */
  rectangleDragSegments_(vertex) {
    const match = this.rBush_
      .getInExtent(boundingExtent([vertex]))
      .find(sd => sd.geometry.getType() === 'Polygon' && isRectangle(sd.geometry));

    if (!match) {
      return null;
    }

    const uid = getUid(match.geometry);
    const ringSegments = this.rBush_
      .getInExtent(match.geometry.getExtent())
      .filter(sd => getUid(sd.geometry) === uid);

    const otherCorners = match.geometry
      .getCoordinates()[0]
      .filter(coord => !coordinatesEqual(coord, vertex));
    const isOtherCorner = coord => otherCorners.some(corner => coordinatesEqual(corner, coord));

    const predicates = [
      sd => coordinatesEqual(sd.segment[0], vertex),
      sd => coordinatesEqual(sd.segment[1], vertex),
      sd => sd.segment[0][0] === vertex[0] && sd.segment[0][1] !== vertex[1] && isOtherCorner(sd.segment[0]),
      sd => sd.segment[1][0] === vertex[0] && sd.segment[1][1] !== vertex[1] && isOtherCorner(sd.segment[1]),
      sd => sd.segment[0][1] === vertex[1] && sd.segment[0][0] !== vertex[0] && isOtherCorner(sd.segment[0]),
      sd => sd.segment[1][1] === vertex[1] && sd.segment[1][0] !== vertex[0] && isOtherCorner(sd.segment[1])
    ];

    const dragSegments = [];
    for (let i = 0; i < predicates.length; i++) {
      const segmentData = ringSegments.find(predicates[i]);
      if (!segmentData) {
        return null; // not a corner of this rectangle, leave it to ol
      }
      dragSegments.push([segmentData, i % 2, RECTANGLE_AXES[i]]);
    }

    return dragSegments;
  }

  /**
   * @override
   */
  findInsertVerticesAndUpdateDragSegments_(pixelCoordinate) {
    const insertVertices = super.findInsertVerticesAndUpdateDragSegments_(pixelCoordinate);

    if (!this.vertexFeature_) {
      return insertVertices;
    }

    const vertex = this.vertexFeature_.getGeometry().getCoordinates();
    const dragSegments = this.rectangleDragSegments_(vertex);
    if (!dragSegments) {
      return insertVertices;
    }

    this.dragSegments_.length = 0;
    this.dragSegments_.push(...dragSegments);
    // Adding a vertex would stop the shape being a rectangle.
    return undefined;
  }

  /**
   * @override
   */
  updateGeometry_(vertex, dragSegment) {
    const axis = dragSegment[2];
    if (axis === FREE) {
      return super.updateGeometry_(vertex, dragSegment);
    }

    // Only one component of the dragged vertex applies to this corner; the
    // other one stays where it is, which is what keeps the angles square.
    const segmentData = dragSegment[0];
    const corner = segmentData.segment[dragSegment[1]].slice();
    corner[axis] = vertex[axis];

    return super.updateGeometry_(corner, dragSegment);
  }
}
