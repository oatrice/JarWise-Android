# Changelog

## [1.5.0] - 2026-02-01
### Added
- **Hierarchical Wallets**: Implemented a hierarchical system allowing users to create and manage sub-wallets under parent wallets for more detailed financial organization.
- **Wallet Management Screen**: Introduced a new dedicated screen to create, edit, delete, and visually organize wallet hierarchies.
- **Deletion Confirmation**: Added a confirmation dialog before deleting a wallet to prevent accidental data loss.

### Fixed
- **UI**: Corrected an issue where the wallet selection dropdown had a fixed height, sometimes hiding wallets from view.
- **Database**: Improved the app update process by adding a fallback for database migrations to prevent crashes.

## [1.4.0] - 2026-01-31
### Added
- **Hierarchical Jars**: Implemented a hierarchical system allowing users to create and manage sub-jars under parent jars for more detailed budget organization.
- **Jar Customization**: Users can now edit the name, percentage allocation, and icon for each jar on the "Manage Jars" screen.
- **UI Enhancements**: The "Manage Jars" screen now features visual connectors to clearly show parent-child relationships and includes a confirmation dialog before deleting a jar.

### Fixed
- **UI**: Corrected an issue where the custom sort order of jars was not being saved correctly.

## [1.3.0] - 2026-01-31
### Added
- **Jar Management**: Implemented a new system for users to create, manage, and save their financial "jars".
- **UI**: Introduced a dedicated "Manage Jars" screen, accessible from the dashboard, to configure jars.

## [1.2.0] - 2026-01-30
### Added
- **Wallet Management**: Users can now create and manage multiple wallets to better organize their finances.
- **Date Selection**: Implemented a date picker on the transaction screen, allowing users to log entries for past or future dates.

### Changed
- **UI**: Redesigned the 'Add Transaction' screen to incorporate the new wallet and date selection options.

## [1.1.0] - 2026-01-30
### Added
- **Transactions**: Implemented draft transaction functionality, allowing users to save and complete entries later.
- **UI**: The transaction history screen now groups entries by date and displays daily totals for improved readability.

### Changed
- **Navigation**: Overhauled the app's navigation system for a more unified and consistent user experience.

## [1.0.0] - 2026-01-18
### Changed
- **Data**: Updated the sample data displayed within the app.

## [0.1.0] - 2026-01-18
### Added
- **Feature**: Currency Support with native persistence (DataStore).
- **UI**: Aligned Dashboard structure with Web mock-ups.

## [0.0.1] - 2026-01-15
### Initialized
- Setup workspace structure.
