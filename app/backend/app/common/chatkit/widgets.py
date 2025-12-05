from typing import Any

from chatkit.actions import ActionConfig
from chatkit.widgets import Box, Button, Card, Col, Icon, Row, Text, Title, Markdown, Divider,WidgetRoot


def build_approval_request(tool_name: str, tool_args: dict[str, Any | None] | None, call_id: str,request_id:str ) -> WidgetRoot:
    """Build an approval request widget for tool execution.

   <Card>
  <Col align="center" gap={4} padding={4}>
    <Box background="yellow-400" radius="full" padding={3}>
      <Icon name="info" size="3xl" color="white" />
    </Box>
    <Col align="center" gap={1}>
      <Title value={title} />
      <Text value={description} color="secondary" />
      <Markdown value={"**GetUserAccount**"} />
    </Col>
  </Col>
  
  <Markdown value={"```py\n{'userName': 'bob.user@contoso.com'}\n```"} />
 
  <Divider spacing={2} />
  <Row>
    <Button
      label="Approve"
      block
      onClickAction={{
        type: "notification.settings",
        payload: { enable: true },
      }}
    />
    <Button
      label="No"
      block
      variant="outline"
      onClickAction={{
        type: "notification.settings",
        payload: { enable: true },
      }}
    />
  </Row>
</Card>
    """
    title = "Approval Required"
    description = "This action requires your approval before proceeding."
    
    # Format tool arguments as Python code
    args_str = str(tool_args)
    code_block = f"```py\n{args_str}\n```"
    
    return Card(
        key="approval_request",
        padding=0,
        size="md",
        children=[
            Col(
                align="center",
                gap=4,
                padding=4,
                children=[
                    Box(
                        background="yellow-400",
                        radius="full",
                        padding=3,
                        children=[
                            Icon(name="info", size="3xl", color="white"),
                        ],
                    ),
                    Col(
                        align="center",
                        gap=1,
                        children=[
                            Title(value=title),
                            Text(value=description, color="secondary"),
                            Markdown(value=f"**{tool_name}**"),
                        ],
                    ),
                ],
            ),
            Markdown(value=code_block),
            Divider(spacing=2),
            Row(
                children=[
                    Button(
                        label="Approve",
                        block=True,
                        onClickAction=ActionConfig(
                            type="approval",
                            payload={"tool_name": tool_name, "tool_args": tool_args, "approved": True, "call_id": call_id, "request_id": request_id},
                        ),
                    ),
                    Button(
                        label="No",
                        block=True,
                        variant="outline",
                        onClickAction=ActionConfig(
                            type="approval",
                            payload={"tool_name": tool_name, "tool_args": tool_args, "approved": False, "call_id": call_id, "request_id": request_id},
                        ),
                    ),
                ],
            ),
        ],
    )