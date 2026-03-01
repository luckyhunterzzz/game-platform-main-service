# 🎮 Game Ops Platform - Main Service

The Main Service is the core business logic engine of the GameOps Platform. It handles content management, game data, and administrative operations.

## ⚙️ Architecture: Trust-Based Security

This service operates behind the **API Gateway**. It does not communicate with Keycloak directly for every request. Instead, it relies on the Gateway to:
1. Validate the JWT.
2. Extract user identity.
3. Pass sanitized information via HTTP headers.

### Security Implementation
* **`GatewayHeadersAuthFilter`**: A custom security filter that intercepts incoming requests, reads `X-User-Id` and `X-User-Roles`, and populates the `SecurityContext`.
* **RBAC**: Enforces internal security using standard Spring Security annotations (e.g., `@PreAuthorize`).
* **Role Prefixing**: Automatically prepends `ROLE_` to authorities received from the Gateway to stay compatible with Spring's `hasRole()` checks.

---

## 🔐 Internal Security Mapping

Downstream headers are mapped to Spring Security Authorities:

| Header | Description | Mapping |
| :--- | :--- | :--- |
| `X-User-Id` | Unique user UUID | Set as `Principal` |
| `X-User-Roles` | Raw roles from Keycloak | Converted to `ROLE_<name>` |

**Example of usage in Controller:**
```java
@GetMapping("/admin/test")
@PreAuthorize("hasRole('admin')")
public ResponseEntity<?> adminAction() {
    return ResponseEntity.ok("Access granted to admin");
}
```

## 📡 API Endpoints (v1)
| Path | Access | Description |
| :--- | :--- | :--- |
| `GET /api/v1/public/**` | PermitAll | Publicly available content |
| `GET /api/v1/admin/test` | ROLE_admin ROLE_superadmin | Diagnostic endpoint for RBAC verification |
| `ANY /api/v1/**` | Authenticated | Generic protected resources |

## 🛠 Tech Stack
Framework: Spring Boot 3.5 (Servlet Stack)
Security: Spring Security (Custom Header Filter)
Observability: Micrometer + Actuator (Health & Metrics)

## 🚀 Development & Integration
### Prerequisites
The service requires the API Gateway to be running for proper authentication. Direct calls will fail to authenticate unless headers are manually mocked (for testing purposes).
### Running in Docker
```java
# Handled by the Infrastructure repository
docker compose up -d main-service
```
### Local DebuggingIf running locally, ensure the service points to the correct database (configured in application-dev.yml or via env variables).

## 📝 Diagnostic Response
The `/admin/test` endpoint returns a detailed diagnostic JSON:
`authCheck`: Boolean flags for `isAdmin/isSuperadmin`.
`userId`: The UUID extracted from headers.
`roles`: The full list of processed `ROLE_` authorities.
