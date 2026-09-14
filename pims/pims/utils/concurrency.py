import asyncio
import threading
from functools import partial, wraps
import anyio
from anyio import CapacityLimiter

_pims_thread_limiter = CapacityLimiter(100)  # Tune based on load testing


def _with_thread_name(name, func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        threading.current_thread().name = name
        return func(*args, **kwargs)
    return wrapper


async def exec_func_async(func, *args, **kwargs):
    if asyncio.iscoroutinefunction(func):
        return await func(*args, **kwargs)

    # Wrap the function so the worker thread sets its log name upon execution
    named_func = _with_thread_name("Import Pool Worker", partial(func, *args, **kwargs))

    return await anyio.to_thread.run_sync(
        named_func,
        limiter=_pims_thread_limiter
    )