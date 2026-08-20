import os
from distutils.util import strtobool

if __name__ == "__main__":
    import uvicorn

    log_config = "logging.yml"
    debug = bool(strtobool(os.getenv('DEBUG', 'false')))
    if debug:
        log_config = "logging-debug.yml"
    log_config = os.getenv('LOG_CONFIG_FILE', log_config)

    uvicorn.run(
        "pims.application:app",
        host="0.0.0.0",
        port=5000,
        log_config=log_config,
        reload=debug
    )
