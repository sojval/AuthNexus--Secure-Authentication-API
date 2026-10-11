# AuthNexus — Secure Authentication API

A Spring Boot authentication system that provides secure user authentication and authorization using JWT, refresh tokens, and Google OAuth2 login.

## Features

- **User Registration** — Register users with their name, email, and password.
- **User Login** — Authenticate users using email and password.
- **JWT Authentication** — Secure API endpoints using JSON Web Tokens.
- **Refresh Tokens** — Generate new access tokens without requiring users to log in again.
- **Refresh Token Cookies** — Manage refresh tokens using HTTP cookies.
- **Google OAuth2 Login** — Support authentication through Google.
- **Spring Security** — Protect endpoints and manage authentication.
- **Global Exception Handling** — Return consistent error responses.
- **PostgreSQL/MySQL Support** — Configure your preferred supported relational database.

## Tech Stack

- Java
- Spring Boot
- Spring Security
- Spring Data JPA / Hibernate
- JWT (JSON Web Tokens)
- OAuth2 / OpenID Connect
- Maven
- SQL database

## Project Structure

```text
src/main/java/com/Sojval/Auth/
├── Configuration/
│   └── SecurityConfig.java
├── Controller/
│   └── AuthController.java
├── Dtos/
│   ├── ApiError.java
│   ├── LoginRequest.java
│   ├── RefreshTokenRequest.java
│   └── TokenResponse.java
├── Entity/
│   ├── JwtProperties.java
│   └── RefreshToken.java
├── Exceptions/
│   └── GlobalExceptionHandler.java
├── Repository/
│   ├── RefreshTokenRepository.java
│   └── UserRepository.java
├── Security/
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtService.java
│   └── OAuth2SuccessHandler.java
└── Service/
    └── CookieService.java
```

*Note: The structure above highlights the main authentication components. Other application classes may exist in the project.*

## Prerequisites

Install the following before running the application:

- JDK 23 (or the JDK version configured in `pom.xml`)
- Maven, or use the included Maven Wrapper if available
- PostgreSQL or MySQL
- A Google Cloud project with OAuth 2.0 credentials, if using Google login

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/sojval/AuthSystem.git
cd AuthSystem
```

### 2. Configure environment variables

Configure your database credentials, JWT signing secret, and OAuth credentials using environment variables.

For Google OAuth2, set these variables in PowerShell:

```powershell
$env:GOOGLE_CLIENT_ID="your-google-client-id"
$env:GOOGLE_CLIENT_SECRET="your-google-client-secret"
```

Reference them in `application.properties`:

```properties
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
```

Configure the database connection and JWT properties according to your application's configuration.

**Security:** Never commit real passwords, OAuth client secrets, or JWT signing secrets to GitHub.

### 3. Configure the database

Create a database in your chosen SQL database server and configure the JDBC URL, username, and password in `application.properties` using environment variables.

Ensure the database settings match your selected database and JPA configuration.

### 4. Run the application

Using Maven:

```bash
mvn spring-boot:run
```

Or, if your project includes the Maven Wrapper:

```bash
.\mvnw.cmd spring-boot:run
```

The application will start on the configured server port, typically `8080`.

## Authentication Flow

### Registration and Login

1. A user registers with their details.
2. The user logs in using their credentials.
3. Spring Security validates the credentials.
4. The application issues an access token and a refresh token.
5. The access token is used to access protected endpoints.
6. The refresh token can be used to obtain a new access token,
