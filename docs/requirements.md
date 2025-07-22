# Maple – Requirements

## 1. Purpose
Automate auditable, consistent release branching/tagging across many first-party repos using a single source of truth (BOM). Reduce toil, drift, and missed steps.

## 2. Stakeholders
- **Release Engineer** – invokes tool, needs reliability/resume.
- **Feature Teams** – need branches/tags for fixes.
- **Build/CI Owners** – integrate into pipelines.
- **Audit/Compliance** – require immutable metadata.

## 3. Scope
**In v1:**
- Parse BOM (JSON first) to list first-party deps.
- For each repo: checkout specified version, build/test, branch/tag/push.
- Dry-run, resume/continue, machine & human readable reports.
- Pluggable BOM formats/build runners.
- Concurrency with optional dependency ordering.

**Out (deferred):**
- BOM generation.
- Cross-VCS support.
- UI dashboard (CLI + logs only).
- Secrets mgmt beyond env/git helpers.

## 4. Functional Requirements
**FR1** CLI accepts `--bom <file>` and `--version <release>`.  
**FR2** Filter first-party deps via config (prefix/allowlist).  
**FR3** Clone/fetch repo cache, checkout BOM version.  
**FR4** Build & test via repo command (Gradle/Maven/etc.) with timeout.  
**FR5** On success: create `release/<version>` branch and `<version>` tag; push both.  
**FR6** Record failures, continue/halt per policy.  
**FR7** Emit JSON report (per-repo status, SHAs, tags).  
**FR8** `--dry-run` (simulate, no pushes).  
**FR9** `--resume-from <repo>` continue partial runs.  
**FR10** `--plan` prints action graph.  
**FR11** `.maple.yml` config overrides.  
**FR12** Optional Slack/Webhook notification.

## 5. Non-Functional Requirements
- **Reliability:** Idempotent per repo, safe re-runs.
- **Performance:** O(100) repos; configurable parallelism & timeouts.
- **Security:** Avoid logging secrets; rely on git creds.
- **Observability:** Structured logs, debug mode.
- **Portability:** macOS/Linux, Java 17+.
- **Testability:** Unit + integ tests, deterministic fixtures.
- **Extensibility:** Parsers/runners via interfaces.

## 6. Inputs / Outputs
**Inputs:** BOM file, config, env (GIT creds), CLI flags.  
**Outputs:** Branches/tags in Git, logs, JSON summary.

## 7. Success Metrics
- % releases with zero manual fixes.
- MTTR for failed runs.
- # of teams using Maple.
- Run duration across N repos.

## 8. Risks / Assumptions
- BOM correctness is assumed.
- Network/git outages; must retry.
- Non-deterministic repo builds.

## 9. Acceptance Criteria
One command performs the release ops or surfaces actionable failure with resume/dry-run support. JSON report is produced and artifacts are traceable.
