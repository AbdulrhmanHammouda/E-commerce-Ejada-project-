# Ejada E-Commerce Platform: Microservices Security Architecture

This document explains the complete authentication, authorization, and Role-Based Access Control (RBAC) architecture of the Ejada E-commerce platform. Because this is a **Microservices** architecture, security is distributed across multiple separate servers.

---

## 1. The Core Concept

In this platform, we have 3 main players involved in security:
1. **Wallet Service (The Identity Provider):** Handles checking passwords, registering users in the MySQL database, and creating the "Passport" (JWT Token).
2. **API Gateway (The Bouncer):** The single entry-point for the entire system. It checks the Passport (JWT), ensures it is valid, extracts the user's Role, and acts as a shield for all other services.
3. **Shop Service (The Destination):** Contains business logic (like buying products). It does not know how to verify JWTs; it completely trusts the API Gateway to do the security checks.

---

## 2. The Login / Register Flow (Wallet Service)

When a user tries to create an account or log in, they talk directly to the **Wallet Service**.

### Key Files Involved:
* `wallet-service/src/main/java/com/example/walletservice/service/AuthService.java`
* `wallet-service/src/main/java/com/example/walletservice/service/JwtService.java`

### Step-by-Step Flow:
1. **Request:** The user sends a `POST` request with their email and password to `/api/auth/login`.
2. **Database Check:** `AuthService.java` looks up the user in the database and verifies the password.
3. **Role Injection:** If the password is correct, `AuthService` creates a Map (dictionary) and injects the user's role:
   ```java
   java.util.Map<String, Object> claims = new java.util.HashMap<>();
   claims.put("role", user.getRole().name()); // Example: "ADMIN" or "USER"
   ```
4. **Token Generation:** It passes this data to `JwtService.java`, which uses a cryptographic **Secret Key** (`jwt.secret` from your `wallet-service.properties`) to generate a JSON Web Token (JWT). The `"role": "ADMIN"` data is permanently and securely baked into the token's payload.
5. **Response:** The Wallet service hands this Token back to the user.

---

## 3. The Security Bouncer (API Gateway)

Now that the user has their token, they want to interact with the **Shop Service** (e.g., they want to add a new product). 

They send a `POST` request to `/api/shop/products`, and attach their token in the HTTP Header: `Authorization: Bearer <token>`.

Because the API Gateway sits in front of the entire network, the request hits the Gateway first.

### Key Files Involved:
* `api-gateway/src/main/java/com/example/apigateway/util/JwtUtil.java`
* `api-gateway/src/main/java/com/example/apigateway/filter/AuthenticationFilter.java`

### Step-by-Step Flow:
1. **The Interceptor:** `AuthenticationFilter.java` intercepts the request. It checks if the path is `/api/auth/**`. If it is, it lets the request through immediately (because users need to be able to log in without a token!).
2. **Token Extraction:** For all other requests (like `/api/shop`), it grabs the token from the header.
3. **Mathematical Verification:** It passes the token to `JwtUtil.java`, which uses the *exact same Secret Key* to verify the signature. If the signature is invalid (meaning a hacker tampered with the token), it instantly rejects the request (`401 Unauthorized`).
4. **Role Extraction:** `JwtUtil` decodes the token's payload and extracts the hidden `"role"` string (e.g., `"ADMIN"`).
5. **The Wrapper Hack:** Because Java does not allow modifying incoming HTTP requests, `AuthenticationFilter.java` creates a "Fake Wrapper" around the request. It tells this wrapper: *"If the Shop Service asks you for a header named `X-User-Role`, give them the role we just found."*
6. **Forwarding:** The Gateway forwards this modified wrapper to the Shop Service.

---

## 4. Role-Based Access Control (Shop Service)

The request finally arrives at the Shop Service. The Shop Service has absolutely zero Spring Security or JWT logic. It relies purely on the HTTP Headers provided by the Gateway.

### Key Files Involved:
* `shop-service/src/main/java/com/example/shopservice/controller/ProductController.java`

### Step-by-Step Flow:
1. **Endpoint Access:** The request reaches the `@PostMapping` inside `ProductController.java`.
2. **Reading the Header:** The Controller is configured to read the custom header the Gateway injected: `@RequestHeader("X-User-Role") String role`.
3. **The Final Check:**
   ```java
   if (!"ADMIN".equals(role)) {
       return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden!
   }
   ```
   If the role is `"USER"`, the request is rejected. If it is `"ADMIN"`, the product is successfully saved to the database.

---

## Summary of the Journey

1. **Wallet Service:** Signs the token and hides the Role inside.
2. **API Gateway:** Verifies the signature, extracts the Role, and smuggles it in an HTTP Header (`X-User-Role`).
3. **Shop Service:** Reads the HTTP Header and decides whether to allow or block the action based on the Role.

This allows us to maintain strict security without having to copy-and-paste JWT parsing logic into the Shop Service, the Inventory Service, the Shipping Service, etc.
