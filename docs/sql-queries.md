# Useful SQLite Queries

## Top failing repos last 14 days
```sql
SELECT name,
       COUNT(*) AS failures
FROM repo_status
WHERE json_extract(json_blob,'$.build.status') = 'FAILED'
  AND run_id IN (
    SELECT run_id FROM runs
    WHERE start >= datetime('now','-14 day')
  )
GROUP BY name
ORDER BY failures DESC
LIMIT 20;
```

## Success ratio per day
```sql
SELECT date(start) AS day,
       SUM(CASE status WHEN 'SUCCEEDED' THEN 1 ELSE 0 END) * 1.0 / COUNT(*) AS success_ratio
FROM runs
GROUP BY day
ORDER BY day DESC;
```

## Average build duration (ms) by repo
```sql
SELECT name,
       AVG(json_extract(json_blob,'$.build.durationMs')) AS avg_ms
FROM repo_status
GROUP BY name
ORDER BY avg_ms DESC;
```

## Runs with partial success
```sql
SELECT run_id, start, status
FROM runs
WHERE status='PARTIAL'
ORDER BY start DESC;
```
