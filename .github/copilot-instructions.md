# Coding agent workflow rules

## General behavior
- For every assigned task that modifies code, you MUST:
  1. Plan the changes.
  2. Implement the changes in small, incremental commits.
  3. Use tests and build commands already used in this repo to validate your work.
  4. **Before considering the task done, run SonarQube analysis using the `sonarqube` MCP server and fix new issues.**

## SonarQube MCP usage
- Use the `sonarqube` MCP tools to:
  - Run analysis on the current branch / changeset.
  - Retrieve issues limited to the files you modified in this task (bugs, vulnerabilities, code smells).
  - Summarize the issues in natural language.
  - Fix the issues directly in the code, explaining each fix briefly in the pull request description.

- Repeat:
  - After you apply fixes, rerun SonarQube analysis if there are still **Blocker** or **Critical** issues in files you touched.
  - Stop only when:
    - There are no new Blocker/Critical issues in your changed files, and
    - there are no obvious new major code smells that you introduced.

## Pull request expectations
- The PR description MUST include:
  - A short summary of the feature or bugfix.
  - A summary of SonarQube results for the modified code (number of new issues before vs after your fixes).
  - Any remaining low-severity issues that you did NOT fix and why.
