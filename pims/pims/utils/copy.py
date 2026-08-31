import copy
from itertools import chain


class SafelyCopiable:
    UNSAFE_DEEPCOPY_ATTRS = ('_cache',)

    def _copy__new(self):
        cls = self.__class__
        return cls.__new__(cls)

    def __copy__(self):
        # Create a new instance
        result = self._copy__new()

        # Copy all attributes
        result.__dict__.update(self.__dict__)

        # Get all __slots__ of the derived class
        slots = chain.from_iterable(
            getattr(s, '__slots__', [])
            for s in self.__class__.__mro__
        )

        # Copy all slot attributes
        for var in slots:
            setattr(result, var, copy.copy(getattr(self, var)))

        # Return updated instance
        return result

    def _deepcopy_new(self):
        return self._copy__new()

    def __deepcopy__(self, memo):
        # Create a new instance
        result = self._deepcopy_new()

        # Don't copy self reference
        memo[id(self)] = result

        # Don't copy the cache - if it exists
        for unsafe_attr in self.UNSAFE_DEEPCOPY_ATTRS:
            if hasattr(self, unsafe_attr):
                unsafe_var = getattr(self, unsafe_attr)
                memo[id(unsafe_var)] = unsafe_var.__new__(type(unsafe_var))

        # Deep copy all other attributes
        for k, v in self.__dict__.items():
            setattr(result, k, copy.deepcopy(v, memo))  # noqa

        # Get all __slots__ of the derived class
        slots = chain.from_iterable(
            getattr(s, '__slots__', [])
            for s in self.__class__.__mro__
        )

        # Deep copy all other attributes
        for var in slots:
            try:
                setattr(
                    result, var,
                    copy.deepcopy(getattr(self, var), memo)  # noqa
                )
            except AttributeError:
                pass

        # Return updated instance
        return result
