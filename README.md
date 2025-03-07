# Облачное хранилище

REST-сервис для управления файлами.

## Описание
Сервис предоставляет REST-интерфейс для загрузки файлов и вывода списка уже загруженных файлов пользователя.

Все запросы к сервису требуют авторизации.

Сервис реализует следующие функции:
* Вывод списка файлов
* Добавление файла
* Удаление файла
* Авторизация

Информация о пользователях сервиса (логины для авторизации) и данные хранятся в базе данных.

# Используемые технологии
* Приложение разработано с использованием Spring Boot
* Использован сборщик пакетов `maven`
* Для запуска можно использовать `docker`

## Конечные точки

### /login
#### POST

_Пример запроса_
```declarative
$ curl -d'{"login":"test","password":"test"}' \
       -H 'Content-Type:application/json' \
       http://localhost:8080/login
```
_Ответ_
```declarative
{
  "auth-token": "eyJhbiJIUzJ9.eyJzdWIiONDU2NTB9.ayn9_J-RlXuRCb-6mRoWW-JfNaFgTg"
}
```

### /logot
#### POST

_Пример запроса_
```declarative
$ curl -v -X POST -H "Auth-Token:${AUTH_TOKEN}" 'http://localhost:8080/logout'
```

### /file
#### POST

_Пример запроса_
```declarative
$ curl -v -H"auth-token:${AUTH_TOKEN}" \
     -F'filename=Рингтон.mp3' \
     -Ffile=@over_the_horizon_ringtone.mp3 \
     http://localhost:8080/file
```

#### DELETE
_Пример запроса_
```declarative
$ curl -v -XDELETE -H"Auth-Token:${AUTH_TOKEN}" \
     -G --data-urlencode 'filename=Рингтон.mp3' \
     'http://localhost:8080/file'
```

#### GET
_Пример запроса_
```declarative
$ curl -v -XGET -H"auth-token:${AUTH_TOKEN}" \
     -G --data-urlencode 'filename=Рингтон.mp3' \
     'http://localhost:8080/file' > ~/Documents/Рингтон.mp3
```

### PUT
_Пример запроса_
```declarative
$ curl -v -XPUT -d'{"filename":"Rington.mp3"}' \
     -H"Auth-Token:${AUTH_TOKEN}" \
     -H'Content-Type: application/json' \
     'http://localhost:8080/file?filename=Lorem%20Ipsum.txt'
```
  
### /list

### GET
_Пример запроса_
```declarative
$ curl -v -H"Auth-Token:${AUTH_TOKEN}" 'http://localhost:8080/list?limit=2'
```
_Ответ_
```declarative
[
  {
    "id": 1,
    "contentType": "text/plain",
    "createdAt": 1740180577120,
    "editedAt": 1740180577120,
    "size": 653,
    "filename": "Lorem Ipsum.txt"
  },
  {
    "id": 4,
    "contentType": "image/jpeg",
    "createdAt": 1740612577120,
    "editedAt": 1740612577120,
    "size": 220603,
    "filename": "Облако.jpg"
  }
]
```

# Демонстрационный экземпляр

## Конечная точка демонстрационного API
  https://fileadmin-i4rimw5qwq-de.a.run.app

  _(по умолчанию порт 80)_
  
## Демонстрационное клиентское приложение (веб)
  https://fileadmin-web-i4rimw5qwq-de.a.run.app

  _(по умолчанию порт 80)_

### Демо-пользователи (логин/пароль)
  - `test/test`
  - `user2@mail.edu/234`

# Docker

## Dockerfile
  https://github.com/StudenikinNikolay/-O-o-x-/blob/main/Dockerfile

Аргументы сборки:  

* Порт прослушивания
```declarative
ARG SERVER_PORT=8080
```

  
## Docker compose
https://github.com/StudenikinNikolay/-O-o-x-/blob/main/compose.yml
