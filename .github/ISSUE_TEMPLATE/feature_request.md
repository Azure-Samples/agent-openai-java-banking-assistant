---
name: Feature request
about: Suggest an idea for this project
title: ''
labels: Feature
assignees: ''

---

**Description**
Brief summary of the problem or enhancement. What is happening or what is needed? Focus on user impact, not implementation details.

**Current Behavior**
Describe what currently happens. Include examples, logs, or snippets if helpful.

**Acceptance Criteria**
List clear, testable outcomes. Example:
- [ ] Credit card numbers in agent responses are masked (only last 4 digits remain)
- [ ] Middleware applies across all agents generating text
- [ ] Unit tests cover detection and non-detection scenarios

**Design Considerations**
High-level approach. Mention patterns (e.g., agent middleware), data flow, constraints, and any alternatives rejected. Do NOT paste full code unless essential.

**Main Affected Modules and/or Classes**
List exact file paths or module names. Example:
- app/copilot/app/agents/azure_chat/account_agent.py
- app/copilot/app/agents/azure_chat/payment_agent.py

**References**
Link to docs, ADRs, samples, related issues. Example:
- Agent middleware docs: <URL>
- Sample middleware implementation: <URL>

## Optional
**Security & Privacy (optional)**
Data masking, logging considerations, PII handling, compliance notes.

**Risks & Mitigations (optional)**
Potential side effects (e.g., false positives masking numbers that aren't cards) and mitigation strategies.

**Open Questions (optional)**
Items needing clarification before implementation.

**Additional Context (optional)**
Supporting info: screenshots, sample responses, logs, reproduction steps.
