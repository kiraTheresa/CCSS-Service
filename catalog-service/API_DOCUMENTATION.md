# Catalog Service API Documentation

## Overview
The Catalog Service provides RESTful APIs for managing course information in the Course Catalog System. It allows clients to create, read, update, and delete courses, as well as search and filter courses based on various criteria.

## Base URL
`http://localhost:8081/api/courses`

## Authentication
Currently, no authentication is required for accessing the Catalog Service APIs.

## Error Handling
All API responses follow a consistent format:

```json
{
  "code": <HTTP status code>,
  "message": <description of the result>,
  "data": <response payload>
}
```

Common error codes:
- 400: Bad Request - Invalid input or parameters
- 404: Not Found - Resource not found
- 500: Internal Server Error - Unexpected server error

## API Endpoints

### 1. Get All Courses
- **Method**: GET
- **URL**: `/api/courses`
- **Description**: Retrieve a list of all courses in the catalog
- **Response**: 
  - 200 OK: List of courses
  - 500 Internal Server Error: Server error

**Example Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "code": "CS101",
      "title": "计算机科学导论",
      "instructorId": "INS001",
      "scheduleId": "SCH001",
      "capacity": 50,
      "createdAt": "2024-01-01T12:00:00",
      "enrolled": 0,
      "description": "计算机科学入门课程",
      "credits": 3,
      "location": "教室101"
    }
  ]
}
```

### 2. Get Course by ID
- **Method**: GET
- **URL**: `/api/courses/{id}`
- **Description**: Retrieve a specific course by its UUID
- **Path Parameters**: 
  - `id`: UUID of the course to retrieve
- **Response**: 
  - 200 OK: Course details
  - 404 Not Found: Course not found
  - 500 Internal Server Error: Server error

**Example Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "code": "CS101",
    "title": "计算机科学导论",
    "instructorId": "INS001",
    "scheduleId": "SCH001",
    "capacity": 50,
    "createdAt": "2024-01-01T12:00:00",
    "enrolled": 0,
    "description": "计算机科学入门课程",
    "credits": 3,
    "location": "教室101"
  }
}
```

### 3. Get Course by Code
- **Method**: GET
- **URL**: `/api/courses/code/{code}`
- **Description**: Retrieve a course by its unique code
- **Path Parameters**: 
  - `code`: Code of the course (e.g., "CS101")
- **Response**: 
  - 200 OK: Course details
  - 404 Not Found: Course not found
  - 500 Internal Server Error: Server error

### 4. Create Course
- **Method**: POST
- **URL**: `/api/courses`
- **Description**: Create a new course
- **Request Body**: Course object
- **Response**: 
  - 201 Created: Created course details
  - 400 Bad Request: Invalid input
  - 500 Internal Server Error: Server error

**Example Request**:
```json
{
  "code": "CS201",
  "title": "数据结构与算法",
  "instructorId": "INS001",
  "scheduleId": "SCH002",
  "capacity": 60,
  "description": "数据结构与算法课程",
  "credits": 4,
  "location": "教室201"
}
```

**Example Response**:
```json
{
  "code": 201,
  "message": "课程创建成功",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "code": "CS201",
    "title": "数据结构与算法",
    "instructorId": "INS001",
    "scheduleId": "SCH002",
    "capacity": 60,
    "createdAt": "2024-01-01T12:05:00",
    "enrolled": 0,
    "description": "数据结构与算法课程",
    "credits": 4,
    "location": "教室201"
  }
}
```

### 5. Update Course
- **Method**: PUT
- **URL**: `/api/courses/{id}`
- **Description**: Update an existing course
- **Path Parameters**: 
  - `id`: UUID of the course to update
- **Request Body**: Updated course object
- **Response**: 
  - 200 OK: Updated course details
  - 400 Bad Request: Invalid input
  - 404 Not Found: Course not found
  - 500 Internal Server Error: Server error

### 6. Delete Course
- **Method**: DELETE
- **URL**: `/api/courses/{id}`
- **Description**: Delete a course by its UUID
- **Path Parameters**: 
  - `id`: UUID of the course to delete
- **Response**: 
  - 204 No Content: Course deleted successfully
  - 404 Not Found: Course not found
  - 500 Internal Server Error: Server error

### 7. Get Courses by Instructor
- **Method**: GET
- **URL**: `/api/courses/instructor/{instructorId}`
- **Description**: Retrieve all courses taught by a specific instructor
- **Path Parameters**: 
  - `instructorId`: ID of the instructor
- **Response**: 
  - 200 OK: List of courses
  - 400 Bad Request: Invalid instructor ID
  - 500 Internal Server Error: Server error

### 8. Search Courses by Title
- **Method**: GET
- **URL**: `/api/courses/search?keyword={keyword}`
- **Description**: Search for courses by title keyword
- **Query Parameters**: 
  - `keyword`: Search term for course titles
- **Response**: 
  - 200 OK: List of matching courses
  - 400 Bad Request: Invalid search keyword
  - 500 Internal Server Error: Server error

## Data Models

### Course
| Field | Type | Description | Constraints |
|-------|------|-------------|-------------|
| id | UUID | Unique identifier | Auto-generated |
| code | String | Course code (e.g., "CS101") | Required, unique, max 20 chars |
| title | String | Course title | Required, max 100 chars |
| instructorId | String | Instructor's ID | Required |
| scheduleId | String | Schedule slot ID | Required |
| capacity | Integer | Maximum enrollment capacity | Required, 1-500 |
| createdAt | LocalDateTime | Creation timestamp | Auto-generated |
| enrolled | Integer | Current number of enrolled students | Default: 0 |
| description | String | Course description | Optional, max 1000 chars |
| credits | Integer | Course credits | Optional, 0-10 |
| location | String | Course location | Optional, max 200 chars |

## Technologies Used
- Spring Boot 3.x
- Spring Data JPA
- Hibernate
- PostgreSQL (or other JPA-compatible database)
- Maven

## Running the Service
```bash
# Build the project
mvn clean package

# Run the service
java -jar target/catalog-service-1.0.0.jar
```

The service will start on port 8080 by default. You can change this in the `application.properties` or `application.yml` file.
