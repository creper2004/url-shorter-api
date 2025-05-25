

---

# 🔗 URL Shortener API

## 📌 Описание

**URL Shortener API** — это RESTful-сервис для генерации коротких ссылок.
Система использует Redis для ограничения количества одновременно генерируемых ссылок: **не более 100 на одного пользователя одновременно**.

---

## 📄 Техническое задание

Техническое задание доступно по ссылке:
[📄 Ссылка на ТЗ (Google Drive)](https://drive.google.com/file/d/1tFCZoRT3dd3WaPiIbdDpEedCTLIVyYFW/view?usp=sharing)

---

## 🚀 Быстрый старт

### 📥 Клонирование репозитория

```bash
git clone https://github.com/creper2004/url-shorter-api/
cd url-shorter-api
```

### 🧪 Сборка проекта

```bash
./mvnw clean package -DskipTests
```

### 🐳 Запуск с помощью Docker Compose

```bash
docker-compose up --build
```

### 🔧 Что запускается

* **3 инстанса приложения**:

  * `http://localhost:8080`
  * `http://localhost:8082`
  * `http://localhost:8084`
* **PostgreSQL** — на стандартном порту `5432`
* **Redis** — на стандартном порту `6379`

---

## 📚 Документация API

OpenAPI (Swagger) спецификация доступна в корне проекта:

```
SwaggerDoc.yaml
```

Вы можете использовать Swagger UI или [Swagger Editor](https://editor.swagger.io/) для визуализации и тестирования.

---

## 🧪 Тестирование

В корне проекта находится скрипт для массового тестирования API:

```bash
python3 test120Req.py
```

Он отправляет 120 запросов для проверки лимитов и поведения системы при превышении порога в 100 ссылок на пользователя.

---

## 🛠️ Используемые технологии

* **Java + Spring Boot**
* **Redis** — кэш и контроль ограничений
* **PostgreSQL** — основная база данных
* **Docker + Docker Compose** — контейнеризация и оркестрация
* **OpenAPI (Swagger)** — описание REST-интерфейсов

---


