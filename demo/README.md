# Notepad Backend API

A complete Spring Boot backend for a notepad application with RESTful API endpoints.

## Features

- ✅ Complete CRUD operations for notepads
- ✅ Search functionality (by title, content, or both)
- ✅ Archive/Unarchive notepads
- ✅ Statistics and health check endpoints
- ✅ H2 in-memory database for development
- ✅ Global exception handling
- ✅ Sample data initialization
- ✅ Cross-origin support for frontend integration

## Technology Stack

- **Spring Boot 3.5.3**
- **Spring Data JPA**
- **H2 Database** (in-memory)
- **Maven**
- **Java 17**

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Running the Application

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Notepad/demo
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - API Base URL: `http://localhost:8080/api/notepads`
   - H2 Console: `http://localhost:8080/h2-console`
   - Health Check: `http://localhost:8080/api/notepads/health`

## API Endpoints

### Base URL: `http://localhost:8080/api/notepads`

#### 📋 Get All Notepads
```http
GET /api/notepads
```

#### 📋 Get Active Notepads
```http
GET /api/notepads/active
```

#### 📋 Get Archived Notepads
```http
GET /api/notepads/archived
```

#### 📋 Get Notepad by ID
```http
GET /api/notepads/{id}
```

#### ➕ Create New Notepad
```http
POST /api/notepads
Content-Type: application/json

{
  "title": "My New Note",
  "content": "This is the content of my note"
}
```

#### ✏️ Update Notepad
```http
PUT /api/notepads/{id}
Content-Type: application/json

{
  "title": "Updated Title",
  "content": "Updated content"
}
```

#### 📦 Toggle Archive Status
```http
PATCH /api/notepads/{id}/toggle-archive
```

#### 🗑️ Delete Notepad
```http
DELETE /api/notepads/{id}
```

#### 🔍 Search Notepads
```http
GET /api/notepads/search?q=search_term
```

#### 🔍 Search by Title
```http
GET /api/notepads/search/title?title=title_term
```

#### 🔍 Search by Content
```http
GET /api/notepads/search/content?content=content_term
```

#### 📊 Get Statistics
```http
GET /api/notepads/stats
```

#### ❤️ Health Check
```http
GET /api/notepads/health
```

## Database Schema

### Notepad Entity
```sql
CREATE TABLE notepads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE
);
```

## Sample Data

The application automatically creates sample notepads on startup:
- Welcome Note
- Shopping List
- Meeting Notes
- Ideas (archived)
- Quick Reminder

## Development

### Database Access
- **H2 Console**: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:notepaddb`
- **Username**: `sa`
- **Password**: `password`

### Logging
The application logs SQL queries and HTTP requests for debugging. Check the console output for detailed logs.

### Testing the API

You can test the API using curl or any REST client:

```bash
# Get all notepads
curl http://localhost:8080/api/notepads

# Create a new notepad
curl -X POST http://localhost:8080/api/notepads \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Note","content":"This is a test note"}'

# Get statistics
curl http://localhost:8080/api/notepads/stats
```

## Project Structure

```
src/main/java/com/example/demo/
├── DemoApplication.java          # Main application class
├── entity/
│   └── Notepad.java            # JPA entity
├── repository/
│   └── NotepadRepository.java   # Data access layer
├── service/
│   └── NotepadService.java      # Business logic layer
├── controller/
│   └── NotepadController.java   # REST API endpoints
├── dto/
│   └── NotepadDto.java         # Data transfer objects
├── exception/
│   └── GlobalExceptionHandler.java # Error handling
└── config/
    └── DataInitializer.java     # Sample data initialization
```

## Configuration

Key configuration in `application.properties`:
- H2 in-memory database
- JPA with automatic schema generation
- Detailed SQL logging
- H2 console enabled
- Actuator endpoints for monitoring

## Next Steps

1. **Add Authentication**: Implement user authentication and authorization
2. **Add Categories**: Organize notepads by categories or tags
3. **Add File Attachments**: Support for file uploads
4. **Add Sharing**: Share notepads between users
5. **Add Real Database**: Switch to PostgreSQL or MySQL for production
6. **Add Caching**: Implement Redis for better performance
7. **Add Tests**: Comprehensive unit and integration tests

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License. 