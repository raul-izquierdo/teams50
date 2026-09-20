# Teams50

## Introduction

This application is intended for teachers who use the [Classroom 50](https://github.com/foundation50/classroom50/wiki) platform.

> **Note:** This application is **part of a toolkit** for managing classes with GitHub Classroom. We strongly recommend reading the [main toolkit repository](https://github.com/raul-izquierdo/classroom-50-tools) first to get an overview of the project and understand where this tool fits.


_teams50_ ensures that the GitHub organization storing the exercise solutions has one team for each lab group. This makes it possible to grant students access to solutions by group. This tool only creates and updates _teams_. Solutions can be shared with each group later, either manually through the GitHub web interface or, as recommended, by using [solutions50](https://github.com/raul-izquierdo/solutions50), as described in the [main toolkit repository](https://github.com/raul-izquierdo/classroom-50-tools).

This tool is implemented in Java and requires JDK 21 or later.


## Installation and Configuration

<!-- TODO: 📅 /**/  -->

Install and configure this tool together with the other toolkit applications by following the steps in the [main toolkit repository README](https://github.com/raul-izquierdo/classroom-50-tools#readme).

## Roster requirements

To add a member to a _team_, GitHub requires their _username_ (login). This information is not available when students are initially added to the classroom roster, because an _email_ address cannot be used to add students to _teams_. The student's GitHub _username_ is added to the roster when they accept the invitation to join the classroom.

Therefore, students must have accepted the invitation before they can be added to their _team_, which means that their _username_ must already be present in the roster. This tool ignores students who do not yet have a _username_ because they have not accepted the invitation, and does not add them to any _team_. A notice is displayed in the command output.


## Using the Application

To run the application, execute the following command in a terminal:
```bash
java -jar teams50.jar
```

This downloads the current classroom roster from Classroom 50 and ensures that the organization storing the solutions has one _team_ for each lab group (_section_) found in the roster.
- If a lab group in the roster does not have a corresponding _team_, a new _team_ is created.
- Members who no longer belong to a lab group are removed from its _team_.
- New members who now belong to a lab group are added to its _team_.

In other words, when execution finishes, the _teams_ in the organization storing the solutions exactly match the lab groups in the current classroom roster.

Run the tool whenever the classroom roster changes or whenever you want to verify that everything is synchronized.

> **Note:** This process only applies to students who have accepted the invitation to join the classroom and therefore already have a _username_ in the roster.


## Removing Teams

This tool does not delete any _team_ from the organization storing the solutions. If a _team_ is no longer needed, delete it manually through the GitHub web interface.

The organization may contain _teams_ that are not related to lab groups. Therefore, an empty _team_ is not necessarily obsolete. For example, the organization may have a _team_ for instructors or teaching assistants that is not referenced in the roster.

In any case, the tool lists _teams_ that have no students and may therefore be candidates for deletion. This allows the instructor to decide whether to delete them.

## Generated Team Names for Groups

_teams50_ creates _teams_ with exactly the same names as the lab groups (_sections_) in the roster. For example, if a lab group is named `A1`, it creates a _team_ named `A1` without adding a prefix or suffix.

When checking whether a _team_ already exists, _teams50_ ignores capitalization. For example, if a lab group is named `A1` and a _team_ named `a1` already exists, _teams50_ considers the _team_ to exist and does not create a new one. Students in that group can specify their section as either `A1` or `a1`, and they are added to the same _team_.

## Command-Line Arguments

Syntax:

```bash
java -jar teams50.jar [flags]
```

Flags:
- **-r [current_roster.csv]**: Use this local roster file instead of downloading the roster from the Classroom 50 repository. If not provided, the tool will download the current roster from the Classroom 50 GitHub repository.
- **-t [token]**: GitHub API access token. If not provided, it will try to read from the GITHUB_TOKEN environment variable or from a `.env` file.
- **-o [organization]**: GitHub organization name associated with the classroom. If not provided, it will try to read from the CLASSROOM_ORG environment variable or from a `.env` file.
- **-c [classroom]**: GitHub classroom name. If not provided, it will try to read from the CLASSROOM_NAME environment variable or from a `.env` file.
- **-s [solutions-org]**: GitHub organization where the solutions are stored. If not provided, it will try to read from the SOLUTIONS_ORG environment variable or from a `.env` file.
- **-h, --help**: Show help.
- **-V, --version**: Show version.

## Exit Codes

The exit codes indicate the result of command execution:
- **0**: The command executed successfully.
- **1**: An error occurred.

## License

See `LICENSE`.
Copyright (c) 2025 Raul Izquierdo Castanedo


---
<style>p:has(+ :is(ul,ol)) { margin-bottom: 0; }</style>
