import typing

from starlette.background import BackgroundTask, BackgroundTasks
from starlette.responses import Response


def add_background_task(
    response: Response, func: typing.Callable,
    *args: typing.Any, **kwargs: typing.Any
):
    if isinstance(response.background, BackgroundTasks):
        response.background.add_task(func, *args, **kwargs)
    elif isinstance(response.background, BackgroundTask):
        tasks = BackgroundTasks([response.background])
        tasks.add_task(func, *args, **kwargs)
        response.background = tasks
    else:
        tasks = BackgroundTasks()
        tasks.add_task(func, *args, **kwargs)
        response.background = tasks

    return response
