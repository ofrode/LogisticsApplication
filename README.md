# Logistics Application

REST API для управления перевозками и заявками в сфере логистики.
Приложение позволяет создавать, просматривать, обновлять и удалять перевозки,
а также фильтровать их по статусу.

## Стек

- Java 21
- Spring Boot 4.0.3
- Spring Web MVC
- Spring Data JPA
- H2 Database (in-memory)
- Maven
- Checkstyle

## Структура проекта

```text
src/main/java/com/logisticsapplication
├── config
│   └── ShipmentDataInitializer.java
├── controller
│   ├── HealthController.java
│   └── ShipmentController.java
├── dto
│   ├── request/ShipmentRequest.java
│   └── response/ShipmentResponse.java
├── mapper
│   └── ShipmentMapper.java
├── model
│   └── Shipment.java
├── repository
│   └── ShipmentRepository.java
├── service
│   ├── ShipmentService.java
│   └── impl/ShipmentServiceImpl.java
└── LogisticsapplicationApplication.java
```

## Запуск

```bash
./mvnw spring-boot:run
```

Приложение стартует на:
- `http://localhost:8080`

Проверочный endpoint:
- `GET http://localhost:8080/api/health`

## API

Базовый путь:
- `/api/shipments`

### Create shipment

`POST /api/shipments`

Пример body:

```json
{
  "cargoName": "Electronics",
  "originCity": "Minsk",
  "destinationCity": "Warsaw",
  "pickupDate": "2026-03-04",
  "weightKg": 1250.5,
  "status": "NEW"
}
```

### Get shipment by id

`GET /api/shipments/{id}`

Пример:
- `GET /api/shipments/1`

### Get all shipments / filter by status

`GET /api/shipments`

Опциональный параметр:
- `status`

Примеры:
- `GET /api/shipments`
- `GET /api/shipments?status=NEW`

### Update shipment

`PUT /api/shipments/{id}`

### Delete shipment

`DELETE /api/shipments/{id}`

## Данные по умолчанию

При старте приложения автоматически добавляются тестовые записи (`ShipmentDataInitializer`), поэтому `GET` запросы сразу возвращают данные.

## H2 Console

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:logistics_db`
- User: `sa`
- Password: *(пусто)*

## Checkstyle

Проверка стиля:

```bash
./mvnw checkstyle:check
```

Полная проверка проекта:

```bash
./mvnw verify
```

Конфиги:
- `config/checkstyle.xml`
- `config/checkstyle-suppressions.xml`

## SonarCloud
```
https://sonarcloud.io/summary/overall?id=ofrode_LogisticsApplication&branch=main
```