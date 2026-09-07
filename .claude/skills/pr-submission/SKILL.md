---
name: pr-submission
description: Use whenever the user asks to submit, open, create, or publish a pull request (PR) — e.g. "submit this as PR", "open a PR", "create PR for me", "submit PR", "make a PR". Enforces pre-submission validation, a linked GitHub issue, active branch reuse (only branching when on master or detached HEAD), and strict zero-AI-attribution commit standards.
---

# PR Submission Workflow

Use this whenever opening a pull request for this repository.

## 1. Core Non-Negotiable Rules

1. **Zero AI Attribution**: Commit messages, commit trailers, PR titles, PR bodies, and comments must never contain `Co-Authored-By: Claude` (or any other assistant trailer), "Generated with...", "🤖", or any mention of Claude, Gemini, Copilot, Cursor, or any other AI tool. Write strictly from the perspective of the repository author.
2. **A linked issue is mandatory**: every PR must reference a GitHub issue in this repo (`chaddy50/Froh`). If no issue number is already in context (mentioned by the user, or evident from the branch name / conversation), stop and ask the user for one before doing anything else.
3. **Validate the issue actually exists** before proceeding: `gh issue view <number> --repo chaddy50/Froh`. If it fails, tell the user and ask for a valid number — don't guess or silently skip.
4. **Branch reuse**: check `git branch --show-current`.
   - If on `master` or detached HEAD, create a new topic branch (`git checkout -b fix/<topic>` or `feat/<topic>`).
   - If already on a named topic branch, stay on it — do not create a new one.
5. **Pre-flight validation must pass** before committing/pushing.

## 2. Step-by-Step Procedure

### Step 1: Confirm the linked issue
- If the user gave an issue number, use it. Otherwise ask: "Which issue number does this PR close?"
- Validate it: `gh issue view <number> --repo chaddy50/Froh`. Do not proceed until this succeeds.

### Step 2: Pre-Submission Validation
Run whichever apply to what changed:
```bash
./gradlew app:testDebugUnitTest
# If UI/behavior that instrumentation tests cover changed:
./gradlew connectedDebugAndroidTest
```
Inspect `git status` / `git diff` to make sure nothing unwanted (scratch files, local.properties, debug logs) is staged.

### Step 3: Branch Check & Staging
```bash
BRANCH=$(git branch --show-current)
```
- If `BRANCH` is empty or `master`: `git checkout -b fix/<topic>` (or `feat/<topic>`).
- Otherwise stay on the current branch.
```bash
git add <files>
```

### Step 4: Commit Message
```bash
git commit -m "fix: concise summary of fix

- Bullet point detailing specific change

Fixes #<issue_number>"
```

### Step 5: Push & Create PR
```bash
BRANCH=$(git branch --show-current)
git push -u origin "$BRANCH"
gh pr create --base master --title "fix: <summary>" --body "Closes #<issue_number>

<what changed and why>

## How to test
<concrete steps>"
```

### Step 6: Final Verification
`gh pr view <number>` to confirm it rendered correctly and links the issue.
