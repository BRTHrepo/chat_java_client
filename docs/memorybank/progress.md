# Progress

This document tracks the current status of the project, including completed features, ongoing work, and known issues.

## Completed Features:

*   **Project Structure:** Initial project structure established with `core` and `ui-swing` Maven modules.
*   **API Communication:** Basic API communication layer (`ApiService`) and authentication (`AuthService`) implemented.
*   **User Interface:**
    *   Login View and Presenter (`LoginView`, `LoginPresenter`) functional.
    *   Main View (`MainView`) and Presenter (`MainPresenter`) implemented with core chat functionalities.
    *   Display of friend lists and messages upon friend selection.
    *   Message sending functionality.
*   **Error Handling:** Centralized error handling mechanism (`ApiException`, `ApiError`, `ErrorMessageTranslator`) in place.
*   **User Management:**
    *   Logout functionality implemented.
    *   Friend management features (accepting/rejecting friend requests, adding new friends).
*   **Data Handling:**
    *   JSON processing errors in `ApiService` resolved.
    *   Local SQLite database integration for message history, friend lists, friend requests, and event logs.
    *   Data persistence for messages, friends, and requests.
    *   Presenter-DAO integration for local data access.
*   **Authentication & Session Management:**
    *   Login token handling improved, with JWT token storage and retrieval.
    *   Automatic JWT token refresh during polling.
    *   Persistent login using stored credentials.
*   **Profile Management:**
    *   Profile View and Presenter (`ProfileView`, `ProfilePresenter`) created for viewing and editing name and avatar URL (local updates only, as server API is not yet available for this).

## Current Status:

The core functionalities of the main view, friend management, and logout are operational. Users can log in, view their friend list and requests, and send messages. Network requests are handled asynchronously to prevent UI blocking. All known JSON processing errors have been fixed.

### Recent Updates (2025.10.21.):

*   **Login Token Handling:** Server sends JWT token as a separate `"token"` key. Client now processes the full JSON response, storing the token separately and loading user data (`user_id`, `nickname`, `email`). A new `registerLoginRaw` method in `ApiService` returns the full JSON response. This ensures the token is not null and is correctly passed in the `Authorization` header for protected API calls.
*   **JWT Token Auto-Refresh during Polling:** The client now checks JWT token validity during each polling cycle. If the token expires or is about to expire, it automatically refreshes (via re-login or refresh mechanism). This prevents users from needing to manually re-login when their token expires.
*   **Login Process Improvement:**
    *   Automatic login on application startup using email/password stored in Preferences (fields are pre-filled).
    *   In case of login failure, only the token is nullified; fields remain filled, allowing users to retry.
    *   Token validity is continuously monitored during polling; if expired or invalid, the client automatically re-logs in or redirects to the login screen.
*   **Local SQLite Database Architecture Documentation:**
    *   An embedded SQLite database in the `core` module manages message history, friend lists, friend requests, and event logs.
    *   The persistence layer is accessed via DAOs, with the Presenter interacting only with the Model.
    *   Documentation (README.md, chat_client_execution_plan.md, chat_design.md, etc.) details MVP integration and multiplatform support (Java, Android, .NET/C#).
*   **Presenter-DAO Integration, Friend List & Message Persistence:**
    *   `MainPresenter` integrates the persistence layer (DBService, FriendDao, MessageDao).
    *   Downloaded friends and messages are saved to the database first, then the UI is updated from the DB.
    *   When sending a message, it's saved to the DB before being sent to the server.
    *   During polling, data from the server is saved to the DB, and the UI always reads from the DB.
    *   Database content persists after application shutdown in the `chatapp.db` file.
*   **SQLite Persistence Layer Implementation:**
    *   `DBService` (database initialization, table creation) is complete.
    *   `MessageDao` (CRUD for messages), `FriendDao` (CRUD for friends), `FriendRequestDao` (CRUD for friend requests), `EventLogDao` (CRUD for event logs) are implemented.
    *   DAOs are part of the Model layer within the MVP architecture, accessed by the Presenter for local data.
    *   The project now supports persistent local storage for message history, friend lists, friend requests, and events.
*   **Profile View and Editing:**
    *   `ProfileView` and `ProfilePresenter` are complete.
    *   Users can view and edit their name and avatar URL.
    *   Modifications are updated only in the local database and memory, as the server-side API for profile data modification is not yet supported.
    *   A "Profile..." menu item in the main menu opens the editor view.
