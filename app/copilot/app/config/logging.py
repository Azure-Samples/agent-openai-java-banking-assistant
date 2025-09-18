import logging
import logging.config
import os
import yaml
from pathlib import Path
from typing import Dict, Any, Optional
from opentelemetry._logs import set_logger_provider
from opentelemetry.sdk._logs import LoggerProvider, LoggingHandler
from opentelemetry.sdk._logs.export import BatchLogRecordProcessor
from opentelemetry.sdk.resources import Resource
from azure.monitor.opentelemetry.exporter import AzureMonitorLogExporter
from opentelemetry.semconv.resource import ResourceAttributes

from app.config.settings import settings


def get_logging_config_path(profile: Optional[str] = None) -> Optional[Path]:
    """Get the path to the logging configuration file based on the profile.
    
    Args:
        profile: The environment profile (dev, prod, test). If None, uses PROFILE env var.
        
    Returns:
        Path to the logging configuration file, or None if not found.
    """
    if profile is None:
        profile = os.getenv("PROFILE")
        if profile is None:
            print("No PROFILE environment variable set, using default logging configuration.")
            return Path(__file__).parent.parent.joinpath("logging-default.yaml")
    
    print(f"App profile is: {profile}")
    
    # Get the project root directory (two levels up from this file)
    config_dir =  Path(__file__).parent.parent
    
    # Try profile-specific config first, then fall back to default
    profile_config = config_dir.joinpath(f"logging-{profile}.yaml")
    default_config = config_dir.joinpath("logging-default.yaml")
    
    if profile_config.exists():
        return profile_config
    elif default_config.exists():
        return default_config
    else:
        # If no config files exist, return None to use basic config
        return None


def load_logging_config(config_path: Optional[Path] = None) -> Dict[str, Any]:
    """Load logging configuration from YAML file.
    
    Args:
        config_path: Path to the logging configuration file.
        
    Returns:
        Dictionary containing logging configuration.
    """
    if config_path is None:
        config_path = get_logging_config_path()
    
    if config_path is None or not config_path.exists():
        # Return basic configuration if no file found
        return {
            "version": 1,
            "disable_existing_loggers": False,
            "formatters": {
                "default": {
                    "format": "%(asctime)s - %(name)s - %(levelname)s - %(message)s",
                    "datefmt": "%Y-%m-%d %H:%M:%S"
                }
            },
            "handlers": {
                "console": {
                    "class": "logging.StreamHandler",
                    "level": "INFO",
                    "formatter": "default",
                    "stream": "ext://sys.stdout"
                }
            },
            "root": {
                "level": "INFO",
                "handlers": ["console"]
            }
        }
    
    try:
        with open(config_path, 'r', encoding='utf-8') as f:
            config = yaml.safe_load(f)
        return config
    except Exception as e:
        print(f"Error loading logging config from {config_path}: {e}")
        # Return basic configuration on error
        return load_logging_config()

def _setup_azure_monitoring_logging() -> None:
    """Setup Azure monitoring logging."""
    exporters = []
    exporters.append(AzureMonitorLogExporter(connection_string=settings.APPLICATIONINSIGHTS_CONNECTION_STRING))

    resource = Resource.create({ResourceAttributes.SERVICE_NAME: "multi-agent-copilot-app"})
    # Create and set a global logger provider for the application.
    logger_provider = LoggerProvider(resource=resource)
    # Log processors are initialized with an exporter which is responsible
    # for sending the telemetry data to a particular backend.
    for log_exporter in exporters:
        logger_provider.add_log_record_processor(BatchLogRecordProcessor(log_exporter))
    # Sets the global default logger provider
    set_logger_provider(logger_provider)

    # Create a logging handler to write logging records, in OTLP format, to the exporter.
    handler = LoggingHandler()
    # Attach the handler to the root logger. `getLogger()` with no arguments returns the root logger.
    # Events from all child loggers will be processed by this handler.
    logger = logging.getLogger()
    logger.addHandler(handler)

def setup_logging(profile: Optional[str] = None) -> None:
    """Setup logging configuration based on the profile.
    
    Args:
        profile: The environment profile (dev, prod, test). If None, uses PROFILE env var.
    """
 
    # Load and apply logging configuration
    config_path = get_logging_config_path(profile)
    config = load_logging_config(config_path)
    
    try:
        logging.config.dictConfig(config)
        if config_path:
            print(f"Logging configured from: {config_path}")
        else:
            print("Logging configured with default settings")

    except Exception as e:
        print(f"Error configuring logging: {e}")
        # Fallback to basic configuration
        logging.basicConfig(level=logging.INFO)


def get_logger(name: Optional[str] = None) -> logging.Logger:
    """Get a logger instance.
    
    Args:
        name: Logger name. If None, uses the caller's module name.
        
    Returns:
        Logger instance.
    """
    if name is None:
        # Get the caller's module name
        import inspect
        frame = inspect.currentframe()
        if frame and frame.f_back:
            name = frame.f_back.f_globals.get('__name__', 'app')
        else:
            name = 'app'
    
    return logging.getLogger(name)