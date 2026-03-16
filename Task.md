# Выполненные задания

## Что было добавлено

В проект был добавлен новый расширенный поиск для `Shipment`, который умеет:

- фильтровать заказы по вложенным сущностям;
- работать в двух вариантах: через `JPQL` и через `native query`;
- возвращать данные постранично через `Pageable`;
- кешировать результаты прошлых запросов в памяти;
- очищать кеш при изменении данных.

---

## 1. Сложный GET-запрос через `@Query` и `JPQL`

Файл: [ShipmentRepository.java](/home/ofrode/vs%20code/Java/logisticsapplication/src/main/java/com/logisticsapplication/repository/ShipmentRepository.java)

Добавлен метод:

- `searchIdsJpql(...)`

Что он делает:

- ищет `Shipment` по email клиента;
- ищет по названию груза;
- фильтрует по дате прибытия из `ShipmentSchedule`;
- использует связи между сущностями, а не имена таблиц.

Почему это считается сложным запросом:

- фильтрация идет не только по полям `Shipment`;
- используются вложенные сущности `customer`, `cargoes`, `schedule`;
- применяется `distinct`, потому что у одного заказа может быть несколько грузов, и без этого один и тот же заказ мог бы прийти несколько раз.

Проще говоря:
мы можем искать заказы не только по самому заказу, а и по данным клиента, груза и расписания.

---

## 2. Аналогичный запрос через `native query`

Файл: [ShipmentRepository.java](/home/ofrode/vs%20code/Java/logisticsapplication/src/main/java/com/logisticsapplication/repository/ShipmentRepository.java)

Добавлен метод:

- `searchIdsNative(...)`

Что он делает:

- выполняет ту же самую логику поиска;
- но уже на чистом SQL;
- работает напрямую с таблицами `shipments`, `app_users`, `cargoes`, `shipment_schedules`.

Для чего это полезно:

- чтобы показать понимание разницы между JPQL и native SQL;
- чтобы иметь вариант, когда нужен полный контроль над SQL;
- это часто спрашивают на учебных и рабочих задачах.

---

## 3. Пагинация через `Pageable`

Файлы:

- [ShipmentController.java](/home/ofrode/vs%20code/Java/logisticsapplication/src/main/java/com/logisticsapplication/controller/ShipmentController.java)
- [ShipmentService.java](/home/ofrode/vs%20code/Java/logisticsapplication/src/main/java/com/logisticsapplication/service/ShipmentService.java)
- [ShipmentServiceImpl.java](/home/ofrode/vs%20code/Java/logisticsapplication/src/main/java/com/logisticsapplication/service/impl/ShipmentServiceImpl.java)
- [PageResponse.java](/home/ofrode/vs%20code/Java/logisticsapplication/src/main/java/com/logisticsapplication/dto/response/PageResponse.java)

Добавлен endpoint:

- `GET /api/shipments/search`

Он принимает:

- `customerEmail`
- `cargoName`
- `arrivalFrom`
- `arrivalTo`
- `queryType=JPQL` или `queryType=NATIVE`
- стандартные параметры пагинации Spring: `page`, `size`, `sort`

Что возвращается:

- список заказов текущей страницы;
- номер страницы;
- размер страницы;
- общее число элементов;
- общее число страниц;
- флаг `fromCache`;
- тип запроса `queryType`.

Пример:

```http
GET /api/shipments/search?customerEmail=customer@test.local&cargoName=Paper&queryType=JPQL&page=0&size=5
```

---

## 4. In-memory индекс на `HashMap<K, V>`

Файлы:

- [ShipmentSearchCacheKey.java](/home/ofrode/vs%20code/Java/logisticsapplication/src/main/java/com/logisticsapplication/cache/ShipmentSearchCacheKey.java)
- [ShipmentSearchIndex.java](/home/ofrode/vs%20code/Java/logisticsapplication/src/main/java/com/logisticsapplication/cache/ShipmentSearchIndex.java)

Что сделано:

- создан индекс в памяти на `HashMap`;
- ключом является составной объект `ShipmentSearchCacheKey`;
- значением является готовый ответ поиска `PageResponse<ShipmentResponse>`.

Какие поля входят в ключ:

- email клиента;
- название груза;
- диапазон дат прибытия;
- тип запроса (`JPQL` или `NATIVE`);
- номер страницы;
- размер страницы;
- сортировка.

Почему нужен составной ключ:

если пользователь ищет:

- те же данные, но с другой страницей;
- те же данные, но через `native`;
- те же данные, но с другим названием груза;

это уже разные запросы, и кеш должен различать их правильно.

---

## 5. `equals()` и `hashCode()`

Файл: [ShipmentSearchCacheKey.java](/home/ofrode/vs%20code/Java/logisticsapplication/src/main/java/com/logisticsapplication/cache/ShipmentSearchCacheKey.java)

Здесь вручную реализованы:

- `equals()`
- `hashCode()`

Для чего это нужно:

`HashMap` определяет, одинаковые ключи или нет, именно через эти методы.

Если бы они были сделаны неправильно:

- кеш мог бы не находить уже сохраненный результат;
- или наоборот мог бы вернуть результат от другого запроса;
- индекс стал бы работать неправильно.

Проще говоря:
эти методы говорят Java, когда два поисковых запроса считать одинаковыми.

---

## 6. Инвалидация индекса при изменении данных

Файлы:

- [ShipmentServiceImpl.java](/home/ofrode/vs%20code/Java/logisticsapplication/src/main/java/com/logisticsapplication/service/impl/ShipmentServiceImpl.java)
- [AppUserServiceImpl.java](/home/ofrode/vs%20code/Java/logisticsapplication/src/main/java/com/logisticsapplication/service/impl/AppUserServiceImpl.java)
- [VehicleServiceImpl.java](/home/ofrode/vs%20code/Java/logisticsapplication/src/main/java/com/logisticsapplication/service/impl/VehicleServiceImpl.java)

Что сделано:

- после `create`, `update`, `delete` выполняется `shipmentSearchIndex.invalidateAll()`;
- кеш также очищается при изменении пользователей и транспорта;
- в демо-методе частичного сохранения кеш тоже очищается перед выбросом ошибки.

Почему это важно:

если не очищать кеш после изменения данных, то поиск мог бы показывать старую информацию.

Пример:

- ты искал заказы с грузом `Paper`;
- результат закешировался;
- потом груз изменили на `Furniture`;
- без очистки индекса старый поиск все равно показывал бы `Paper`, хотя в базе уже другое значение.

---

## 7. Как работает новый поиск внутри

Файл: [ShipmentServiceImpl.java](/home/ofrode/vs%20code/Java/logisticsapplication/src/main/java/com/logisticsapplication/service/impl/ShipmentServiceImpl.java)

Логика такая:

1. Приходит запрос в контроллер.
2. Сервис собирает составной ключ кеша.
3. Смотрит, есть ли уже такой результат в `HashMap`.
4. Если есть, возвращает его с `fromCache=true`.
5. Если нет, делает запрос в БД.
6. Сначала получает страницу `id` заказов.
7. Потом отдельным запросом загружает уже полные данные по этим `id`.
8. Сохраняет результат в индекс.
9. Возвращает ответ клиенту.

Почему сначала ищутся `id`, а потом грузятся сами заказы:

- так удобнее сочетать сложную фильтрацию и пагинацию;
- это помогает не ломать структуру ответа;
- после этого можно спокойно загрузить все нужные связи у найденных заказов.

---

## 8. Что было добавлено в тесты

Файл: [ShipmentTransactionIntegrationTest.java](/home/ofrode/vs%20code/Java/logisticsapplication/src/test/java/com/logisticsapplication/ShipmentTransactionIntegrationTest.java)

Добавлены проверки:

- что поиск через `JPQL` работает;
- что пагинация работает;
- что повторный одинаковый запрос идет из кеша;
- что после изменения `Shipment` кеш очищается;
- что `native query` тоже работает корректно.

---

## 9. Что теперь можно показать преподавателю или в отчете

Теперь в проекте есть:

- сложный `GET` с фильтрацией по вложенным сущностям;
- `JPQL` запрос;
- `native query`;
- пагинация;
- in-memory индекс на `HashMap`;
- составной ключ с `equals()` и `hashCode()`;
- инвалидация кеша при изменении данных;
- тесты на этот функционал.

---

## 10. Как использовать

Пример JPQL-запроса:

```http
GET /api/shipments/search?customerEmail=customer@test.local&cargoName=Paper&queryType=JPQL&page=0&size=5
```

Пример native-запроса:

```http
GET /api/shipments/search?customerEmail=customer@test.local&cargoName=Paper&queryType=NATIVE&page=0&size=5
```

Пример с датой:

```http
GET /api/shipments/search?arrivalTo=2026-03-20T18:00:00&queryType=JPQL&page=0&size=10
```

---

## Итог простыми словами

Ты добавил в проект не просто еще один endpoint, а полноценный механизм поиска:

- умный запрос в базу;
- две реализации одного и того же поиска;
- удобную разбивку по страницам;
- ускорение повторных запросов через кеш в памяти;
- защиту от устаревших данных через очистку кеша.

Это уже выглядит как функциональность ближе к реальному production-подходу, а не просто учебный CRUD.
