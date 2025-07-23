# Observability (Prometheus + Grafana)

## Metrics We Expose
- `maple_runs_total{status}` counter
- `maple_repo_processed_total{status}` counter
- `maple_build_duration_seconds` histogram
- `maple_git_op_duration_seconds{op}` histogram
- `maple_retries_total`

## How
- Prometheus Java Simpleclient (no Spring).
- Embedded HTTP server on `METRICS_PORT` (default 9404).

## Enabling
```
MAPLE_METRICS_ENABLED=true MAPLE_METRICS_PORT=9404 ./maple run ...
```

## Grafana
Import `observability/grafana/maple-dashboard.json`.
Add Prometheus datasource.

### Panels
Runs over time (succeeded vs failed)
Repo failures by errorType
P95 build duration
