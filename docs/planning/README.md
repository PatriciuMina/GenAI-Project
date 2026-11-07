# Planning

| File | What it is |
|------|------------|
| [`midterm-tickets.md`](midterm-tickets.md) | All mid-term tickets T-01..T-99 (points, labels, dependencies, full description) |

## Creating the GitHub issues

[`scripts/create-issues.sh`](../../scripts/create-issues.sh) reads `midterm-tickets.md` and
creates everything on GitHub for **PatriciuMina/GenAI-Project** only:

1. The labels (`type:*`, `area:*`, `workflow:W1..W5`, `points:1..5`, `good-first-ticket`,
   `no-genai`, `blocked`, plus any other label used in the file).
2. The milestone **Mid-term**.
3. The project board **UBB Events Board** (owner `PatriciuMina`):
   - the existing "GenAI Pro" board is renamed;
   - it gets a number field **Points**;
   - its Status options are set to Todo / In Progress / In Review / Done.
4. One issue per ticket:
   - title `[T-nn] <title>`, body from the file, labels + `points:<n>`, milestone Mid-term, no assignees;
   - added to the board with Points set and Status = Todo.
5. Clickable dependencies:
   - `Depends on: T-03 (#12)` in the body;
   - GitHub's native "blocked by" relations.

**Requirements:** `gh` logged in as `PatriciuMina` with the `repo` and `project` scopes
(`gh auth status`), plus `jq` and `python3`.

```bash
./scripts/create-issues.sh                    # dry-run (default): only prints what it would do
./scripts/create-issues.sh --show-body T-01   # preview one generated issue body (offline)
./scripts/create-issues.sh --run              # really create everything (about 15 minutes)
```

- **Safe to re-run.** Anything that already exists is skipped:
  - labels, milestone, project and board fields;
  - issues, matched by `[T-nn]` in the title;
  - board items, body links and dependency links.

  If the script stops (e.g. a network error), fix the cause and run `--run` again.
- **Field values on the board:** Points and Status are only set when they are empty, so
  re-running never moves a ticket the team has already started.
- **Pacing:** the script sleeps 3 s after each issue and 1 s after every other write, to
  avoid GitHub's secondary rate limits. If it still hits one, it waits 60 s and retries once.
  Override with `SLEEP_CREATE=5 SLEEP_WRITE=2 ./scripts/create-issues.sh --run`.
- **Editing tickets later:** change the issue on GitHub. The script never changes existing
  issue text except the `Depends on:` line, and only when its links are missing or wrong.

## Rule for `good-first-ticket`

`good-first-ticket` goes **only** on tickets with **1–2 points and `Depends on: none`**, so
it marks tickets that can be started right away. `create-issues.sh` enforces this rule
itself, whatever the `Labels:` line in the ticket file says.

[`scripts/fix-good-first-labels.sh`](../../scripts/fix-good-first-labels.sh) applies the
rule to issues that already exist on GitHub:
- it removes the label from issues that have dependencies and adds it where it is missing;
- it keeps the `- Labels:` line in the issue body consistent;
- it updates the label description.

"Has dependencies" means a `Depends on:` line other than `none`, or any native "Blocked by"
relation.

```bash
./scripts/fix-good-first-labels.sh          # dry-run (default): lists the changes and a summary
./scripts/fix-good-first-labels.sh --run    # really change the labels (about 2 minutes)
```

It is safe to re-run: issues that already follow the rule are skipped.
