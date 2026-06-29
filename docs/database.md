# Database Design

## Module 1: User Management

### Table: users

| Column        | Data Type    | Description               |
| ------------- | ------------ | ------------------------- |
| id            | BIGINT       | Primary Key               |
| full_name     | VARCHAR(100) | User's Full Name          |
| email         | VARCHAR(150) | Unique Email Address      |
| password      | VARCHAR(255) | Encrypted Password        |
| role          | ENUM         | USER / ADMIN              |
| profile_image | VARCHAR(500) | Profile Image URL         |
| is_verified   | BOOLEAN      | Email Verification Status |
| created_at    | TIMESTAMP    | Account Creation Time     |
| updated_at    | TIMESTAMP    | Last Update Time          |

### Notes

* Email must be unique.
* Password will be encrypted using BCrypt.
* Profile image will be stored in Cloudinary.
* Every new user will have the USER role by default.
* ADMIN accounts will be created manually.
