# Hawk API — Postman / Insomnia (Sprint 2)

Base URL: `http://localhost:8080/api/v1`

## Usuários seed

| Perfil | Email | Senha |
| --- | --- | --- |
| Operador | carlos.mendes@aguiabranca.com.br | senha-segura |
| Gestor | ana.gestora@aguiabranca.com.br | senha-segura |
| Líder | bruno.lider@aguiabranca.com.br | senha-segura |

## Fluxo rápido

1. `POST /auth/login` com email/senha → copiar `token`
2. Header em todas as rotas: `Authorization: Bearer <token>`
3. Operador: `POST /ideias` → Gestor: `PATCH /ideias/{id}` (Em análise → Aprovada) → Gestor: `POST /projetos` → Líder: `GET /dashboard/summary`

## Swagger

`http://localhost:8080/swagger-ui.html`
