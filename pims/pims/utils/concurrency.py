import asyncio

from starlette.concurrency import run_in_threadpool


async def exec_func_async(func, *args, **kwargs):
    is_async = asyncio.iscoroutinefunction(func)
    if is_async:
        return await func(*args, **kwargs)
    else:
        return await run_in_threadpool(func, *args, **kwargs)