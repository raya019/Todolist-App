# Todo List Application

This repository contains the source code for a Todo List application with the following features:

- **User Authentication**
- **JWT (JSON Web Token) for secure access**
- **Refresh Tokens for extended sessions**
- **Password Change functionality**
- **CRUD (Create, Read, Update, Delete) operations for Todo List**

## Features

### 1. **Authentication**
- Users can register, log in, and access their account with JWT tokens for authentication and refresh tokens for session renewal.
- The system uses `bearerAuth` (JWT in headers) and `refreshToken` (stored in cookies).

### 2. **JWT and Refresh Tokens**
- Access tokens (JWT) are issued to authenticate user requests.
- Refresh tokens allow users to renew their session without re-authentication.

### 3. **Change Password**
- Users can update their password securely via the `/user/current` endpoint.

### 4. **Todo List CRUD**
- Users can create, view, update, and delete their Todo items. Each user has their own Todo List stored in the database.

## API Documentation

The API specification is available in [TodolistSpecAPI.json](./TodolistSpecAPI.json) and can be accessed through an OpenAPI/Swagger viewer to explore and test the available endpoints. Below is an overview of key API endpoints: