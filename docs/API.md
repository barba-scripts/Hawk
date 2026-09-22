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

## Contrato `GET /dashboard/summary` (mobile)

```json
{
  "totalInvestment": 0.0,
  "totalReturn": 0.0,
  "averageRoi": 0.0,
  "averageCostReduction": 0.0,
  "averageProductivity": 0.0,
  "activeProjects": 0,
  "completedProjects": 0,
  "ideasApproved": 0,
  "ideasInAnalysis": 0,
  "totalProjects": 4,
  "projectsByDivision": [
    { "division": "Logística", "count": 2 },
    { "division": "Passageiros", "count": 1 },
    { "division": "Comercial", "count": 1 }
  ]
}
```

Regras:

- `totalProjects` é o total de projetos considerados no filtro atual (`division`/`from`/`to`); `count` de cada item de `projectsByDivision` é relativo a esse mesmo total.
- O backend **não** envia `percent`: o app deve calcular `count / totalProjects` para evitar inconsistência entre backend e cliente. `count` + `totalProjects` são a fonte de verdade.
- `projectsByDivision` traz **todas** as divisões existentes nos projetos filtrados, não um conjunto fixo. O app não deve fixar apenas "Logística", "Passageiros" e "Comercial" no gráfico — divisões como "Financeiro" ou "Operações" também podem aparecer, e ignorá-las faz o total visual não fechar em 100%.
