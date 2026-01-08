# Convo - Real-Time Chat Application

A modern, full-stack real-time chat application built with **Angular** and **Spring Boot**, featuring WebSocket communication, JWT authentication, and a unique procedural text generation feature called "The Oracle."

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green)
![Angular](https://img.shields.io/badge/Angular-20-red)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![OpenAPI](https://img.shields.io/badge/OpenAPI-3.0-green)

## Features

### Core Functionality
- **Real-time Messaging** - Instant message delivery using WebSocket/STOMP protocol
- **JWT Authentication** - Secure token-based login and registration
- **Multiple Chat Rooms** - Create, join, and manage conversation channels
- **Typing Indicators** - See when other users are typing in real-time
- **User Presence** - Track online/offline/away/busy status
- **Message History** - Paginated message loading with search
- **Read Receipts** - Track message read status

### The Room Oracle
Each chat room has its own "Oracle" - a procedural text generator that creates cryptic responses using only words from the room's conversation history. Built with N-gram Markov chains, it demonstrates algorithmic text generation without external AI dependencies. [Learn more](#the-room-oracle-1)

## Technology Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Runtime |
| Spring Boot | 3.5.7 | Application framework |
| Spring Security | - | JWT authentication |
| Spring WebSocket | - | STOMP real-time messaging |
| Spring Data JPA | - | Data persistence |
| MySQL | 8.0 | Database |
| Flyway | - | Database migrations |
| Lombok | - | Boilerplate reduction |
| springdoc-openapi | 2.8 | OpenAPI 3 / Swagger UI |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| Angular | 20 | Web framework |
| Angular Material | 20 | UI components |
| RxJS | 7.8 | Reactive programming |
| STOMP.js | 7.2 | WebSocket protocol |
| TypeScript | 5.9 | Type-safe JavaScript |

### DevOps
- **Docker** & **Docker Compose** - Containerization
- **Maven** - Build automation

## Project Structure

```
convo/
├── backend/
│   └── src/main/java/com/jameselner/convo/
│       ├── config/          # Security, WebSocket configuration
│       ├── controller/      # REST endpoints
│       ├── dto/             # Data transfer objects
│       ├── model/           # JPA entities (User, ChatRoom, Message)
│       ├── repository/      # Data access layer
│       ├── security/        # JWT utilities, filters, interceptors
│       ├── service/         # Business logic (Chat, Auth, Oracle)
│       └── websocket/       # WebSocket handlers
│
└── frontend/src/app/
    ├── core/
    │   ├── guards/          # Route protection
    │   ├── interceptors/    # JWT injection
    │   ├── models/          # TypeScript interfaces
    │   └── services/        # HTTP & WebSocket services
    └── features/
        ├── auth/            # Login, registration
        └── chat/            # Chat UI components
```

## Getting Started

### Prerequisites
- JDK 21+
- Node.js 18+ and npm
- Maven 3.8+
- MySQL 8.0+ (or use Docker)
- Angular CLI (`npm install -g @angular/cli`)

### Option 1: Docker (Recommended)

```bash
# Start MySQL and backend
docker-compose up --build

# In a separate terminal, start frontend
cd frontend
npm install
ng serve
```

Access the application at `http://localhost:4200`

### Option 2: Manual Setup

**Backend:**
```bash
cd backend

# Configure database in application.properties
# spring.datasource.url=jdbc:mysql://localhost:3306/convo
# spring.datasource.username=your_username
# spring.datasource.password=your_password

mvn spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
ng serve
```

- Backend: `http://localhost:8080`
- Frontend: `http://localhost:4200`

## API Reference

### Interactive Documentation

Once the backend is running, access the interactive API documentation:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

Swagger UI allows you to explore endpoints, view request/response schemas, and test the API directly from your browser.

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login, returns JWT |

### Chat
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/chat/rooms` | List all public rooms |
| GET | `/api/chat/room/{id}` | Get room details |
| POST | `/api/chat/room` | Create new room |
| PUT | `/api/chat/room/{id}` | Update room |
| DELETE | `/api/chat/room/{id}` | Delete room |
| GET | `/api/chat/room/{id}/messages` | Get messages (paginated) |
| GET | `/api/chat/room/{id}/search?keyword=` | Search messages |
| POST | `/api/chat/room/{id}/oracle/ask?order=2` | Invoke the Oracle |

### User
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/me` | Get current user |
| PUT | `/api/users/status` | Update presence status |

### WebSocket
| Endpoint | Direction | Description |
|----------|-----------|-------------|
| `/ws-chat` | Connect | WebSocket handshake |
| `/app/chat/{roomId}` | Send | Send message |
| `/app/typing/{roomId}` | Send | Typing indicator |
| `/topic/room/{roomId}` | Subscribe | Receive messages |
| `/topic/typing/{roomId}` | Subscribe | Receive typing indicators |

## The Room Oracle

A unique feature that gives each chat room its own procedural "voice."

### Concept
The Oracle generates cryptic, surreal responses assembled entirely from words used in the room's conversation history. No external APIs, no LLMs - just a Markov chain trained on your words.

### How It Works

1. **Tokenization** - Extracts words and punctuation from recent messages (up to 300)
2. **N-gram Model** - Builds probabilistic state transitions from token sequences
3. **Generation** - Walks the chain until hitting terminal punctuation or max length
4. **Formatting** - Applies capitalization and spacing rules

### Configuration

The `order` parameter controls coherence vs. creativity:
- `order=1` (unigram): Most chaotic, pure word soup
- `order=2` (bigram): Balanced surrealism (default)
- `order=3` (trigram): More coherent, closer to original phrasing

### Example

```
Alice: Should we deploy tonight or tomorrow?
Bob: I'm slightly worried about the database migration.
Carol: YOLO deploy, what could go wrong

[Ask the Oracle]

Oracle: "YOLO migration tomorrow, slightly deploy what could be."
        Forged from 47 messages (312 unique words, order-2 chain)
```

### Response Format
```json
{
  "id": 123,
  "content": "Deploy what could be slightly worried.",
  "type": "ORACLE",
  "senderUsername": "Oracle",
  "timestamp": "2024-01-15T10:30:00",
  "oracleMetadata": {
    "messagesAnalyzed": 47,
    "uniqueTokens": 312,
    "chainOrder": 2
  }
}
```

## Testing

```bash
# Backend unit tests
cd backend
mvn test

# Frontend tests
cd frontend
ng test
```

## Security

- **Password Hashing** - BCrypt encryption
- **JWT Tokens** - Stateless authentication with configurable expiration
- **WebSocket Auth** - JWT validation on STOMP connections via channel interceptor
- **CORS** - Configured for frontend origin
- **SQL Injection** - Prevented via JPA parameterized queries

## Architecture Highlights

- **Service Layer Pattern** - Business logic decoupled from controllers
- **DTO Pattern** - Clean separation between entities and API contracts
- **Reactive State** - RxJS BehaviorSubjects for frontend state management
- **Standalone Components** - Modern Angular architecture without NgModules
- **Database Migrations** - Flyway for version-controlled schema changes

## License

This project is licensed under the MIT License.

## Author

James Elner - [GitHub](https://github.com/JamesRepo)

---

**Built with Spring Boot and Angular**
