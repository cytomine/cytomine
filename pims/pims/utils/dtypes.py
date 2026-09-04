import numpy as np


def dtype_to_bits(dtype) -> int:
    """Get number of bits for a dtype-like (Numpy or string datatype)."""
    if type(dtype) is str:
        dtype = np.dtype(dtype)
    return dtype.type(0).nbytes * 8


def bits_to_str_dtype(bits: int) -> str:
    """
    Get the required (string) datatype for data encoded on given bits.

    Parameters
    ----------
    bits
        Number of bits used to encode data

    Returns
    -------
    str_dtype
        Datatype (in string format) for given `bits`
    """
    if bits > 16:
        return 'uint32'
    elif bits > 8:
        return 'uint16'
    else:
        return 'uint8'


def np_dtype(bits: int) -> np.dtype:
    """
    Get Numpy datatype for data encoded on given bits.

    Parameters
    ----------
    bits
        Number of bits used to encode data

    Returns
    -------
    dtype
        Numpy datatype for given `bits`
    """
    return np.dtype(bits_to_str_dtype(bits))
