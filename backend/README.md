# PV Case Review — Backend

Spring Boot service for reviewing pharmacovigilance (PV) cases and merging in
follow-up reports. No database — cases and reviewer queries live in memory
for the lifetime of the process.

## Stack

- Java 17, Spring Boot 3.3, Gradle (wrapper included)
- Lombok for model/DTO boilerplate
- JSON request/response bodies use `snake_case` everywhere (`case_id`,
  `field_path`, etc.) via a global Jackson naming strategy — fields that are
  `null` are omitted from responses rather than serialized as `null`

## Running it

```bash
./gradlew bootRun
```

The app starts on `http://localhost:8080`. On startup it bootstraps one case,
**`PV-2026-0451`**, from `src/main/resources/data/case_v1.json`, so there's
something to query immediately — no seeding step required.

## Running the tests

```bash
./gradlew test
```

## The four main endpoints

### 1. Get a case

```bash
curl http://localhost:8080/cases/PV-2026-0451
```

```json
{
  "case_id": "PV-2026-0451",
  "version": 1,
  "case_classification": "non-significant",
  "extracted_at": "2026-04-08T09:14:00Z",
  "source_document": "initial_report_PV-2026-0451.pdf",
  "sections": {
    "patient": {
      "age": { "value": "62", "confidence": 0.91, "source": "p.2 §1" },
      "weight_kg": { "value": "78", "confidence": 0.85, "source": "p.3 §2" }
    }
  },
  "missing_fields": []
}
```

Unknown case id → `404` with an `ErrorResponse` body.

### 2. Apply a follow-up report (merge)

```bash
curl -X POST http://localhost:8080/cases/PV-2026-0451/follow-ups \
  -H "Content-Type: application/json" \
  -d '{
    "case_classification": "significant",
    "sections": {
      "patient": {
        "age": { "value": "63", "confidence": 0.90, "source": "p.2 §1" }
      },
      "adverse_event": {
        "severity_grade": { "value": "Grade 2", "confidence": 0.83, "source": "p.4 §3" }
      },
      "lab_results": {
        "ck_level": { "value": "450 U/L", "confidence": 0.89, "source": "p.6 §1" }
      }
    },
    "missing_fields": ["patient.weight_kg"]
  }'
```

```json
{
  "case_id": "PV-2026-0451",
  "version": 2,
  "case_classification": "significant",
  "sections": {
    "patient": {
      "age": { "value": "63", "confidence": 0.90, "source": "p.2 §1", "status": "OVERRIDDEN", "previous_value": "62" },
      "weight_kg": { "value": "78", "confidence": 0.85, "source": "p.3 §2", "status": "RETAINED" }
    },
    "adverse_event": {
      "severity_grade": { "value": "Grade 2", "confidence": 0.83, "source": "p.4 §3", "status": "NEW" }
    },
    "lab_results": {
      "ck_level": { "value": "450 U/L", "confidence": 0.89, "source": "p.6 §1", "status": "NEW" }
    }
  },
  "missing_fields": ["patient.weight_kg"]
}
```

`version` auto-increments if you don't supply one. Unknown case id → `404`;
malformed JSON body → `400`.

### 3. Submit a reviewer query against a field

```bash
curl -X POST http://localhost:8080/queries \
  -H "Content-Type: application/json" \
  -d '{
    "case_id": "PV-2026-0451",
    "field_path": "patient.age",
    "question": "Why did age change from 62 to 63?"
  }'
```

```json
{
  "id": "6f9ee839-db97-49e4-90e6-260761a210c1",
  "case_id": "PV-2026-0451",
  "field_path": "patient.age",
  "question": "Why did age change from 62 to 63?",
  "created_at": "2026-06-21T10:04:53.312608Z"
}
```

Any blank field → `400` (e.g. `"question is required"`).

### 4. List queries for a case

```bash
curl "http://localhost:8080/queries?caseId=PV-2026-0451"
```

```json
[
  {
    "id": "6f9ee839-db97-49e4-90e6-260761a210c1",
    "case_id": "PV-2026-0451",
    "field_path": "patient.age",
    "question": "Why did age change from 62 to 63?",
    "created_at": "2026-06-21T10:04:53.312608Z"
  }
]
```

Note: `caseId` here is a URL query parameter, not a JSON body field, so it
stays camelCase — the `snake_case` rule only applies to JSON bodies.

*(There's also a `GET /health` liveness check, returning `{"status": "UP"}`.)*

## Merge behavior

Every field on a case carries a `status` describing how it got its current
value, computed each time a follow-up is applied:

| Status | Meaning |
|---|---|
| `NEW` | Field (or whole section) didn't exist before; the follow-up introduced it. |
| `OVERRIDDEN` | Field existed before with a different value; the follow-up replaced it. `previous_value` holds what it used to be. |
| `UNCHANGED` | Field existed before with the *same* value; the follow-up just re-confirmed it. |
| `RETAINED` | Field existed before and the follow-up **didn't mention it at all**; the stored value carries forward as-is. |

### Why `RETAINED` exists

A follow-up report only contains the fields the extractor found evidence for
in *that* document — it's a partial payload, not a full restatement of the
case. If a field is missing from the follow-up, that's almost always because
the new document simply didn't re-cover it, not because it should be erased.
Treating "absent from follow-up" the same as "explicitly removed" would
silently destroy data from earlier reports, which is the wrong default for
PV case data where every value is sourced and audited.

So instead of overwriting or dropping anything, the merge keeps the stored
field's `value`/`confidence`/`source` untouched and tags it `RETAINED`,
making it visible to a reviewer as "carried forward, not reconfirmed" rather
than indistinguishable from a freshly-extracted field. This applies at both
granularities: a single field can be retained inside an otherwise-updated
section (e.g. `patient.weight_kg` above), and a whole section omitted from
the follow-up is retained field-by-field, not dropped wholesale.

## Error responses

All errors share one shape:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "No case found with id UNKNOWN-ID",
  "path": "/cases/UNKNOWN-ID",
  "timestamp": "2026-06-21T10:07:46.857813Z"
}
```

| Situation | Status |
|---|---|
| Unknown `caseId` | `404` |
| Malformed JSON body | `400` |
| Missing/blank required field (e.g. in `/queries`) | `400` |
| Missing required query param (e.g. `caseId` on `GET /queries`) | `400` |
