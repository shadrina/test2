# Третья контрольная курса Базы Данных в CSC

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
