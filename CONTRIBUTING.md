# Contributing

This portfolio project uses a feature-branch and pull-request workflow.

1. Open an issue or describe the proposed improvement.
2. Branch from `main` with a descriptive branch name.
3. Keep changes small and focused; do not add a framework unless it provides a real benefit.
4. Run `bash run.sh test` and `bash run.sh demo` with JDK 21.
5. Update documentation and tests whenever behavior changes.
6. Open a pull request and wait for GitHub Actions to finish before merging.

The repository's tests are custom Java assertions, not JUnit tests. Avoid implying otherwise.
