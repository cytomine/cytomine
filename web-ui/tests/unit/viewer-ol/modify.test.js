import Collection from 'ol/Collection';
import Feature from 'ol/Feature';
import Polygon from 'ol/geom/Polygon';

import ModifyInteraction from '@/viewer-ol/modify.js';
import { isRectangle } from '@/utils/geometry-utils.js';

/**
 * The interaction's map-driven entry points (`handleDownEvent`,
 * `handleDragEvent`) need a rendered map, so these exercise the two seams the
 * fork actually overrides: picking the drag segments for a rectangle corner,
 * and writing a dragged vertex back one axis at a time.
 */
describe('viewer-ol/modify', () => {
  const RECTANGLE = [[[0, 0], [10, 0], [10, 5], [0, 5], [0, 0]]];

  const createInteraction = (coordinates = RECTANGLE) => {
    const feature = new Feature(new Polygon(coordinates));
    feature.setId(1);
    const interaction = new ModifyInteraction({ features: new Collection([feature]) });
    return { interaction, feature };
  };

  const drag = (interaction, from, to) => {
    const dragSegments = interaction.rectangleDragSegments_(from);
    dragSegments.forEach(dragSegment => interaction.updateGeometry_(to.slice(), dragSegment));
    return dragSegments;
  };

  describe('rectangleDragSegments_', () => {
    it('should drag the grabbed corner plus its two neighbours', () => {
      const { interaction } = createInteraction();

      const dragSegments = interaction.rectangleDragSegments_([0, 0]);

      // Two segments meet at the grabbed corner, and one at each neighbour.
      expect(dragSegments).toHaveLength(6);
      // The neighbours are constrained to one axis each, the corner is free.
      expect(dragSegments.map(([, , axis]) => axis)).toEqual([undefined, undefined, 0, 0, 1, 1]);
    });

    it('should leave a non-rectangular polygon to ol', () => {
      const { interaction } = createInteraction([[[0, 0], [10, 1], [10, 5], [0, 5], [0, 0]]]);

      expect(interaction.rectangleDragSegments_([0, 0])).toBeNull();
    });

    it('should leave a point that is not one of the corners to ol', () => {
      const { interaction } = createInteraction();

      expect(interaction.rectangleDragSegments_([5, 0])).toBeNull();
    });
  });

  describe('updateGeometry_', () => {
    it('should keep the shape rectangular when a corner is dragged', () => {
      const { interaction, feature } = createInteraction();

      drag(interaction, [0, 0], [-2, -3]);

      expect(feature.getGeometry().getCoordinates())
        .toEqual([[[-2, -3], [10, -3], [10, 5], [-2, 5], [-2, -3]]]);
      expect(isRectangle(feature.getGeometry())).toBe(true);
    });

    it('should keep the shape rectangular from any corner', () => {
      const { interaction, feature } = createInteraction();

      drag(interaction, [10, 5], [7, 9]);

      expect(isRectangle(feature.getGeometry())).toBe(true);
      expect(feature.getGeometry().getCoordinates())
        .toEqual([[[0, 0], [7, 0], [7, 9], [0, 9], [0, 0]]]);
    });

    it('should move a free vertex to the pointer, as ol does', () => {
      const { interaction, feature } = createInteraction();
      const dragSegments = interaction.rectangleDragSegments_([0, 0]);

      // Only the two unconstrained segments, i.e. plain ol behaviour.
      dragSegments.slice(0, 2).forEach(dragSegment => interaction.updateGeometry_([-2, -3], dragSegment));

      const ring = feature.getGeometry().getCoordinates()[0];
      expect(ring[0]).toEqual([-2, -3]);
      expect(ring[4]).toEqual([-2, -3]);
      // ...and on its own it stops being a rectangle, which is the whole point
      // of the fork.
      expect(isRectangle(feature.getGeometry())).toBe(false);
    });
  });
});
