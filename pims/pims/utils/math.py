def get_rationed_resizing(
    resized: int | float,
    length: int,
    other_length: int,
) -> tuple[int, int]:
    """
    Get resized lengths for `length` and `other_length` according to
    the ratio between `resized` and `length`.

    Parameters
    ----------
    resized : int or float
        Already resized length. If float, it is the ratio.
    length : int
        Non-resized length related to `resized`.
    other_length : int
        Other non-resized length to resize according the ratio.

    Returns
    -------
    resized : int
        First resized length according ratio.
    other_resized : int
        Other resized length according ratio.
    """
    ratio = resized if isinstance(resized, float) else resized / length
    resized = resized if isinstance(resized, int) else round(ratio * length)
    other_resized = round(ratio * other_length)
    return resized, other_resized


def max_intensity(bitdepth: int, count: bool = False):
    """
    Get maximum intensity for a given bitdepth.
    To get number of possible intensities, set `count` to True.
    """
    mi = 2**bitdepth
    if not count:
        mi -= 1
    return mi
