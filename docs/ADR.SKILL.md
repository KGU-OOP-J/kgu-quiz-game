---
name: architecture-decision-record
description: Investigate, compare, review, and record significant architectural decisions. Use when choosing or changing system architecture, technical dependencies, interfaces, data storage, infrastructure, or other decisions with lasting technical consequences; also use when drafting or reviewing an ADR.
---

# Architecture Decision Records

Understand the problem before choosing a solution. Investigate thoroughly; keep the process and the record short. The ADR records the decision, its rationale, and its consequences.

## Workflow

1. **Understand.** Define the problem without assuming a solution. Inspect the relevant code, configuration, existing ADRs, and available operational data. Establish the current system, its scale, intended future state, and constraints. Ask the user only for missing information that could change the decision.
2. **Explore.** Identify materially distinct, feasible approaches before ranking them. Include keeping the current design, improving it incrementally, and deferring the decision when applicable. Define evaluation criteria from the requirements. Compare the approaches against those same criteria. Verify important technical claims using relevant documentation, source code, measurements, or targeted experiments. Challenge the leading option with failure scenarios, changing assumptions, and simpler alternatives.
3. **Review.** Present the options, key trade-offs, recommendation, evidence gaps, and the decision required from the user. Explain why the leading option fits this project now and when another would be preferable. Resolve material uncertainties or agree on explicit assumptions. Obtain approval from the authorized decision-maker; do not treat a preference, silence, or existing code as approval.
4. **Record.** Write the ADR using the template below or the repository's existing format. Record the actual decision and its reasons. A draft requested before approval remains Proposed. Mark it Accepted only after explicit approval. Check consistency with existing ADRs, update the decision log if one exists, and report the file path and any unresolved items. Do not implement the decision unless separately requested.

Repeat only the step affected by new information. Do not require a separate report, meeting, scorecard, or questionnaire for each step. For a documented historical decision, recover the evidence and record gaps rather than inventing prior deliberation.

## What to establish

- **Product:** purpose, users, current features, planned capabilities, and growth targets.
- **Workload:** current and expected traffic, concurrency, data volume, request patterns, and performance or availability targets.
- **Infrastructure:** deployment topology, server count, CPU, RAM, storage, network, utilization, budget, and expansion plans.
- **Constraints:** existing architecture, compatibility, security, reliability, team capacity, operating cost, and delivery time.

Check relevance before collecting details. Distinguish measured facts, user statements, estimates, assumptions, and unknowns. Never infer production capacity from hardware alone. If an unknown could change the recommendation, investigate or ask; if it cannot be resolved, give conditional options instead of an unconditional conclusion. Do not design for hypothetical growth without a credible requirement.

## Decision rules

- Explore the design space before comparing specific products. Do not manufacture weaker alternatives or claim exhaustive coverage of unknowable options.
- Reject approaches that violate mandatory constraints. Compare remaining options by actual benefits, drawbacks, development and operating costs, and reversibility.
- Prefer the simplest approach that meets the agreed requirements. Account for migration, future change, and failure recovery where relevant.
- Cite material evidence. Never invent requirements, metrics, benchmarks, approvals, or historical rationale. Distinguish validation plans from completed tests.
- Keep one independently reviewable decision per ADR. Explain why it was made; leave detailed implementation to other documents.

## ADR template

```markdown
# ADR-NNNN: <decision title>

- Status: Proposed | Accepted | Rejected | Superseded
- Date: YYYY-MM-DD
- Owner: <verified owner, if known>

## Context
<Problem, relevant current and target conditions, constraints, and decision criteria.>

## Decision
<Selected approach and rationale. Summarize material alternatives and why they were not selected. For Proposed records, label the choice as a proposal.>

## Consequences
- Positive: <benefits>
- Negative: <costs, limitations, or risks>

## Compliance
<How to verify that the implementation follows the decision. Include review triggers or rollback conditions when relevant.>

## Notes
<Evidence links, related ADRs, and approval or version details when applicable.>
```

Context, Decision, and Consequences are required. Include Compliance and Notes when they add useful information or the project requires them. Remove unused placeholders. Keep the ADR as short as the decision permits.

## Record management

Follow the repository's ADR conventions. Otherwise, use `docs/adr/NNNN-decision-title.md` with unique sequential numbers and English kebab-case filenames. Use the project's documentation language. Record actual dates and known approval details. Preserve Accepted and Rejected records. Change an accepted decision through a new ADR; when approved, mark the previous record Superseded and link both records. Record rejection reasons. Flag conflicting existing implementations rather than silently rewriting code or decisions.

## Communication and writing

Lead with the decision needed, the supported recommendation, or the result. Before investigation, lead with the problem or the missing information instead of a premature recommendation. Use direct sentences, short paragraphs, and specific technical terms. Group related points; use tables only when they improve comparisons. Avoid repeating the same argument, generic claims, filler, and tangents. Keep genuine uncertainty visible. Across turns, state the current stage and what remains open in one line. If action is needed, end with the next concrete question or action. Report blockers with their impact and resolution path. Keep analysis complete even when the presentation is short.
