# Mir Exchange

Сервис для проверки курса снятия наличных в банкоматах ВТБ с карты МИР на территории Казахстана.

## Endpoints

- `/calc` - запрашивает расчёт стоимости. Пример запроса `/calc?amount=10000000&chatId=345678&currency=tenge`.
  - currency - необязательный параметр, указывает на то в какой валюте приходит запрос. По умолчанию tenge.
  Может быть rub.
  - amount - сумма для расчёта.
  - chatId - id чата в который надо отправить расчёт.
- `/resend` - повторно рассылает всем подписанным пользователям сообщение об изменение курса.
- `/users/add` - подписывает пользователя на рассылку информации об изменение курса. Пример запроса `/users/add?chatId=345678`.
- `/users/dell` - отписывает пользователя от рассылки информации об изменение курса. Пример запроса `/users/dell?chatId=345678`.
