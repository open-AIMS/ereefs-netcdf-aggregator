# Standard Git Workflow

This page details the standard Git workflow recommended for all AIMS Knowledge Systems software development projects. It 
is based on the document [Git Workflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow) by 
Atlassian.

## Branches

### 'production' branch
- The official release history of the project.
- No changes permitted directly to this branch.
- Deployments can only be performed from this branch.
- This branch must be deployable at all times.
- Merges to this branch should be performed with "no fast-forward" flag set to capture workflow in the Git log.

### 'main' branch 
- The integration branch for features and bug fixes.
- No changes permitted directly to this branch.
- The only branch for merging to **production**.
- Working branches should be branched off this branch, and merged into this branch.
- This branch must always pass the suite of automated tests.
- Code must not be merged into this branch until it passes the suite of automated tests.
- Merges to this branch should be performed with "no fast-forward" flag set to capture workflow in the Git log.

### 'release' branch

A **release** branch is a temporary branch used to capture the state of **main** in preparation for a release 
(merging to **production**). This means that code is not merged directly from **main** to **production**, but instead
is merged via the **release** branch. Capturing the state of **main** in the **release** branch allows other team 
members to continue working with **main** as normal without interrupting the release process.

The workflow of a release is:

- Fork the **release** branch from the latest **main** branch, starting the release cycle. No new features can be added
  to **release** after this point, only release-oriented work.
- Make any changes required for the release (eg: increment release number in Maven files, release-specific documentation 
  updates).
- Once ready to deploy, merge **release** (no fast-forward) into **production**, and tag **production** branch with the 
  release number.
- If any changes were made directly to the **release** branch, merge **release** (no fast-forward) into **main** to 
  capture those changes. Note that master may have progressed since the release was initiated, so some merge conflict 
  may be expected.
- Delete the **release** branch.

### 'hotfix' branch

A **hotfix** branch is a special branch that may exist to patch a bug in **production** that cannot be fixed in the
**main** `HEAD` because it would result in new features deployed to **production**.

- Fork **production** to create the **hotfix** branch.
- Perform the necessary bug fix, ensuring the automated test suite passes.
- Rebase to clean commits as appropriate/desired.
- Follow the **release** branch workflow to merge changes into **production** and then **main**.
- Delete the **hotfix** branch.

### Working branches

- Working branches should be forked from **main**, normally from the `HEAD` commit.
- The suite of automated tests must pass before merging changes into **main**.
- Merge **main** into the working branch before attempting to merge the working branch into **main** to ensure 
  conflicts are handled as a separate logical step to the actual merge.
- Rebase before merging **main** to clean commits as appropriate/desired.
- Working branches should be deleted ASAP after merging.
- All commits must reference a tracking issue ID (such as JIRA) where one exists.

#### BugFix branch

This type of branch signals to the reader that the changes were made to fix a bug, and are not expected to be 
significant in scope.

#### Feature branch

This type of branch signals to the reader that the changes implemented a new feature, and could therefore be significant
in scope.
