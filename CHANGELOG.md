# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).



## [1.0.0]


### Added

- Functionality to read a CSV file with student identifiers and GitHub usernames, and create GitHub teams based on the group ID in the student identifier.
- Adds students to their corresponding teams.
- Removes students from teams if they are no longer present in the CSV file.
- Print existing teams that are no longer needed.
- The `--dry-run` option to preview the actions that would be performed without making any changes in GitHub.
- Messages to remind that users need to accept invitations to join teams.
- Students with pending invitations are considered existing members, so they won't be invited again.
