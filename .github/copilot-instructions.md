# NextRep Java contributor instructions

- Target Java 21 and preserve dependency-free builds unless a separate milestone explicitly adds dependencies.
- Keep the CLI, training logic, immutable domain model, and CSV persistence separate.
- Prefer clear code and meaningful validation to unnecessary abstractions.
- Write tests for changes and run `bash run.sh test`.
- Do not describe this as Spring Boot, Maven, JUnit, a REST API, or a hosted web application.
- Do not include sensitive athlete information, credentials or generated CSV files in commits.
- Keep README claims aligned with what the source code actually implements.
