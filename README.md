# Ktor Task API

Учебный backend-проект на Kotlin и Ktor. Приложение предоставляет CRUD API для задач, регистрацию пользователей, безопасное хэширование паролей BCrypt и авторизацию защищённых операций через JWT.

Данные хранятся в памяти и сбрасываются при перезапуске приложения. Репозитории используют `ConcurrentHashMap` и `AtomicLong`, поэтому хранение и генерация ID потокобезопасны.

## Технологии

- Kotlin 2.2.21, JVM 21;
- Ktor 3.6.0 и Netty;
- Gradle Kotlin DSL;
- kotlinx.serialization и JSON ContentNegotiation;
- Ktor Authentication + JWT (`com.auth0:java-jwt`);
- BCrypt (`org.mindrot:jbcrypt`);
- StatusPages;
- Ktor `testApplication`.

## Запуск

Требуется JDK 21. Устанавливать Gradle отдельно не нужно — Gradle Wrapper включён в проект.

```bash
./gradlew run
```

Сервер будет доступен по адресу `http://localhost:8080`.

Для production-подобного запуска задайте собственный секрет (достаточно длинную случайную строку):

```bash
JWT_SECRET="replace-with-a-long-random-secret" ./gradlew run
```

Без переменной `JWT_SECRET` используется локальное учебное значение из `application.conf`. Порт можно переопределить переменной `PORT`.

## API и статусы

| Метод и путь | Доступ | Успех | Основные ошибки |
|---|---|---:|---:|
| `POST /api/v1/auth/register` | публичный | 201 | 400, 409 |
| `POST /api/v1/auth/login` | публичный | 200 | 400, 401 |
| `GET /api/v1/tasks` | публичный | 200 | — |
| `GET /api/v1/tasks/{id}` | публичный | 200 | 400, 404 |
| `POST /api/v1/tasks` | Bearer JWT | 201 | 400, 401 |
| `PUT /api/v1/tasks/{id}` | Bearer JWT | 200 | 400, 401, 404 |
| `DELETE /api/v1/tasks/{id}` | Bearer JWT | 204 | 400, 401, 404 |

Ошибки возвращаются как JSON, например `{"message":"Task not found"}`. Имя пользователя не должно быть пустым, пароль должен содержать минимум 8 символов, а заголовок задачи не должен быть пустым.

## Примеры curl

### 1. Регистрация

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password123"
  }'
```

Ответ: `201 Created` и пользователь без пароля и хэша:

```json
{
  "id": 1,
  "username": "admin"
}
```

Повторное имя пользователя возвращает `409 Conflict`.

### 2. Вход

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password123"
  }'
```

Скопируйте `token` из JSON-ответа и сохраните его:

```bash
TOKEN="eyJ..."
```

JWT действует 1 час, содержит claims `userId` и `username`, а также стандартные `iss`, `aud` и `exp`. Сервер проверяет подпись, issuer, audience, срок действия и наличие существующего пользователя из `userId`.

### 3. Получение списка задач

```bash
curl -i http://localhost:8080/api/v1/tasks
```

Этот запрос публичный и возвращает `200 OK` с JSON-массивом.

### 4. Получение задачи по ID

```bash
curl -i http://localhost:8080/api/v1/tasks/1
```

Возвращает `200 OK`, `404 Not Found` для отсутствующей задачи или `400 Bad Request` для некорректного ID.

### 5. Создание задачи

```bash
curl -i -X POST http://localhost:8080/api/v1/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Learn Ktor",
    "description": "Complete authentication homework",
    "completed": false
  }'
```

Возвращает `201 Created` и созданную задачу с ID, сгенерированным сервером.

### 6. Обновление задачи

```bash
curl -i -X PUT http://localhost:8080/api/v1/tasks/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Learn Ktor and JWT",
    "description": "Homework completed",
    "completed": true
  }'
```

Возвращает `200 OK` и обновлённую задачу.

### 7. Удаление задачи

```bash
curl -i -X DELETE http://localhost:8080/api/v1/tasks/1 \
  -H "Authorization: Bearer $TOKEN"
```

Успешный ответ — `204 No Content` с пустым телом.

## Демонстрация защиты

Запрос создания без заголовка `Authorization`:

```bash
curl -i -X POST http://localhost:8080/api/v1/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test",
    "description": "Should fail",
    "completed": false
  }'
```

Ответ — `401 Unauthorized`. Аналогично защищены `PUT` и `DELETE`. Неверный, повреждённый или просроченный JWT также приводит к `401 Unauthorized`.

## Тесты и сборка

```bash
./gradlew clean test
./gradlew build
```

Интеграционные тесты через `testApplication` покрывают:

1. регистрацию;
2. запрет повторного username;
3. успешный login и выдачу JWT;
4. неправильный пароль и неизвестного пользователя;
5. публичный список задач без JWT;
6. `POST` без JWT (`401`);
7. `POST` с JWT (`201`);
8. получение созданной задачи;
9. обновление с JWT;
10. удаление с JWT (`204`, пустое тело);
11. `404` после удаления;
12. неправильный JWT (`401`);
13. некорректные ID, JSON и данные (`400`);
14. `PUT` и `DELETE` без JWT (`401`).

## Структура

```text
src/main/kotlin/com/example/
├── Application.kt
├── config/       # JSON, JWT Authentication, StatusPages
├── dto/          # DTO запросов и ответов
├── model/        # Task и внутренний User
├── repository/   # потокобезопасное in-memory хранение
├── routes/       # auth и task HTTP-маршруты
└── service/      # валидация, BCrypt, JWT и бизнес-логика
```

В `User` и `UserRepository` хранится только `passwordHash`. Исходный пароль существует лишь во входном DTO на время обработки запроса, хэшируется через `BCrypt.hashpw`, а при входе проверяется через `BCrypt.checkpw`. Хэш никогда не включается в HTTP-ответ.
