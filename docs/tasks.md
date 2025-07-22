# Maple – Backlog

Labels used: `epic`, `task`, `bug`, `docs`, `infra`.

----

## Epic 0 – Repo Hygiene & Docs  _(label: epic)_
- [ ] **Task:** Create /docs with requirements, design, tasks (this PR).  
- [ ] **Task:** Add CONTRIBUTING.md, CODE_OF_CONDUCT.md, LICENSE.  
- [ ] **Task:** Enhance README with quickstart & diagrams.  
- [ ] **Task:** Add CI workflow (build/test on PR).

## Epic 1 – CLI & Config
- [ ] **Task:** Choose CLI lib (picocli/clikt).  
- [ ] **Task:** Implement `maple plan` to print parsed BOM and actions.  
- [ ] **Task:** Implement `.maple.yml` loader + precedence rules (flags > file > defaults).  
- [ ] **Task:** Unit tests for arg parsing & config.

## Epic 2 – BOM Parsing
- [ ] **Task:** Define JSON BOM schema (`name`, `version`, `repo`, `firstParty`, `buildCmd?`).  
- [ ] **Task:** Implement `JsonBomParser`.  
- [ ] **Task:** Validation & error messages for malformed BOM.  
- [ ] **Task:** Parser unit tests (happy path, malformed, missing fields).

## Epic 3 – Repo Manager
- [ ] **Task:** Decide JGit vs shell exec; abstract behind interface.  
- [ ] **Task:** Implement clone/fetch/checkout/tag/branch/push.  
- [ ] **Task:** Retry/backoff util for git ops.  
- [ ] **Task:** Integration tests with temp repos.

## Epic 4 – Build Runner
- [ ] **Task:** Detect or configure build command per repo.  
- [ ] **Task:** Exec w/ timeout, capture stdout/stderr to file.  
- [ ] **Task:** Map exit codes → success/failure; propagate error.  
- [ ] **Task:** Tests with dummy gradle/maven projects.

## Epic 5 – Orchestrator & State Store
- [ ] **Task:** Define step/task state machine (PENDING→RUNNING→SUCCEEDED/FAILED).  
- [ ] **Task:** Serialize run state to JSON/SQLite for resume.  
- [ ] **Task:** Implement parallel executor w/ configurable concurrency.  
- [ ] **Task:** `--resume-from` and `--dry-run` flags.

## Epic 6 – Reporting & Notifications
- [ ] **Task:** JSON summary writer (RunSummary).  
- [ ] **Task:** Pretty console table output.  
- [ ] **Task:** Optional Slack/Webhook notifier (flag + URL).  
- [ ] **Task:** Golden file tests for summary output.

## Epic 7 – Advanced Features (Stretch)
- [ ] **Task:** Build dependency DAG & topological order.  
- [ ] **Task:** Multi-BOM merge/diff command.  
- [ ] **Task:** Plugin API for custom steps/hook scripts.  
- [ ] **Task:** GraalVM native-image packaging.

## Epic 8 – Release & Adoption
- [ ] **Task:** Define semver & release process.  
- [ ] **Task:** GitHub release workflow + changelog automation.  
- [ ] **Task:** Sample repos + demo script.  
- [ ] **Task:** Internal adoption guide.

---

### Definition of Done (global)
- Tests & CI green
- Docs updated
- Code reviewed/approved