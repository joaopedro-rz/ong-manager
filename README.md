# ONG Manager — Sistema Web para Gestão de Doações em ONGs

> **TCC2 — UTFPR.** Sistema web gratuito, open-source e seguro para ONGs de pequeno
> e médio porte gerenciarem doações financeiras e materiais, campanhas e
> voluntariado, com painéis administrativos por perfil de usuário.

## Stack

- **Backend:** Java 21, Spring Boot 3, Spring Security + JWT, Spring Data JPA,
  Flyway, MapStruct, Lombok, Springdoc OpenAPI.
- **Frontend:** React 18 + TypeScript + Vite, React Router, React Query,
  React Hook Form + Zod, Tailwind CSS, Recharts.
- **Banco:** PostgreSQL 15 (Flyway).
- **Infra:** Docker Compose + Nginx (reverse proxy).

## Arquitetura

Backend em 3 camadas (Controller → Service → Repository) seguindo SOLID, com
DTOs separando entrada e saída e tratamento global de erros (`@ControllerAdvice`).
Autenticação stateless via JWT (access + refresh) com BCrypt e RBAC por
`@PreAuthorize` (`ADMIN`, `ONG_MANAGER`, `DONOR`, `VOLUNTEER`).

## Pré-requisitos

- Docker e Docker Compose
- (Dev) Java 21 + Maven 3.9, Node.js 20+

## Executando com Docker

```bash
cp .env.example .env
docker compose up --build
```

- Frontend: http://localhost (Nginx)
- API:      http://localhost/api/v1
- Swagger:  http://localhost:8080/swagger-ui.html (acesso direto ao backend)

## Executando em desenvolvimento

```bash
# 1. Banco
docker compose up -d db

# 2. Backend
cd backend && ./mvnw spring-boot:run    # ou: mvn spring-boot:run

# 3. Frontend
cd frontend && npm install && npm run dev
```

## Variáveis de ambiente (.env)

Veja `.env.example`. Principais: `POSTGRES_*`, `JWT_SECRET`,
`JWT_EXPIRATION_MS`, `MAIL_*`, `APP_BASE_URL`, `ALLOWED_ORIGINS`.

## Endpoints principais

- `POST /api/v1/auth/register` · `POST /api/v1/auth/login` · `POST /api/v1/auth/refresh`
- `GET /api/v1/ngos/public` · `POST /api/v1/ngos` · `PATCH /api/v1/ngos/{id}/status`
- `GET /api/v1/campaigns/public` · `POST /api/v1/campaigns`
- `POST /api/v1/donations` · `POST /api/v1/donations/{id}/confirm` · `GET .../receipt` (PDF)
- `GET /api/v1/volunteer-opportunities` · `POST .../applications`
- `GET /api/v1/dashboard/admin`

Documentação completa: **Swagger UI** em `/swagger-ui.html`.

## Estrutura de pastas

```
backend/   # Spring Boot (config, controller, service, repository, entity, dto, mapper, security)
frontend/  # React + Vite (features modulares, hooks, lib, router)
nginx/     # Reverse proxy
scripts/   # Backup e utilitários
```

## Testes

```bash
cd backend && mvn test
cd frontend && npm run test
```

## Backup do banco

```bash
./scripts/backup.sh
```

## Licença

MIT.
