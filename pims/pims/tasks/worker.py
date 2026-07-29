from cytomine import Cytomine

# Quick hack to avoid circular imports
try:
    from pims.application import app as _  # noqa
except:  # noqa
    pass
# ----

from pims.api.exceptions import AuthenticationException
from pims.importer.importer import run_import as run_import_
from pims.tasks.queue import celery_app


@celery_app.task
def run_import_with_cytomine(cytomine_auth, filepath, name, cytomine_listener, prefer_copy):
    with Cytomine(*cytomine_auth, configure_logging=False) as c:
        if not c.current_user:
            raise AuthenticationException("PIMS authentication to Cytomine failed.")

        run_import_(
            filepath, name,
            extra_listeners=[cytomine_listener], prefer_copy=prefer_copy
        )


def run_import_with_cytomine_fallback(
    cytomine_auth, filepath, name, cytomine_listener, prefer_copy
):
    run_import_(
        filepath, name,
        extra_listeners=[cytomine_listener], prefer_copy=prefer_copy
    )

@celery_app.task
def run_import(filepath, name, prefer_copy):
    run_import_fallback(filepath, name, prefer_copy=prefer_copy)


def run_import_fallback(filepath, name, prefer_copy):
    run_import_(filepath, name, prefer_copy=prefer_copy)