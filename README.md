# Logistics Application

REST API для управления логистическими заказами. Проект построен на `Spring Boot`, `Spring Data JPA` и поддерживает `H2` и `PostgreSQL`, покрывая требования по CRUD, связям между сущностями, транзакциям и демонстрации проблемы `N+1`.

## Стек

- `Java 21`
- `Spring Boot 4.0.3`
- `Spring Web MVC`
- `Spring Data JPA`
- `H2 Database`
- `PostgreSQL`
- `Maven`
- `Checkstyle`

## Модель данных

В проекте реализовано 5 сущностей:

- `AppUser` - пользователь системы с ролями `ADMIN`, `MANAGER`, `CUSTOMER`, `CARRIER`
- `Shipment` - заказ на перевозку
- `Cargo` - единица груза с названием и весом
- `ShipmentSchedule` - даты заказа: создание, прием, прибытие
- `Vehicle` - транспорт перевозчика

Связи:

- `Shipment -> Cargo` : `OneToMany`
- `Shipment <-> Vehicle` : `ManyToMany`
- `AppUser -> Shipment` : `ManyToOne` для заказчика и менеджера
- `Vehicle -> AppUser` : `ManyToOne` для закрепленного перевозчика
- `Shipment -> ShipmentSchedule` : `OneToOne`

## ER-диаграмма

```mermaid
erDiagram
    APP_USERS ||--o{ SHIPMENTS : customer_id
    APP_USERS ||--o{ SHIPMENTS : manager_id
    APP_USERS ||--o{ VEHICLES : carrier_id
    SHIPMENTS ||--o{ CARGOES : shipment_id
    SHIPMENTS ||--|| SHIPMENT_SCHEDULES : shipment_id
    SHIPMENTS }o--o{ VEHICLES : shipment_vehicle

    APP_USERS {
        bigint id PK
        string full_name
        string email UK
        string role
    }

    SHIPMENTS {
        bigint id PK
        string tracking_number UK
        string origin_city
        string destination_city
        string status
        bigint customer_id FK
        bigint manager_id FK
    }

    CARGOES {
        bigint id PK
        string name
        decimal weight_kg
        bigint shipment_id FK
    }

    SHIPMENT_SCHEDULES {
        bigint id PK
        datetime order_created_at
        datetime order_received_at
        datetime arrival_at
        bigint shipment_id FK UK
    }

    VEHICLES {
        bigint id PK
        string registration_number UK
        decimal capacity_kg
        bigint carrier_id FK
    }

    SHIPMENT_VEHICLE {
        bigint shipment_id FK
        bigint vehicle_id FK
    }
```

## Почему так настроены `CascadeType` и `FetchType`

- Для `Shipment.cargoes` и `Shipment.schedule` используется `CascadeType.ALL`, потому что это составные части заказа. Они создаются, обновляются и удаляются вместе с `Shipment`.
- Для `Shipment.vehicles`, `Shipment.customer`, `Shipment.manager` каскад не используется, потому что транспорт и пользователи живут независимо от заказа.
- Для коллекций и ассоциаций выставлен `FetchType.LAZY`, чтобы не тянуть лишние данные автоматически и иметь возможность показать проблему `N+1`.

## CRUD API

Основные endpoints:

- `GET/POST/PUT/DELETE /api/users`
- `GET/POST/PUT/DELETE /api/vehicles`
- `GET/POST/PUT/DELETE /api/shipments`

Пример создания заказа:

```json
{
  "trackingNumber": "SHIP-2001",
  "originCity": "Minsk",
  "destinationCity": "Berlin",
  "status": "CREATED",
  "customerId": 3,
  "managerId": 2,
  "vehicleIds": [1, 2],
  "cargoes": [
    {
      "name": "Industrial Equipment",
      "weightKg": 1800.50
    },
    {
      "name": "Boxes",
      "weightKg": 220.00
    }
  ],
  "schedule": {
    "orderCreatedAt": "2026-03-10T09:00:00",
    "orderReceivedAt": "2026-03-10T12:00:00",
    "arrivalAt": "2026-03-12T18:00:00"
  }
}
```

## N+1 и решение

Для демонстрации добавлен один и тот же endpoint с двумя режимами:

- `GET /api/shipments?optimized=false` - обычная загрузка, которая провоцирует `N+1`
- `GET /api/shipments?optimized=true` - загрузка через `@EntityGraph`, которая забирает связанные данные сразу

SQL виден в логах, потому что включен `spring.jpa.show-sql=true`.

## Транзакции

Есть два специальных endpoint:

- `POST /api/shipments/demo/partial-save` - намеренно падает без `@Transactional`, поэтому часть данных остается в БД
- `POST /api/shipments/demo/rollback` - намеренно падает с `@Transactional`, поэтому вся операция откатывается

Для этих сценариев добавлен интеграционный тест.

## База данных

По умолчанию проект стартует с профилем `h2`:

- JDBC URL: `jdbc:h2:mem:logistics_db`
- Console: `http://localhost:8080/h2-console`
- User: `sa`
- Password: пустой

Для PostgreSQL добавлен профиль `postgres` в [application-postgres.properties](/home/ofrode/vs%20code/Java/logisticsapplication/src/main/resources/application-postgres.properties):

- JDBC URL: `jdbc:postgresql://localhost:5432/logistics_db`
- User: `logistics_user`
- Password: `secret`

Перед запуском с PostgreSQL создайте базу и пользователя:

```sql
CREATE DATABASE logistics_db;
CREATE USER logistics_user WITH PASSWORD 'secret';
GRANT ALL PRIVILEGES ON DATABASE logistics_db TO logistics_user;
```

## Проверка проекта

Checkstyle запускается на фазе `validate`, поэтому при ошибках стиля сборка не проходит:

```bash
mvn validate
```

Запуск тестов:

```bash
mvn test
```

Запуск приложения с H2:

```bash
mvn spring-boot:run
```

Запуск приложения с PostgreSQL:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```
