from azure.core.credentials import TokenCredential
from semantic_kernel.connectors.ai.open_ai import AzureChatCompletion
from semantic_kernel.connectors.mcp import MCPStreamableHttpPlugin
from semantic_kernel.agents import ChatCompletionAgent, Agent
from semantic_kernel.functions.kernel_plugin import KernelPlugin
from semantic_kernel.connectors.mcp import MCPPluginBase
from semantic_kernel.functions import KernelArguments
import logging


logger = logging.getLogger(__name__)

class TriageAgent :
    """ this agent is used in hand-off based orchestration as source agent to be configured in handoffs rules for semantic kernel.
    """
    instructions = """
      You are a banking customer support agent triaging customer requests about their banking account, movements, payments.
      Based on the customer request you need to route the request to the appropriate agent.
      In all other cases not related to account, transactions, payments you should respond to the user that you are not able to help with the request.
    """
    name = "TriageAgent"
    description = "This agent triages customer requests and routes them to the appropriate agent."

    def __init__(self, chatCompletionService: AzureChatCompletion):
        self.chatCompletionService = chatCompletionService


    async def build_sk_agent(self)-> Agent:
    

      return ChatCompletionAgent(
            service=self.chatCompletionService,
            name=TriageAgent.name,
            instructions=TriageAgent.instructions,

        )