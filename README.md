# NutriFlow — Personalized Nutrition Delivery Platform

![Java](https://img.shields.io/badge/Java-17-orange?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-blue?logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-7-red?logo=redis)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)
![Stripe](https://img.shields.io/badge/Stripe-Payments-purple?logo=stripe)

---

## Table of Contents

1. [What is NutriFlow?](#1-what-is-nutriflow)
2. [Before You Start — Concepts for Beginners](#2-before-you-start--concepts-for-beginners)
3. [Tech Stack at a Glance](#3-tech-stack-at-a-glance)
4. [Project Folder Structure](#4-project-folder-structure)
5. [How the Business Works (End-to-End Flow)](#5-how-the-business-works-end-to-end-flow)
6. [User Roles](#6-user-roles)
7. [Database Design](#7-database-design)
8. [Security Architecture](#8-security-architecture)
9. [REST API Reference](#9-rest-api-reference)
10. [Scheduled (Background) Tasks](#10-scheduled-background-tasks)
11. [Email & Notifications](#11-email--notifications)
12. [Payments with Stripe](#12-payments-with-stripe)
13. [File Uploads](#13-file-uploads)
14. [Activity Audit Log](#14-activity-audit-log)
15. [Configuration Reference](#15-configuration-reference)
16. [Prerequisites](#16-prerequisites)
17. [Running the Project Locally](#17-running-the-project-locally)
18. [Docker Setup](#18-docker-setup)
19. [Default Credentials for Testing](#19-default-credentials-for-testing)
20. [Swagger / API Documentation UI](#20-swagger--api-documentation-ui)
21. [Error Handling](#21-error-handling)
22. [Testing Scheduled Tasks Manually](#22-testing-scheduled-tasks-manually)

---

## 1. What is NutriFlow?

NutriFlow is a **full-stack backend REST API** for a personalized nutrition delivery service.

Here is the big picture of what NutriFlow does:

- A **user** registers, fills in their health profile (height, weight, goals, dietary restrictions, medical files), and pays for a subscription.
- A **dietitian** studies that user's health data and creates a personalized monthly meal plan.
- The user reviews and approves the plan.
- A **caterer** prepares and delivers the meals daily.
- **Admins** manage the entire platform: they assign dietitians and caterers to users, monitor subscriptions, view payments, and receive weekly reports.

> **This project is the backend only.** It exposes HTTP REST APIs that a frontend (React, Vue, Angular, mobile app, etc.) can call.

---

## 2. Before You Start — Concepts for Beginners

If you are new to Spring Boot or Java backends, here is a plain-English explanation of the main concepts used in this project.

### What is Spring Boot?
Spring Boot is a Java framework that helps developers build web APIs quickly. Instead of manually configuring every component, Spring Boot auto-configures them. You write classes annotated with special markers (like `@RestController`, `@Service`, `@Repository`) and Spring connects them all together.

### What is a REST API?
A REST API is a set of URLs (called endpoints) that a client can send HTTP requests to. For example:
- `POST /api/v1/auth/login` — send an email and password, get back a token
- `GET /api/v1/user/dashboard/summary` — get the current user's dashboard data

### What is JPA / Hibernate?
JPA (Java Persistence API) lets you write your database tables as Java classes (called **entities**). Instead of writing raw SQL to insert/select rows, you call Java methods. Hibernate is the engine that translates those Java calls into SQL behind the scenes.

### What is Liquibase?
Liquibase manages your database schema through version-controlled migration files (YAML in this project). Every time the app starts, Liquibase checks which migrations have already been applied and runs only the new ones. This ensures every developer and environment has the same database structure.

### What is JWT?
JWT (JSON Web Token) is like a temporary digital ID card. After you log in, the server gives you a signed token. You send this token in every future request inside the `Authorization: Bearer <token>` header. The server verifies it without needing to query the database every time.

### What is Redis?
Redis is an extremely fast in-memory data store. NutriFlow uses it to:
- Store OTP (one-time passwords) with a short expiry time
- Store refresh tokens (7-day session tokens)

### What is Docker Compose?
Docker Compose lets you start multiple services (PostgreSQL, Redis) with a single command. You do not need to install PostgreSQL or Redis on your machine manually.

### What is Stripe?
Stripe is a payment processing platform. NutriFlow uses Stripe to create subscription checkout sessions and receive payment confirmation via webhooks.

### What is AOP (Aspect-Oriented Programming)?
AOP allows you to inject behaviour (like logging) into methods automatically, without modifying those methods directly. NutriFlow uses AOP to log slow methods and track execution times across all controllers, services, and repositories.

---

## 3. Tech Stack at a Glance

| Category | Technology | Purpose |
|---|---|---|
| Language | Java 17 | Core programming language |
| Framework | Spring Boot 3.4.2 | Web, Security, Data, Scheduling |
| Build Tool | Gradle (Groovy DSL) | Dependency management, build |
| Database | PostgreSQL 18 | Persistent relational storage |
| ORM | Spring Data JPA + Hibernate | Java ↔ Database mapping |
| DB Migrations | Liquibase | Schema versioning |
| Security | Spring Security + JWT + Google OAuth2 | Authentication & authorization |
| Cache / Sessions | Redis 7 | OTP & refresh token storage |
| Email | Spring Mail (Gmail SMTP) | OTP emails, notifications |
| Payments | Stripe Java SDK 24.0.0 | Subscription checkout |
| File Storage | Local filesystem | Medical file uploads |
| API Docs | SpringDoc OpenAPI / Swagger UI | Interactive API browser |
| Logging / AOP | Spring AOP | Performance & audit logging |
| Async Tasks | `@EnableAsync` | Non-blocking email sending |
| Scheduling | `@EnableScheduling` | Cron jobs & background tasks |
| Containerization | Docker + Docker Compose | Portable local dev environment |
| Testing | JUnit 5, Spring Security Test | Unit & integration tests |

---

## 4. Project Folder Structure

```
nutriflow/
├── src/
│   ├── main/
│   │   ├── java/com/nutriflow/
│   │   │   ├── NutriflowApplication.java      ← App entry point
│   │   │   │
│   │   │   ├── aspect/                        ← AOP logging (cross-cutting concerns)
│   │   │   │   └── EnhancedLoggingAspect.java
│   │   │   │
│   │   │   ├── config/                        ← Spring configuration beans
│   │   │   │   ├── AopConfig.java             ← Enables AOP
│   │   │   │   ├── DataInitializer.java       ← Seeds default users on startup
│   │   │   │   ├── RedisConfig.java           ← Redis template beans
│   │   │   │   └── SchedulingConfig.java      ← Enables scheduling
│   │   │   │
│   │   │   ├── constants/                     ← String constants (no magic strings)
│   │   │   │   ├── ActionType.java
│   │   │   │   ├── AuthMessages.java
│   │   │   │   ├── FileConstants.java
│   │   │   │   ├── LoggingConstants.java
│   │   │   │   └── LogMessages.java
│   │   │   │
│   │   │   ├── controllers/                   ← HTTP request handlers (thin layer)
│   │   │   │   ├── AdminController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── CatererController.java
│   │   │   │   ├── DietitianController.java
│   │   │   │   ├── HealthProfileController.java
│   │   │   │   ├── MedicalFileController.java
│   │   │   │   ├── PaymentController.java
│   │   │   │   ├── UserController.java
│   │   │   │   └── admin/
│   │   │   │       └── SchedulerController.java  ← Manual test triggers
│   │   │   │
│   │   │   ├── dto/                           ← Data Transfer Objects (API shapes)
│   │   │   │   ├── request/                   ← What the client sends IN
│   │   │   │   └── response/                  ← What the server sends OUT
│   │   │   │
│   │   │   ├── entities/                      ← JPA database table mappings
│   │   │   ├── enums/                         ← Allowed constant values
│   │   │   ├── exceptions/                    ← Custom exceptions + global handler
│   │   │   ├── helpers/                       ← Utility helper classes
│   │   │   ├── mappers/                       ← Entity ↔ DTO conversion
│   │   │   │
│   │   │   ├── repositories/                  ← Database query interfaces
│   │   │   │
│   │   │   ├── scheduler/                     ← Cron job classes
│   │   │   │   ├── DatabaseCleanupScheduler.java
│   │   │   │   ├── RedisCleanupScheduler.java
│   │   │   │   └── SubscriptionScheduler.java
│   │   │   │
│   │   │   ├── security/                      ← JWT, OAuth2, filters
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtService.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── OAuth2SuccessHandler.java
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   └── SecurityUser.java
│   │   │   │
│   │   │   ├── services/                      ← Business logic interfaces
│   │   │   │   └── impl/                      ← Business logic implementations
│   │   │   │
│   │   │   └── utils/                         ← General utility functions
│   │   │
│   │   └── resources/
│   │       ├── application.properties         ← Shared config
│   │       ├── application-local.properties   ← Local secrets (DB, JWT, Stripe…)
│   │       └── db/changelog/
│   │           ├── db.changelog-master.yaml
│   │           └── changes/
│   │               ├── 001-create-base-tables.yaml
│   │               ├── 002-create-user-related-tables.yaml
│   │               ├── 003-create-menu-tables.yaml
│   │               ├── 004-create-delivery-payment-tables.yaml
│   │               └── 005-create-activity-log-table.yaml
│   │
│   └── test/java/com/nutriflow/
│       └── NutriflowApplicationTests.java
│
├── uploads/
│   └── medical-files/                         ← Uploaded medical documents stored here
│
├── build.gradle                               ← Project dependencies & build config
├── docker-compose.yml                         ← PostgreSQL + Redis containers
├── Dockerfile                                 ← Full app container build
└── settings.gradle
```

### Layered Architecture Explained

NutriFlow follows a classic **Controller → Service → Repository** layered architecture:

```
HTTP Request
     ↓
[ Controller ]   ← Receives request, validates input, calls service
     ↓
[  Service   ]   ← Contains all business logic, calls repositories
     ↓
[ Repository ]   ← Talks to the database via JPA
     ↓
[ Database   ]   ← PostgreSQL
```

- **Controllers** are thin. They only parse HTTP requests and delegate to services.
- **Services** contain all business rules, calculations, and orchestration.
- **Repositories** are interfaces that Spring auto-implements — just declare the method name and Spring generates the SQL.

---

## 5. How the Business Works (End-to-End Flow)

### Complete User Journey

```
Step 1  ─ User Registration
          POST /api/v1/auth/register
          → Account created (status: REGISTERED)
          → OTP verification email sent

Step 2  ─ Email Verification
          POST /api/v1/auth/verify  { email, otp }
          → Account verified (status: VERIFIED)
          → JWT access token + refresh token returned

Step 3  ─ Health Profile Submission
          POST /api/v1/health-profile/submit  (multipart/form-data)
          → Height, weight, goal, dietary restrictions, medical files saved
          → Account status → DATA_SUBMITTED

Step 4  ─ Subscription Payment
          POST /api/v1/payments/subscribe
          → Stripe Checkout Session URL returned
          → User redirected to Stripe hosted payment page

Step 5  ─ Payment Webhook (automatic)
          POST /api/v1/payments/webhook  ← Stripe calls this
          → Subscription created (status: ACTIVE) for 1 month
          → Account status → ACTIVE

Step 6  ─ Admin Assigns Dietitian & Caterer
          Admin reviews the user's health data and assigns:
          PATCH /api/v1/admin/users/{userId}/assign-dietitian/{dietitianId}
          PATCH /api/v1/admin/users/{userId}/assign-caterer/{catererId}

Step 7  ─ Dietitian Creates Monthly Meal Plan
          POST /api/v1/dietitian/create-menu
          → Menu in DRAFT status
          PATCH /api/v1/dietitian/batch/{batchId}/submit
          → Menu status → SUBMITTED (user can now review it)

Step 8  ─ User Approves or Rejects the Menu
          POST /api/v1/user/menu/approve  → status: APPROVED
          POST /api/v1/user/menu/reject   → status: REJECTED
          (If rejected, dietitian revises and resubmits)

Step 9  ─ Meal Deliveries Begin
          Caterer sees daily delivery list
          GET  /api/v1/caterer/deliveries
          PATCH /api/v1/caterer/deliveries/{id}/status
          → DeliveryStatus: PENDING → IN_PROGRESS → READY → ON_THE_WAY → DELIVERED

Step 10 ─ Subscription Expiry (automated)
          SubscriptionScheduler runs daily at 01:00
          → Checks subscription end dates
          → Expired subscriptions deactivated
          → User status → EXPIRED
          → Warning emails sent 7 days before expiry
```

### Menu Status Lifecycle

```
DRAFT  →  SUBMITTED  →  APPROVED    (deliveries auto-generated)
                     →  REJECTED    (dietitian must revise)
                     →  CANCELLED
            ↑
      (dietitian updates rejected batch and resubmits)
```

### Delivery Status Lifecycle

```
PENDING  →  IN_PROGRESS  →  READY  →  ON_THE_WAY  →  DELIVERED
                                                   →  FAILED
```

---

## 6. User Roles

NutriFlow has 5 distinct roles, each with different access rights:

| Role | Description | Can Access |
|---|---|---|
| `SUPER_ADMIN` | Platform owner | Everything, including creating sub-admins |
| `ADMIN` | Platform manager (sub-admin) | Most admin features, can NOT manage other admins |
| `DIETITIAN` | Nutrition specialist | Their assigned patients, menu creation |
| `CATERER` | Food delivery company | Their daily delivery list |
| `USER` | End customer | Their own data, menus, deliveries |

### How Roles Work in Code

Each HTTP endpoint is protected with `@PreAuthorize`. For example:

```java
// Only SUPER_ADMIN can create sub-admins
@PreAuthorize("hasRole('SUPER_ADMIN')")
@PostMapping("/sub-admins")
public ResponseEntity<?> createSubAdmin(...) { ... }

// Both ADMIN and SUPER_ADMIN can view users
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
@GetMapping("/users")
public ResponseEntity<?> getAllUsers(...) { ... }
```

---

## 7. Database Design

### Entity Relationship Overview

```
AdminEntity          ── standalone table (platform staff)
DietitianEntity      ── assigned to many users
CatererEntity        ── assigned to many users
UserEntity ──────────── has one → AddressEntity
           │          ├─ has one → HealthProfileEntity
           │          │              └─ has many → MedicalFileEntity
           │          ├─ has one → SubscriptionEntity
           │          │              └─ has one → PaymentEntity
           │          ├─ many-to-one → DietitianEntity
           │          ├─ many-to-one → CatererEntity
           │          ├─ has many → MenuEntity
           │          └─ has many → DeliveryEntity
MenuEntity ──────────── has many → MenuBatchEntity
                                      └─ has many → MenuItemEntity
DeliveryEntity       ── references User, Caterer, Address, MenuBatch
OtpEntity            ── standalone (email + code + expiry)
ActivityLogEntity    ── audit trail (who did what, when, on which record)
```

### All Database Tables

| Table | Key Columns |
|---|---|
| `admins` | id, first_name, last_name, email, password, role, is_super_admin, is_active |
| `dietitians` | id, first_name, last_name, email, password, phone, specialization, role, is_active |
| `caterers` | id, name, email, password, phone, address, role, status |
| `users` | id, first_name, last_name, email, password, phone, role, status, is_email_verified, dietitian_id, caterer_id |
| `addresses` | id, user_id, address_details, city, district, delivery_notes |
| `health_profiles` | id, user_id (unique), height, weight, goal, restrictions, notes |
| `medical_files` | id, health_profile_id, file_url, file_name, file_type |
| `subscriptions` | id, user_id (unique), plan_name, price, status, start_date, end_date |
| `payments` | id, subscription_id (unique), provider, amount, status, transaction_ref |
| `menus` | id, user_id, dietitian_id, year, month, dietary_notes — unique(user, year, month) |
| `menu_batches` | id, menu_id, status, rejection_reason |
| `menu_items` | id, batch_id, day_number, meal_type, description, calories, protein, carbs, fats — unique(batch, day, meal_type) |
| `deliveries` | id, user_id, caterer_id, address_id, batch_id, delivery_date, status, estimated_delivery_time |
| `otps` | id, code, email, expires_at, is_used |
| `activity_logs` | id, actor_type, actor_id, action, entity_type, entity_id, old_value, new_value, ip_address |

### All Enums (Allowed Values)

| Enum | Values |
|---|---|
| `Role` | `SUPER_ADMIN`, `ADMIN`, `USER`, `DIETITIAN`, `CATERER` |
| `UserStatus` | `REGISTERED` → `VERIFIED` → `DATA_SUBMITTED` → `ACTIVE` → `EXPIRED` |
| `CatererStatus` | `ACTIVE`, `INACTIVE`, `SUSPENDED` |
| `DeliveryStatus` | `PENDING`, `IN_PROGRESS`, `READY`, `ON_THE_WAY`, `DELIVERED`, `FAILED` |
| `GoalType` | `LOSE`, `MAINTAIN`, `GAIN` |
| `MealType` | `BREAKFAST`, `LUNCH`, `DINNER`, `SNACK` |
| `MenuStatus` | `DRAFT`, `SUBMITTED`, `PREPARING`, `APPROVED`, `REJECTED`, `CANCELLED` |
| `PaymentStatus` | `PENDING`, `SUCCESS`, `FAILED`, `CANCELLED` |
| `SubscriptionStatus` | `ACTIVE`, `EXPIRED`, `CANCELLED` |
| `OperationStatus` | `SUCCESS`, `FAILURE`, `PENDING` |

---

## 8. Security Architecture

### Authentication Flow

```
Client sends: POST /api/v1/auth/login  { email, password }
                         ↓
          CustomUserDetailsService.loadUserByUsername()
          → Searches admins → dietitians → caterers → users (in order)
          → Checks isActive + isEmailVerified
                         ↓
          BCryptPasswordEncoder.matches(raw, hashed)
                         ↓
          JwtService.generateAccessToken()   ← 24-hour JWT
          JwtService.generateRefreshToken()  ← 7-day token (stored in Redis)
                         ↓
          Response: { accessToken, refreshToken, user details }
```

### Every Subsequent Request

```
Client sends: GET /api/v1/user/dashboard  
              Header: Authorization: Bearer <access_token>
                         ↓
          JwtAuthenticationFilter.doFilterInternal()
          → Extracts token from header
          → JwtService.validateToken() — checks signature + expiry
          → Sets SecurityContextHolder with user identity
                         ↓
          Controller @PreAuthorize check passes
                         ↓
          Response: dashboard data
```

### Google OAuth2 Login

```
Client visits: GET /oauth2/authorization/google
                         ↓
                   Google login page
                         ↓
          OAuth2SuccessHandler.onAuthenticationSuccess()
          → Auto-creates user account (status: VERIFIED) if first time
          → Generates JWT tokens
          → Stores refresh token in Redis
          → Redirects to frontend:
            ├─ /tell-us-about-yourself   (if no health profile)
            ├─ /choose-plan              (if no active subscription)
            └─ /dashboard               (fully active user)
```

### Token Refresh

```
Client sends: POST /api/v1/auth/refresh-token
              Header: Authorization: Bearer <refresh_token>
                         ↓
          Validates token in Redis
          → Issues new access token
          → Returns TokenResponse
```

### Publicly Accessible Endpoints (No Token Needed)

- `/api/v1/auth/**` — all auth endpoints
- `/oauth2/**` and `/login/oauth2/**` — Google OAuth
- `/api/v1/payments/webhook` — Stripe webhook
- `/swagger-ui/**` and `/v3/api-docs/**` — API documentation
- `/api/admin/scheduler-test/**` — manual scheduler testing

### CORS Configuration

Allowed origins: `localhost:3000`, `localhost:5173`, `localhost:4200`, `localhost:3001`  
Credentials: allowed (for cookie-based auth in future)

---

## 9. REST API Reference

All endpoints use base path `/api/v1/` unless noted. All protected endpoints require:
```
Authorization: Bearer <your_access_token>
```

---

### Authentication — `/api/v1/auth` (Public)

| Method | Endpoint | Body / Params | Description |
|---|---|---|---|
| `POST` | `/register` | `{ firstName, lastName, email, password }` | Register new user, receive OTP email |
| `POST` | `/verify` | `{ email, otp }` | Verify OTP, get JWT tokens |
| `POST` | `/login` | `{ email, password }` | Login, get JWT tokens |
| `GET` | `/google-login-url` | — | Returns Google OAuth2 redirect URL |
| `POST` | `/refresh-token` | `Authorization: Bearer <refresh_token>` | Get new access token |
| `POST` | `/resend-otp` | `{ email }` | Resend email OTP |
| `POST` | `/forgot-password` | `{ email }` | Send password reset OTP |
| `POST` | `/reset-password` | `{ email, otp, newPassword }` | Reset password with OTP |

---

### User — `/api/v1/user` (Role: `USER`)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/dashboard/summary` | User dashboard overview |
| `GET` | `/my-menu` | Active menus for the current month |
| `POST` | `/menu/approve` | Approve meal plan (with delivery notes) |
| `POST` | `/menu/reject` | Reject meal plan with reason |
| `GET` | `/medical-profile` | View own health profile & medical files |
| `PUT` | `/profile/update` | Update personal info |
| `POST` | `/subscription/cancel` | Cancel subscription |
| `GET` | `/deliveries` | View delivery history |
| `GET` | `/subscription/info` | Subscription and payment details |
| `GET` | `/personal-info` | Personal info summary |

---

### Health Profile — `/api/v1/health-profile` (Role: `USER`)

| Method | Endpoint | Body | Description |
|---|---|---|---|
| `POST` | `/submit` | `multipart/form-data` | Submit health data + medical file uploads |

The multipart form includes:
- `data` — JSON: `{ height, weight, goal, restrictions, notes, address {...} }`
- `files` — One or more medical files (PDF, images, etc., max 10 MB each)

---

### Medical Files — `/api/v1/medical-files`

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| `GET` | `/{fileId}/download` | `USER`, `DIETITIAN`, `ADMIN` | Securely download a medical file |

---

### Dietitian — `/api/v1/dietitian` (Role: `DIETITIAN`)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/my-users` | List all assigned patients |
| `POST` | `/create-menu` | Create a monthly meal plan for a patient |
| `GET` | `/profile` | Get own profile |
| `PUT` | `/profile` | Update own profile |
| `GET` | `/dashboard/stats` | Dashboard statistics |
| `GET` | `/patients/urgent` | Patients needing urgent attention |
| `GET` | `/patient/{userId}/profile` | Patient's health profile |
| `GET` | `/menu/{userId}` | Patient's current monthly menu |
| `PATCH` | `/batch/{batchId}/submit` | Submit menu batch for user review |
| `GET` | `/patients/search` | Search patients by name or email |
| `GET` | `/batch/{batchId}/rejection-reason` | View rejection reason |
| `GET` | `/patient/file/{fileId}` | Get download URL of a patient's file |
| `GET` | `/batch/{batchId}/items` | Get all menu items in a batch |
| `PUT` | `/batch/{batchId}/update` | Update a rejected menu batch |
| `DELETE` | `/batch/{batchId}/delete-content` | Delete specific day/meal from batch |

**Create Menu Request Example:**
```json
{
  "userId": 5,
  "year": 2026,
  "month": 3,
  "dietaryNotes": "Avoid gluten. High protein diet recommended.",
  "menuItems": [
    {
      "dayNumber": 1,
      "mealType": "BREAKFAST",
      "description": "Oat porridge with berries",
      "calories": 350,
      "protein": 12,
      "carbs": 55,
      "fats": 8
    }
  ]
}
```

---

### Caterer — `/api/v1/caterer` (Role: `CATERER`)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/stats` | Delivery statistics dashboard |
| `GET` | `/deliveries` | Daily deliveries (filter by name, district, date) |
| `PATCH` | `/deliveries/{id}/status` | Update delivery status + optional note |
| `GET` | `/profile` | Get own profile |
| `PUT` | `/profile` | Update own profile |
| `PUT` | `/deliveries/{id}/estimate` | Set estimated delivery time |
| `PATCH` | `/deliveries/failed` | Mark a delivery as failed |

---

### Payments — `/api/v1/payments`

| Method | Endpoint | Role | Description |
|---|---|---|---|
| `POST` | `/subscribe` | `USER` | Create Stripe checkout session, returns URL |
| `POST` | `/webhook` | Public | Stripe webhook — confirms payment, activates subscription |

**Subscribe Response:**
```json
{
  "checkoutUrl": "https://checkout.stripe.com/pay/cs_test_..."
}
```

---

### Admin — `/api/v1/admin` (Role: `ADMIN` or `SUPER_ADMIN`)

#### Users
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/users` | All users (pageable: `?page=0&size=10`) |
| `GET` | `/users/{id}` | User by ID |
| `GET` | `/users/search` | Search users (`?query=...`) |
| `POST` | `/users` | Create user |
| `PUT` | `/users/{id}` | Edit user |
| `DELETE` | `/users/{id}` | Delete user |
| `PATCH` | `/users/{id}/toggle-status` | Activate / deactivate user |
| `GET` | `/users/pending-assignments` | Users without a dietitian |
| `GET` | `/users/pending-caterer-assignments` | Users without a caterer |
| `POST` | `/users/{userId}/assign-dietitian/{dietitianId}` | Assign dietitian |
| `POST` | `/users/{userId}/assign-caterer/{catererId}` | Assign caterer |
| `PATCH` | `/users/{userId}/reassign-dietitian` | Change dietitian |
| `PATCH` | `/users/{userId}/reassign-caterer` | Change caterer |

#### Dietitians & Caterers
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/dietitians` | All dietitians (pageable) |
| `GET` | `/dietitians/{id}` | Dietitian by ID |
| `GET` | `/dietitians/search` | Search dietitians |
| `POST` | `/dietitians` | Create dietitian |
| `PUT` | `/dietitians/{id}` | Edit dietitian |
| `DELETE` | `/dietitians/{id}` | Delete dietitian |
| `PATCH` | `/dietitians/{id}/toggle-status` | Toggle active status |
| `GET` | `/caterers` | All caterers (pageable) |
| `POST` | `/caterers` | Create caterer |
| `PUT` | `/caterers/{id}` | Edit caterer |
| `DELETE` | `/caterers/{id}` | Delete caterer |
| `PATCH` | `/caterers/{id}/toggle-status` | Toggle active status |

#### Sub-Admins (SUPER_ADMIN only)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/sub-admins` | All sub-admins |
| `POST` | `/sub-admins` | Create sub-admin |
| `PUT` | `/sub-admins/{id}` | Edit sub-admin |
| `DELETE` | `/sub-admins/{id}` | Delete sub-admin |
| `PATCH` | `/sub-admins/{id}/toggle-status` | Toggle active status |

#### Menus, Payments, Logs
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/menus` | All menu batches |
| `GET` | `/menus/{batchId}` | Menu batch details |
| `GET` | `/payments` | All payments (pageable) |
| `GET` | `/payments/{id}` | Payment by ID |
| `GET` | `/users/{userId}/subscription/info` | User subscription |
| `GET` | `/logs` | Activity audit logs (pageable) |
| `GET` | `/dashboard/stats` | Platform statistics (filter by date range) |
| `PUT` | `/profile` | Admin updates own profile |

---

## 10. Scheduled (Background) Tasks

NutriFlow runs automated jobs using Spring's `@Scheduled` annotation with cron expressions. These run entirely in the background without any manual trigger.

### Subscription Scheduler

| Trigger | Action |
|---|---|
| **App startup** | Deactivates already-expired subscriptions; sends catch-up warning emails |
| **Daily at 01:00** | Deactivates subscriptions past their `end_date`; sends expiry notification email to user |
| **Daily at 10:00** | Sends warning emails to users with ≤7 days left on subscription |
| **Every Monday at 09:00** | Sends weekly subscription status report to all admins |

### Database Cleanup Scheduler

| Trigger | Action |
|---|---|
| **1st of every month at 03:00** | Deletes delivery records older than 1 year |
| **Daily at 02:00** | Logs database statistics (record counts, etc.) |

### Redis Cleanup Scheduler

| Trigger | Action |
|---|---|
| **Every hour** | Removes expired OTP keys from Redis |
| **Daily at 04:00** | Removes expired refresh token keys from Redis |
| **Every 6 hours** | Logs Redis key statistics |

### Cron Expression Quick Reference

```
0 0 1 * * ?    ← Every day at 01:00
0 0 3 1 * ?    ← 1st of every month at 03:00
0 0 * * * ?    ← Every hour on the hour
0 0 9 * * MON  ← Every Monday at 09:00
```

---

## 11. Email & Notifications

NutriFlow sends transactional emails using Gmail SMTP with Spring Mail. All email sending is **asynchronous** (annotated with `@Async`) so it does not block the HTTP request.

### Emails Sent

| Trigger | Recipient | Content |
|---|---|---|
| Registration | User | OTP verification code |
| Forgot password | User | Password reset OTP |
| Subscription expiry warning | User | "Your subscription expires in X days" |
| Subscription expired | User | "Your subscription has expired — renew now" |
| Weekly report | All Admins | Subscription summary statistics |

---

## 12. Payments with Stripe

### How It Works

1. User calls `POST /api/v1/payments/subscribe`
2. Server creates a **Stripe Checkout Session** and returns the hosted URL
3. User is redirected to `checkout.stripe.com` (handled by frontend)
4. User completes payment on Stripe's secure page
5. Stripe calls `POST /api/v1/payments/webhook` with a signed event
6. Server verifies the Stripe signature (using `stripe.webhook.secret`)
7. On `checkout.session.completed` event: subscription is created (`status: ACTIVE`), user status becomes `ACTIVE`

### Subscription Price

Configured via `nutriflow.subscription.premium-price=150000` (in cents = $1,500.00 or local currency equivalent — adjust per your Stripe currency setting).

### Webhook Security

The webhook endpoint skips CSRF protection and verifies the `Stripe-Signature` header using the webhook secret. This ensures only genuine Stripe calls are processed.

---

## 13. File Uploads

Medical files (PDFs, images, lab reports) are uploaded during health profile submission.

- **Endpoint:** `POST /api/v1/health-profile/submit` (multipart/form-data)
- **Storage path:** `./uploads/medical-files/` (relative to where the app runs)
- **Max file size:** 10 MB per file (configured in `application.properties`)
- **Download:** `GET /api/v1/medical-files/{fileId}/download` — access controlled: only the owner user, their assigned dietitian, or an admin can download

---

## 14. Activity Audit Log

Every significant action on the platform is recorded in the `activity_logs` table via `ActivityLogService`. Logged data includes:

| Field | Example |
|---|---|
| `actorType` | `ADMIN`, `USER`, `DIETITIAN`, `CATERER` |
| `actorId` | `42` |
| `action` | `USER_CREATED`, `STATUS_TOGGLED`, `MENU_APPROVED` |
| `entityType` | `USER`, `MENU`, `SUBSCRIPTION` |
| `entityId` | `7` |
| `oldValue` | Previous state (JSON text) |
| `newValue` | New state (JSON text) |
| `ipAddress` | `192.168.1.10` |

Admins can retrieve paginated logs via `GET /api/v1/admin/logs`.

---

## 15. Configuration Reference

### `application.properties` (shared across all environments)

```properties
spring.application.name=nutriflow
spring.profiles.active=local

# Liquibase — schema versioning
spring.liquibase.enabled=true
spring.liquibase.change-log=classpath:/db/changelog/db.changelog-master.yaml

# JPA — disable auto schema creation (Liquibase handles it)
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true

# Server
server.port=8080
spring.servlet.multipart.max-file-size=10MB
file.upload-dir=./uploads/medical-files

# Subscription price in smallest currency unit (e.g. cents)
nutriflow.subscription.premium-price=150000

# AOP performance thresholds (in milliseconds)
nutriflow.logging.slow-method-threshold=1000
nutriflow.logging.very-slow-method-threshold=5000
```

### `application-local.properties` (local secrets — never commit to git)

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/nutriflow_db
spring.datasource.username=postgres
spring.datasource.password=postgres

# JWT
nutriflow.jwt.secret=<64-character-secret-key>
nutriflow.jwt.expiration=86400000          # 24 hours in ms
nutriflow.jwt.refresh-token.expiration=604800000  # 7 days in ms

# Redis
spring.data.redis.url=redis://localhost:6379

# Gmail SMTP
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your@gmail.com
spring.mail.password=<gmail-app-password>

# Stripe
stripe.api.key=sk_test_...
stripe.webhook.secret=whsec_...

# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=...
spring.security.oauth2.client.registration.google.client-secret=...
spring.security.oauth2.client.registration.google.scope=profile,email
```

---

## 16. Prerequisites

Before running NutriFlow, you need:

| Requirement | Version | Purpose |
|---|---|---|
| Java JDK | 17+ | Run the application |
| Gradle | Bundled via `gradlew` | Build tool (no separate install needed) |
| Docker Desktop | Latest | Run PostgreSQL + Redis containers |
| Git | Any | Clone the repository |
| A Stripe account | Free test account | Payment processing |
| A Google Cloud project | Free | Google OAuth2 login |
| A Gmail account | Free | SMTP email sending |

> **Tip:** You do NOT need to install PostgreSQL or Redis separately if you use Docker.

---

## 17. Running the Project Locally

### Step 1 — Clone the Repository

```bash
git clone <repository-url>
cd nutriflow
```

### Step 2 — Start PostgreSQL and Redis with Docker

```bash
docker-compose up -d
```

This starts:
- PostgreSQL on port `5432` (database: `nutriflow_db`, user: `postgres`, password: `nutriflow555`)
- Redis on port `6379`

### Step 3 — Configure Local Secrets

Create or edit `src/main/resources/application-local.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/nutriflow_db
spring.datasource.username=postgres
spring.datasource.password=nutriflow555

nutriflow.jwt.secret=replace-with-a-64-character-random-string-here-for-security
nutriflow.jwt.expiration=86400000
nutriflow.jwt.refresh-token.expiration=604800000

spring.data.redis.url=redis://localhost:6379

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your@gmail.com
spring.mail.password=your-gmail-app-password

stripe.api.key=sk_test_your_stripe_test_key
stripe.webhook.secret=whsec_your_webhook_secret

spring.security.oauth2.client.registration.google.client-id=your-google-client-id
spring.security.oauth2.client.registration.google.client-secret=your-google-client-secret
spring.security.oauth2.client.registration.google.scope=profile,email
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/google
nutriflow.auth.google-login-url=http://localhost:8080/oauth2/authorization/google
```

### Step 4 — Run the Application

```bash
# On macOS/Linux
./gradlew bootRun

# On Windows
gradlew.bat bootRun
```

The application will start on **http://localhost:8080**

Liquibase will automatically run all database migrations on first start.
`DataInitializer` will seed the default admin, dietitian, and caterer accounts.

### Step 5 — Verify It's Running

```bash
curl http://localhost:8080/v3/api-docs
```

Or open the Swagger UI: http://localhost:8080/swagger-ui/index.html

---

## 18. Docker Setup

### Option A — Only Databases in Docker (App Runs Locally)

```bash
# Start PostgreSQL + Redis
docker-compose up -d

# Run app from your machine
./gradlew bootRun
```

### Option B — Everything in Docker (Including the App)

```bash
# Build the Docker image
docker build -t nutriflow .

# Run with environment variables
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/nutriflow_db \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=nutriflow555 \
  nutriflow
```

### Docker Compose Services

```yaml
db:
  image: postgres:18
  ports: ["5432:5432"]
  environment:
    POSTGRES_DB: nutriflow_db
    POSTGRES_USER: postgres
    POSTGRES_PASSWORD: nutriflow555

redis:
  image: redis:7-alpine
  ports: ["6379:6379"]
```

### Dockerfile Build Process

1. Starts from `eclipse-temurin:17-jdk-alpine` (lightweight Java 17 image)
2. Copies entire project into `/app`
3. Runs `./gradlew bootJar -x test` to build the JAR (skips tests for speed)
4. Exposes port `8080`
5. Runs the application JAR

---

## 19. Default Credentials for Testing

These accounts are automatically created when the application starts for the first time (via `DataInitializer`):

| Role | Email | Password |
|---|---|---|
| **Super Admin** | `admin@nutriflow.com` | `admin123` |
| **Dietitian** | `diet@nutriflow.com` | `diet123` |
| **Caterer** | `caterer@nutriflow.com` | `caterer123` |

Passwords are stored as BCrypt hashes in the database — never in plain text.

> **Important:** Change these credentials in production before deploying.

### Quick Login Test

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@nutriflow.com","password":"admin123"}'
```

Response:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "role": "SUPER_ADMIN",
  "email": "admin@nutriflow.com"
}
```

---

## 20. Swagger / API Documentation UI

NutriFlow includes a built-in, interactive API browser powered by **SpringDoc OpenAPI (Swagger UI)**.

**URL:** http://localhost:8080/swagger-ui/index.html

In Swagger UI you can:
- Browse all endpoints grouped by controller
- See required request body shapes and response schemas
- Click "Try it out" → fill in fields → "Execute" to make real API calls
- Authorize with a JWT token (click the lock icon, paste `Bearer <token>`)

**Raw OpenAPI JSON:** http://localhost:8080/v3/api-docs

---

## 21. Error Handling

All errors are returned as consistent JSON via `GlobalExceptionHandler`:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "OTP has expired. Please request a new one.",
  "timestamp": "2026-02-22T14:30:00"
}
```

### HTTP Status Codes Used

| Code | Meaning | When |
|---|---|---|
| `200 OK` | Success | Request processed successfully |
| `201 Created` | Created | New resource created |
| `400 Bad Request` | Validation error | Invalid OTP, bad input |
| `401 Unauthorized` | Auth failed | Invalid or expired token |
| `403 Forbidden` | Access denied | Wrong role for endpoint |
| `404 Not Found` | Missing resource | User/menu/file not found |
| `409 Conflict` | Duplicate | Email already exists |
| `500 Internal Server Error` | Server fault | Unexpected error |

### Custom Exceptions

| Exception | HTTP Code | Description |
|---|---|---|
| `InvalidOtpException` | 400 | OTP is wrong or expired |
| `InvalidTokenException` | 401 | JWT is invalid or expired |
| `EmailAlreadyExistsException` | 409 | Duplicate email registration |
| `UserNotFoundException` | 404 | User not found by ID or email |
| `MenuNotFoundException` | 404 | Menu record not found |
| `HealthProfileNotFoundException` | 404 | Health profile not found |
| `SubscriptionNotFoundException` | 404 | Subscription not found |
| `FileStorageException` | 500 | File save/delete failed |
| `FileAccessDeniedException` | 403 | User not allowed to access file |
| `InvalidMenuStatusException` | 400 | Illegal menu state transition |
| `ResourceNotFoundException` | 404 | Generic resource missing |
| `ResourceAlreadyExistsException` | 409 | Generic duplicate resource |
| `ResourceNotAvailableException` | 400 | Resource exists but not usable |
| `BusinessException` | 400 | General business rule violation |
| `WebhookProcessingException` | 400 | Stripe webhook processing failed |

---

## 22. Testing Scheduled Tasks Manually

NutriFlow exposes public endpoints for manually triggering scheduled tasks during development. Do NOT expose these in production.

**Base path:** `/api/admin/scheduler-test`

| Method | Endpoint | Triggers |
|---|---|---|
| `POST` | `/cleanup-db` | Database cleanup job |
| `POST` | `/deactivate-subscriptions` | Subscription deactivation |
| `POST` | `/send-warnings` | Expiry warning emails |
| `POST` | `/redis-stats` | Log Redis key stats |
| `POST` | `/redis-cleanup` | Clean expired Redis keys |
| `POST` | `/weekly-report` | Send admin weekly report |
| `POST` | `/create-test-data` | Seed test data |
| `DELETE` | `/delete-test-data` | Remove test data |

Example:
```bash
curl -X POST http://localhost:8080/api/admin/scheduler-test/weekly-report
```

You can also use the PowerShell script included in the project:
```powershell
.\test-schedulers-onetime.ps1
```

---

## Troubleshooting

### App fails to start — "Connection refused" for database
Make sure Docker containers are running:
```bash
docker-compose ps
docker-compose up -d
```

### "JWT secret key must be at least 256 bits"
Your `nutriflow.jwt.secret` is too short. Use a 64-character random string.

### Emails not sending
Make sure you're using a **Gmail App Password** (not your Google account password). Enable 2FA on your Google account first, then generate an App Password at https://myaccount.google.com/apppasswords

### Stripe webhook not working locally
Use the Stripe CLI to forward webhooks to localhost:
```bash
stripe listen --forward-to localhost:8080/api/v1/payments/webhook
```
Copy the displayed `whsec_...` secret into your `application-local.properties`.

### Port 5432 already in use
Another PostgreSQL instance is running on your machine. Either stop it or change the port in `docker-compose.yml`.

---

## License

This project is proprietary software. All rights reserved.
