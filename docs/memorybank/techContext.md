# Tech Context

This document details the technologies, development setup, and technical constraints for the project.

## Technologies Used:

*   **Programming Language:** Java
*   **GUI Framework:** Java Swing
*   **HTTP Client:** OkHttp
*   **JSON Processing:** Gson
*   **Build Tool:** Maven

## Development Setup:

*   **IDE:** IntelliJ IDEA Community Edition
*   **Operating System:** Windows 11
*   **Project Structure:** Multi-module Maven project with `core` and `ui-swing` modules.

## Technical Constraints:

*   **Platform:** The client application is designed to run on desktop environments supporting Java.
*   **Server Dependency:** Requires a running chat server with a compatible API.
*   **Java Version:** Compatibility with a specific Java version should be maintained (e.g., Java 11 or later, depending on project requirements).

## Dependencies:

The `core` module will depend on:
*   OkHttp (for HTTP requests)
*   Gson (for JSON serialization/deserialization)

The `ui-swing` module will depend on:
*   The `core` module.
*   Standard Java Swing libraries.

All dependencies will be managed via Maven.
