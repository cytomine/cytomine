from __future__ import annotations


class ImageArea:
    """
    ImageArea represent a rectangular region of an image.
    This class allows to perform geometry query between two areas to detect overlap, coverage, ...
    """

    def __init__(self, coord: tuple[int, int], size: tuple[int, int]):
        """
        Create a new ImageArea using the given coordinates and size.

        Parameters
        ----------
        coord
            Tuple containing the coordinates of the top left point of the area
        size
            Tuple containing the size of the area
        """

        self.coord = coord
        self.size = size

    @classmethod
    def from_region(cls, region) -> ImageArea:
        """
        Create a new ImageArea from the given Region object.

        Parameters
        ----------
        region
            The Region object of the pyramid tier
        Returns
        -------
        area
            The newly created area object
        """
        return ImageArea((region.left, region.top), (region.width, region.height))

    @classmethod
    def from_scaled_area(cls, area: ImageArea, order: int) -> ImageArea:
        """
        Create a new ImageArea representing the area at the given order

        Parameters
        ----------
        area
            ImageArea object to scale
        order
            Order scale to apply to the received coordinates
        Returns
        -------
        area
            The newly created area object
        """
        scale = 2 ** order
        coord = (area.coord[0] * scale, area.coord[1] * scale)
        size = (area.size[0] * scale, area.size[1] * scale)
        return ImageArea(coord, size)

    @property
    def top_left_coord(self) -> tuple[int, int]:
        """
        Return the coordinates of the top left pixel as a tuple
        """
        return self.coord

    @property
    def bottom_right_coord(self) -> tuple[int, int]:
        """
        Return the coordinates of the bottom right pixel as a tuple
        """
        return (self.coord[0] + self.size[0] - 1, self.coord[1] + self.size[1] - 1)

    def to_scale(self, order: int) -> ImageArea:
        """
        Create an ImageArea object from this area and optionally scale it to the given order

        Parameters
        ----------
        order
            Optional order scale to apply when generating the new area
        Returns
        -------
        area
            Scaled area
        """

        scale = 2 ** order
        coord = (self.coord[0] / scale, self.coord[1] / scale)
        size = (self.size[0] / scale, self.size[1] / scale)
        return ImageArea(coord, size)

    def intersect(self, other: ImageArea) -> bool:
        """
        Check if the given ImageArea intersect this area

        Parameters
        ----------
        other
            Image area to check the intersection with
        Returns
        -------
        intersect
            Boolean indicating if the two areas overlap
        """

        dx = (min(self.coord[0] + self.size[0] - 1, other.coord[0] + other.size[0] - 1)
              - max(self.coord[0], other.coord[0]))
        dy = (min(self.coord[1] + self.size[1] - 1, other.coord[1] + other.size[1] - 1)
              - max(self.coord[1], other.coord[1]))
        return dx >= 0 and dy >= 0

    def union(self, other: ImageArea) -> ImageArea:
        """
        Create an ImageArea representing the intersection of this area and the given ImageArea

        Parameters
        ----------
        other
            Image area to intersect
        Returns
        -------
        area
            New area corresponding to the intersection of the two areas
        """

        min_x = max(self.coord[0], other.coord[0])
        max_x = min(self.coord[0] + self.size[0] - 1, other.coord[0] + other.size[0] - 1)
        min_y = max(self.coord[1], other.coord[1])
        max_y = min(self.coord[1] + self.size[1] - 1, other.coord[1] + other.size[1] - 1)
        return ImageArea((min_x, min_y), (max_x - min_x + 1, max_y - min_y + 1))

    def cover(self, area: ImageArea) -> bool:
        """
        Check if this ImageArea entirely cover the given ImageArea

        Parameters
        ----------
        other
            Image area to check the coverage
        Returns
        -------
        cover
            Boolean indicating if this area covers entirely the given area
        """

        return (self.coord[0] <= area.coord[0] and
                self.coord[1] <= area.coord[1] and
                self.coord[0] + self.size[0] >= area.coord[0] + area.size[0] and
                self.coord[1] + self.size[1] >= area.coord[1] + area.size[1]
                )

    def __str__(self):
        return f"(pos: {self.coord}, size: {self.size})"
