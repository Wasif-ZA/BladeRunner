# Engineering Decisions Log

This log captures key engineering decisions and trade-offs made to keep the project understandable and team-friendly.

## 1) Keep the repo multi-module (instead of splitting immediately)

* **Decision:** Preserve the existing subprojects (embedded, Java simulation, and UI) in a single repository.
* **Why:** It keeps the system context in one place for portfolio review and recruiter walkthroughs.
* **Trade-off:** The repo is larger and requires clearer documentation.

## 2) Standardize documentation under `docs/`

* **Decision:** Consolidate legacy `DOCs/` content into `docs/legacy/` and place new documentation in `docs/`.
* **Why:** A single docs root is easier to navigate and aligns with common team conventions.
* **Trade-off:** Legacy content is kept only in portable text formats to avoid binary bloat.

## 3) Focus automation on the Next.js UI first

* **Decision:** Add scripts and CI checks for `website/code/` as the most runnable surface.
* **Why:** It provides a fast feedback loop and demonstrates professional repo hygiene.
* **Trade-off:** Java and firmware linting/test automation can be added later.
* **Update:** CI now also compiles `CCP/` and runs `CCP.CCPSelfTest`. Firmware is still not built in CI.

## 4) Use TypeScript checks as the baseline lint/test signal

* **Decision:** Implement `lint` and `test` as `tsc --noEmit` checks.
* **Why:** Next.js 16 no longer ships `next lint`, and external ESLint packages were blocked by registry policy.
* **Trade-off:** Rule-level linting can be layered in once package access is available.
