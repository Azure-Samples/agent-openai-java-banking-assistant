from azure.monitor.opentelemetry.exporter import AzureMonitorLogExporter, AzureMonitorTraceExporter
from opentelemetry.sdk.resources import Resource
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.semconv.resource import ResourceAttributes
from opentelemetry.trace import set_tracer_provider
from opentelemetry import trace
from opentelemetry.trace.span import format_trace_id
from app.config.settings import settings

resource = Resource.create({ResourceAttributes.SERVICE_NAME: "multi-agent-copilot-app"})

def set_up_tracing():
    exporters = []
    exporters.append(AzureMonitorTraceExporter(connection_string=settings.APPLICATIONINSIGHTS_CONNECTION_STRING))

    # Initialize a trace provider for the application. This is a factory for creating tracers.
    tracer_provider = TracerProvider(resource=resource)
    # Span processors are initialized with an exporter which is responsible
    # for sending the telemetry data to a particular backend.
    for exporter in exporters:
        tracer_provider.add_span_processor(BatchSpanProcessor(exporter))
    # Sets the global default tracer provider
    set_tracer_provider(tracer_provider)

def enable_trace(func):
    """A decorator to enable observability """

    async def wrapper(*args, **kwargs):
        if not settings.APPLICATIONINSIGHTS_CONNECTION_STRING:
            # If the connection string is not set, skip observability setup.
            return await func(*args, **kwargs)

        tracer = trace.get_tracer(__name__)
        with tracer.start_as_current_span(func.__name__) as current_span:
            print(f"Trace ID: {format_trace_id(current_span.get_span_context().trace_id)}")
            return await func(*args, **kwargs)

    return wrapper