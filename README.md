# Третья контрольная курса Базы Данных в CSC

## Решение

Студент: Анастасия Шадрина

#### Найденные ошибки

1) Фикс для `/flights`. Коммит: [тык](https://github.com/shadrina/test2/commit/6a03ee16c690e4f7df266e4b5d2e4bc3939f6bda). В исходной версии хэндлера было много лишних запросов: селект всех айдишников из таблицы `Flights`, затем для каждого из этих айдишников запрос на поиск по ключу в `FlightEntityView`, который строится достаточно долго из-за наличия джойна с `FlightAvailableSeatsView`. Все эти запросы избыточны, так как в результате дают много лишней информации, которая в выводе не используется.
2) Фикс для `/delay_flights`. Коммит: [тык](https://github.com/shadrina/test2/commit/1aa0621bc57db18327b9a03859a35928b33f57bd). Во-первых, здесь из-за прямой вставки `flightId` в запрос появляется вероятность sql-инъекций, во-вторых, опять же ненужное обращение к `cacheFlights`.
3) Фикс для `/delete_planet`. Коммит: [тык](https://github.com/shadrina/test2/commit/31c460d5f67a8d9ae8e6bf340b180479183fe152). Здесь запрос исполнялся с выключенным режимом `auto-commit`. => Либо мы дописываем после исполнения запроса `it.commit()`, либо включаем этот режим (я выбрала вариант 2).
4) Еще для `/delay_flights` можно сделать индекс на `Flight.date`.

### SQL
по умолчанию пользователь postgres с пустым паролем на localhost:5432/postgres

```
psql -h localhost -U postgres -f sql/gen-schema.sql
psql -h localhost -U postgres -f sql/gen-data.sql
-- Аргумент функции GenerateData -- коэффициент роста БД. Он пропорционально увеличивает количество полетов и бронирований. Значения 10 будет более чем достаточно для смелых экспериментов.
psql -h localhost -U postgres -c 'SELECT GenerateSchema(); SELECT GenerateData(1)'
```

### Python
```
cd python/hello-db
python3 app.py
```

### Kotlin
```
cd kotlin/hello-db
gradle run
```

Если нужно поменять хост, порт, пользователя, базу данных или пароль, воспользуйтесь аргументами командной строки. `python3 app.py -h` и `gradle run --args='-h'` вам про них расскажут.

### Как сдавать
Пул-реквест в репозиторий github.com/dbarashev/test2 или письмо с заархивированным кодом на `dmitry+csc@barashev.net`. Пожалуйста, не присылайте в архиве каталоги `build`, `.gradle` и `.idea` 
