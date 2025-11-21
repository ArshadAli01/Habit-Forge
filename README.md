# 📖 Habit Tracker API - README

## 🎯 Project Overview

**Habit Tracker** is a Spring Boot REST API application that enables users to register, authenticate, and manage their daily habits with full CRUD operations, pagination, filtering, and ownership-based access control.

**Current Version:** 1.0.0  
**Status:** Production Ready  
**Java Version:** 21  
**Build Tool:** Maven

---

## ✨ Features

### User Module
- User registration with email and password
- Password complexity validation (8+ chars, 1 uppercase, 1 digit)
- BCrypt password encryption
- User profile retrieval
- Email normalization (case-insensitive)
- HTTP Basic Authentication

### Habit Module
- Create, read, update, and delete habits
- Maximum 50 active habits per user
- Unique habit names per user
- Soft delete functionality (data retention)
- Pagination support (default 20, max 100 items per page)
- Filtering by active status and frequency
- Sorting by multiple fields (name, created_at, updated_at, frequency)
- User ownership validation on all operations
- Audit trail (created/updated timestamps and actors)

### Security
- HTTP Basic Authentication
- SQL injection protection via parameterized queries
- Input sanitization and validation
- User ownership enforcement
- Stateless API design

---

## 🏗️ Architecture

### Technology Stack
- **Framework:** Spring Boot 3.2.2
- **Language:** Java 21
- **Database:** PostgreSQL 15+
- **Build Tool:** Maven
- **Authentication:** Spring Security with HTTP Basic Auth
- **ORM:** JDBC Template (manual SQL management)
- **Validation:** Jakarta Bean Validation
- **Logging:** SLF4J with Logback

### Module Structure

```
habit-tracker-parent/
├── common/          # Shared utilities, base classes, exceptions
├── user/            # User management and authentication
├── habit/           # Habit CRUD operations
└── server/          # Main application entry point
```

### Layered Architecture

Each module follows this pattern:

```
module/
├── api/              # Interfaces and contracts
│   ├── dao/          # Data access interfaces
│   ├── dto/          # Request/Response DTOs
│   ├── entity/       # Domain entities (POJOs)
│   ├── enums/        # Enumerations
│   ├── exception/    # Module-specific exceptions
│   └── service/      # Service interfaces
├── core/             # Implementation
│   ├── config/       # Spring configurations
│   ├── constants/    # Module constants
│   ├── converter/    # DTO ↔ Entity converters
│   ├── dao/          # DAO implementations
│   └── service/      # Service implementations
└── rest/             # REST API
    └── controllers/  # HTTP endpoints
```

---

## 🚀 Getting Started

### Prerequisites

- Java 21 or higher
- PostgreSQL 15 or higher
- Maven 3.8.1 or higher
- Git

### Installation

#### Step 1: Clone the Repository
```bash
git clone <repository-url>
cd habit-tracker-parent
```

#### Step 2: Configure Database

Create a PostgreSQL database:
```sql
CREATE DATABASE habit_tracker;
```

Update `application.properties` in the server module:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/habit_tracker
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# Flyway Migration
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# Logging
logging.level.root=INFO
logging.level.com.habittracker=DEBUG
logging.level.org.springframework.security=INFO
```

#### Step 3: Build the Project
```bash
mvn clean install
```

#### Step 4: Run the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

---

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### Authentication

All endpoints except user registration require HTTP Basic Authentication.

**Format:**
```
Authorization: Basic base64(email:password)
```

**Example:**
```bash
curl -X GET http://localhost:8080/api/v1/users/me \
  -H "Authorization: Basic am9obkBleGFtcGxlLmNvbTpQYXNzd29yZDEyMw=="
```

---

## 👤 User Endpoints

### Register User
```
POST /users/register
```

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "Password123"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "createdAt": "2025-11-15T10:30:00Z"
}
```

**Error Responses:**
- `409 Conflict` - Email already exists
- `422 Unprocessable Entity` - Invalid input (password, email, name validation)

---

### Get Current User Profile
```
GET /users/me
```

**Authentication:** Required (HTTP Basic Auth)

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "createdAt": "2025-11-15T10:30:00Z"
}
```

**Error Responses:**
- `401 Unauthorized` - Missing or invalid credentials
- `404 Not Found` - User not found

---

## 🎯 Habit Endpoints

### Create Habit
```
POST /habits
```

**Authentication:** Required

**Request Body:**
```json
{
  "name": "Morning Exercise",
  "description": "30 minutes of cardio",
  "frequency": "DAILY"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "name": "Morning Exercise",
  "description": "30 minutes of cardio",
  "frequency": "DAILY",
  "isActive": true,
  "createdAt": "2025-11-15T10:40:00Z"
}
```

**Error Responses:**
- `400 Bad Request` - Maximum 50 active habits exceeded
- `409 Conflict` - Habit name already exists for user
- `422 Unprocessable Entity` - Invalid input
- `401 Unauthorized` - Not authenticated

---

### Get All Habits
```
GET /habits?page=0&size=20&isActive=true&frequency=DAILY&sortBy=name&sortDirection=ASC
```

**Authentication:** Required

**Query Parameters (all optional):**
- `page` (default: 0) - Page number
- `size` (default: 20, max: 100) - Items per page
- `isActive` (true/false) - Filter by active status
- `frequency` (DAILY, WEEKLY, MONTHLY) - Filter by frequency
- `sortBy` (name, created_at, updated_at, frequency, default: created_at) - Sort field
- `sortDirection` (ASC, DESC, default: DESC) - Sort direction

**Response (200 OK):**
```json
{
  "habits": [
    {
      "id": 1,
      "name": "Morning Exercise",
      "description": "30 minutes of cardio",
      "frequency": "DAILY",
      "isActive": true,
      "createdAt": "2025-11-15T10:40:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "hasNext": false,
  "hasPrevious": false
}
```

---

### Get Habit by ID
```
GET /habits/{id}
```

**Authentication:** Required

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Morning Exercise",
  "description": "30 minutes of cardio",
  "frequency": "DAILY",
  "isActive": true,
  "createdAt": "2025-11-15T10:40:00Z"
}
```

**Error Responses:**
- `404 Not Found` - Habit not found or user is not owner
- `401 Unauthorized` - Not authenticated

---

### Update Habit
```
PUT /habits/{id}
```

**Authentication:** Required

**Request Body (all fields optional):**
```json
{
  "name": "Morning Workout",
  "description": "45 minutes of cardio and strength",
  "frequency": "WEEKLY"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Morning Workout",
  "description": "45 minutes of cardio and strength",
  "frequency": "WEEKLY",
  "isActive": true,
  "createdAt": "2025-11-15T10:40:00Z"
}
```

**Error Responses:**
- `404 Not Found` - Habit not found or unauthorized
- `409 Conflict` - New name already exists for user
- `422 Unprocessable Entity` - Invalid input
- `401 Unauthorized` - Not authenticated

---

### Delete Habit (Soft Delete)
```
DELETE /habits/{id}
```

**Authentication:** Required

**Response (204 No Content)**

**Error Responses:**
- `400 Bad Request` - Habit already deleted
- `404 Not Found` - Habit not found or unauthorized
- `401 Unauthorized` - Not authenticated

---

## 🔌 Habit Frequencies

Valid frequency values:
- `DAILY` - Every day
- `WEEKLY` - Every week
- `MONTHLY` - Every month

---

## 📊 Error Response Format

All errors follow a consistent format:

```json
{
  "timestamp": "2025-11-15T10:50:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Habit not found: 123",
  "path": "/api/v1/habits/123"
}
```

**Common HTTP Status Codes:**
- `200 OK` - Request successful
- `201 Created` - Resource created successfully
- `204 No Content` - Request successful, no content to return
- `400 Bad Request` - Business logic violation
- `401 Unauthorized` - Authentication required or invalid
- `404 Not Found` - Resource not found
- `409 Conflict` - Duplicate resource or constraint violation
- `422 Unprocessable Entity` - Validation failed
- `500 Internal Server Error` - Unexpected error

---

## 🔐 Security & Validation

### Password Requirements
- Minimum 8 characters
- At least 1 uppercase letter (A-Z)
- At least 1 digit (0-9)

**Examples:**
- ✅ `Password123`
- ❌ `password123` (no uppercase)
- ❌ `Password` (no digit)
- ❌ `Pass1` (too short)

### Email Validation
- Must be valid email format
- Case-insensitive (stored as lowercase)
- Must be unique across all users

### Habit Name Validation
- Minimum 3 characters
- Maximum 100 characters
- Must be unique per user
- Trimmed of leading/trailing whitespace

### Habit Description Validation
- Optional field
- Maximum 500 characters

### Rate Limits
- Maximum 50 active habits per user
- Soft-deleted habits don't count toward limit

### SQL Injection Protection
All database queries use parameterized statements. Dynamic sort fields are whitelisted.

---

## 🗄️ Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(50) DEFAULT 'system',
    updated_by VARCHAR(50) DEFAULT 'system'
);
```

### Habits Table
```sql
CREATE TABLE habits (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    frequency VARCHAR(10) NOT NULL CHECK (frequency IN ('DAILY', 'WEEKLY', 'MONTHLY')),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(50) DEFAULT 'system',
    updated_by VARCHAR(50) DEFAULT 'system',
    CONSTRAINT unique_user_habit_name UNIQUE (user_id, name)
);
```

**Indexes:**
- `idx_users_email` on `users(email)`
- `idx_habits_user_id` on `habits(user_id)`
- `idx_habits_user_active` on `habits(user_id, is_active)` where `is_active = TRUE`

---

## 🧪 Testing

### Prerequisites for Testing
- Application running on `http://localhost:8080`
- Postman or similar REST client

### Test Coverage
- 50+ comprehensive test cases
- User registration and authentication
- Habit CRUD operations
- Pagination and filtering
- Security and ownership validation
- Edge cases and boundary tests
- Input validation
- Error handling

### Running Tests

**User Module Tests:**
1. Register user
2. Get user profile
3. Test invalid credentials

**Habit Module Tests:**
1. Create habits
2. List habits with filters
3. Update habits
4. Delete habits
5. Test ownership validation

See `TESTING.md` for complete test cases documentation.

---

## 📝 Project Structure

```
habit-tracker-parent/
├── pom.xml                          # Parent POM
├── README.md                        # This file
│
├── common/
│   ├── pom.xml
│   └── src/main/java/com/habittracker/common/
│       ├── api/
│       │   ├── BaseEntity.java
│       │   └── ErrorResponse.java
│       ├── exception/
│       │   ├── BusinessException.java
│       │   ├── DuplicateResourceException.java
│       │   ├── EntityNotFoundException.java
│       │   └── GlobalExceptionHandler.java
│       └── utility/
│           └── AuditFieldUtility.java
│
├── user/
│   ├── pom.xml
│   └── src/main/java/com/habittracker/user/
│       ├── api/
│       │   ├── dao/UserDao.java
│       │   ├── dto/request/CreateUserRequest.java
│       │   ├── dto/response/UserResponse.java
│       │   ├── entity/User.java
│       │   ├── exception/UserNotFoundException.java
│       │   ├── service/UserService.java
│       │   └── validation/PasswordValidator.java
│       ├── core/
│       │   ├── config/SecurityConfig.java
│       │   ├── constants/UserConstants.java
│       │   ├── converter/UserConverter.java
│       │   ├── dao/UserDaoImpl.java
│       │   ├── security/
│       │   │   ├── CustomUserDetails.java
│       │   │   └── CustomUserDetailsService.java
│       │   └── service/UserServiceImpl.java
│       └── rest/controllers/UserController.java
│
├── habit/
│   ├── pom.xml
│   └── src/main/java/com/habittracker/habit/
│       ├── api/
│       │   ├── dao/HabitDao.java
│       │   ├── dto/request/CreateHabitRequest.java
│       │   ├── dto/request/UpdateHabitRequest.java
│       │   ├── dto/response/HabitResponse.java
│       │   ├── dto/response/PagedHabitResponse.java
│       │   ├── entity/Habit.java
│       │   ├── enums/Frequency.java
│       │   ├── exception/HabitNotFoundException.java
│       │   └── service/HabitService.java
│       ├── core/
│       │   ├── constants/HabitConstants.java
│       │   ├── converter/HabitConverter.java
│       │   ├── dao/HabitDaoImpl.java
│       │   └── service/HabitServiceImpl.java
│       └── rest/controllers/HabitController.java
│
└── server/
    ├── pom.xml
    ├── src/main/java/com/habittracker/server/
    │   └── HabitTrackerApplication.java
    └── src/main/resources/
        ├── application.properties
        └── db/migration/
            ├── V1__create_users_table.sql
            └── V2__create_habits_table.sql
```

---

## 🛠️ Configuration

### application.properties

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/habit_tracker
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# Connection Pool
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5

# Flyway Migration
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# Logging
logging.level.root=INFO
logging.level.com.habittracker=DEBUG
logging.level.org.springframework.security=INFO
logging.level.org.springframework.web=INFO

# Jackson Configuration
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.default-property-inclusion=non_null

# Application Name
spring.application.name=habit-tracker
```

---

## 📦 Dependencies

Key dependencies:
- `spring-boot-starter-web` - REST API support
- `spring-boot-starter-jdbc` - Database access
- `spring-boot-starter-security` - Authentication/Authorization
- `spring-boot-starter-validation` - Input validation
- `postgresql` - PostgreSQL driver
- `lombok` - Code generation
- `jackson-databind` - JSON processing
- `flyway` - Database migrations

---

## 🚨 Troubleshooting

### Issue: Database Connection Failed
**Solution:**
- Verify PostgreSQL is running
- Check credentials in `application.properties`
- Ensure database exists: `CREATE DATABASE habit_tracker;`

### Issue: 401 Unauthorized on All Requests
**Solution:**
- Verify user is registered
- Check credentials are correct
- Ensure `CustomUserDetailsService` bean is created
- Check Spring Security is configured

### Issue: 409 Conflict on Habit Creation
**Solution:**
- Habit name must be unique per user
- Check if habit with same name already exists
- Soft-deleted habits can have same name as new habits

### Issue: Pagination Returns Empty Results
**Solution:**
- Verify habits exist for authenticated user
- Check `page` parameter is not too high
- Use `GET /habits` without filters to list all

### Issue: Flyway Migration Fails
**Solution:**
- Delete existing migration files if corrupted
- Ensure migration files are in `src/main/resources/db/migration/`
- Check migration SQL syntax
- Review `flyway_schema_history` table

---

## 📋 Development Guidelines

### Code Style
- Follow Spring Boot conventions
- Use meaningful variable names
- Add logging for debugging
- Write comprehensive Javadoc for public APIs

### Naming Conventions
- Classes: PascalCase (e.g., `UserService`)
- Methods: camelCase (e.g., `getCurrentUser`)
- Constants: UPPER_SNAKE_CASE (e.g., `MAX_ACTIVE_HABITS`)
- Database columns: snake_case (e.g., `created_at`)

### Exception Handling
- Throw specific exceptions (BusinessException, EntityNotFoundException, etc.)
- Let GlobalExceptionHandler convert to HTTP responses
- Never expose sensitive error details

### Logging
- Use `@Slf4j` annotation from Lombok
- Log at appropriate levels: INFO, DEBUG, WARN, ERROR
- Include relevant context (IDs, emails) in messages

### Transactions
- Use `@Transactional` at service layer
- Mark read-only operations with `readOnly = true`
- Let Spring handle rollbacks on exceptions

---

## 🔄 Future Enhancements

- JWT authentication (replace HTTP Basic Auth)
- Refresh tokens
- Password reset via email
- Email verification on registration
- Role-based access control (Admin, User)
- Habit tracking (log daily completions)
- Habit statistics (streaks, completion rates)
- User profile updates
- Soft delete for users
- API documentation (Swagger/OpenAPI)
- Performance monitoring
- Docker containerization

---

## 📄 License

This project is proprietary and confidential.

---

## 👥 Support

For issues or questions:
1. Check troubleshooting section above
2. Review test cases for usage examples
3. Check application logs with DEBUG level
4. Verify database schema and data integrity

---

## ✅ Verification Checklist

After installation, verify:
- [x] Application starts without errors
- [x] Database migrations run successfully
- [x] User registration works (`POST /users/register`)
- [x] User authentication works (`GET /users/me`)
- [x] Habit CRUD operations work
- [x] Pagination and filtering work
- [x] Error responses are consistent
- [x] Ownership validation works
- [x] All test cases pass

---

## 📊 API Summary

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| POST | `/users/register` | No | Register new user |
| GET | `/users/me` | Yes | Get current user profile |
| POST | `/habits` | Yes | Create habit |
| GET | `/habits` | Yes | List habits (paginated) |
| GET | `/habits/{id}` | Yes | Get habit by ID |
| PUT | `/habits/{id}` | Yes | Update habit |
| DELETE | `/habits/{id}` | Yes | Delete habit |

---

**Version:** 1.0.0  
**Last Updated:** November 2025  
**Status:** Production Ready
