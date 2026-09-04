# LendFlow Lite

Digital lending & repayment platform. Хэрэглэгч зээл хүсэх, автомат credit scoring, admin review, disbursement, сар бүрийн repayment schedule, гүйлгээний бүрэн түүхийг агуулсан full-stack backend.

## Tech Stack

**Backend:** Java 21, Spring Boot, Spring Data JPA, Spring Security + JWT, PostgreSQL, Maven, Lombok
**Frontend:** React, Vite, Tailwind CSS
**Infra:** Docker, Docker Compose

## Боломжууд

- JWT authentication (register/login), нууц үг BCrypt hash
- Rule-based credit scoring (орлого, ажилласан хугацаа дээр үндэслэсэн)
- Автомат зөвшөөрөл (score ≥ 70) эсвэл admin review
- Disbursement — `@Transactional`, Account balance/Loan status/Transaction record нэг дор
- EMI томьёогоор тооцоолсон сар бүрийн repayment schedule автоматаар үүсдэг
- Repayment — давхар төлөлт, хүрэлцэхгүй үлдэгдлээс хамгаалалттай
- Global Exception Handler — цэвэрхэн JSON алдааны хариу
- Bean Validation бүх endpoint дээр
- 18 unit test (JUnit 5 + Mockito)

## Architecture

React Frontend
│
REST API
│
Spring Boot Backend
│
┌───┴────┬─────────────┐
│ │ │
Loan Credit Assess Repayment
Service Service Service
│ │ │
└────────┴─────────────┘
│
PostgreSQL


## Database Schema

User ──1:1── Account
User ──1:N── Loan ──1:N── Repayment
──1:N── Transaction


## API Endpoints

| Method | Endpoint | Тайлбар |
|---|---|---|
| POST | `/api/auth/register` | Бүртгүүлэх |
| POST | `/api/auth/login` | Нэвтрэх |
| POST | `/api/loans` | Зээл хүсэх (auth шаардана) |
| GET | `/api/loans` | Өөрийн зээлүүд |
| GET | `/api/loans/{id}` | Зээлийн дэлгэрэнгүй |
| POST | `/api/loans/{id}/disburse` | Мөнгө олгох |
| GET | `/api/loans/{id}/repayments` | Сар бүрийн хуваарь |
| POST | `/api/repayments/{id}/pay` | Төлбөр хийх |
| GET | `/api/transactions` | Гүйлгээний түүх |
| GET | `/api/admin/loans` | Шалгах хүлээгдэж буй зээлүүд |
| PATCH | `/api/admin/loans/{id}/approve` | Зөвшөөрөх |
| PATCH | `/api/admin/loans/{id}/reject` | Татгалзах |

## Business Rules

- Credit score: base 50 + income bonus (up to +20) + employment bonus (up to +15)
- Score ≥ 70 → автомат APPROVED, эс бол admin review (UNDER_REVIEW)
- Зээлийн дүн сарын орлогын 10 дахинаас ихгүй байх ёстой
- Disbursement, repayment хоёул `@Transactional` — алдаа гарвал бүх өөрчлөлт rollback

## Ажиллуулах

### Docker-оор (санал болгож буй арга)
```bash
docker compose up --build
```
Backend `http://localhost:8080`, PostgreSQL container дотор автоматаар асна.

### Локал (Docker-гүйгээр)
```bash
# Database
psql postgres -c "CREATE DATABASE lendflow;"
psql postgres -c "CREATE USER lendflow_user WITH PASSWORD 'l******3';"
psql postgres -c "GRANT ALL PRIVILEGES ON DATABASE lendflow TO lendflow_user;"

# Backend
./mvnw spring-boot:run

# Frontend
cd ../lendflow-frontend
npm install && npm run dev
```

## Testing

```bash
mvn test
```
18 unit test — CreditAssessmentService, LoanService, RepaymentService, DisbursementService.

## Дараагийн сайжруулалт

- Redis (credit score cache, idempotency key)
- Integration tests
- Swagger/OpenAPI
