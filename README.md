# A LifeTime Project

Basic Maven + Spring Boot application with:

- Static HTML/CSS/JavaScript in `src/main/resources/static`
- JSON REST APIs under `/api`
- Login and register controller under `/api/auth`

## Run

```powershell
.\mvn-local.ps1 spring-boot:run
```

Then open:

```text
http://localhost:8080
```

If Maven is installed globally later, this also works:

```powershell
mvn spring-boot:run
```

## API

Register:

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "name": "Demo User",
  "email": "demo@example.com",
  "password": "password123"
}
```

Login:

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "demo@example.com",
  "password": "password123"
}
```
