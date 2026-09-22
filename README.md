# Hawk

Backend da plataforma de inovação, com Java 25, Spring Boot e MongoDB.
O projeto executável é o `pom.xml` da raiz (também utilizado pelo Dockerfile).
A pasta `hawk/` contém um esqueleto separado e não participa desse build.

## IA — Insights do Dashboard

Gera sob demanda uma análise gerencial em português usando o Google Gemini:
resumo executivo, destaques, pontos de atenção, observações e recomendações.
Não persiste análises nem altera o banco.

**Endpoint:** `POST /api/v1/dashboard/insights`, com JWT de usuário `LIDER`,
seguindo a mesma autorização de `GET /api/v1/dashboard/summary`.

### Configuração

Crie uma chave em [Google AI Studio](https://aistudio.google.com/apikey) e
configure as variáveis no ambiente do backend (por exemplo, em Environment no Render):

```dotenv
GEMINI_API_KEY=sua-chave-aqui
GEMINI_MODEL=gemini-2.5-flash-lite
```

O modelo padrão é `gemini-2.5-flash-lite`. Use uma chave/projeto habilitado para
esse modelo, observando os limites e a disponibilidade da cota gratuita da sua conta.
A implementação não ativa faturamento e não troca automaticamente de modelo ou
provedor quando a cota se esgota.

Consulte `.env.example` para as demais variáveis. O Spring Boot **não carrega
`.env` automaticamente**: exporte as variáveis no processo ou configure-as no
serviço de hospedagem. Nunca envie a chave ao frontend ou ao Git.

Configure também `MONGODB_URI` com a conexão da sua instalação. O padrão é
`mongodb://localhost:27017/hawk`, sem credenciais incorporadas. A aplicação
inicia sem `GEMINI_API_KEY`; somente o endpoint de IA fica indisponível.

### Request

Envie apenas os filtros opcionais `division`, `from` e `to`. Use `{}` para
analisar o dashboard sem filtros. As datas usam `YYYY-MM-DD` e `from` não pode
ser posterior a `to`. A divisão aceita até 100 caracteres.

```bash
curl -X POST 'http://localhost:8080/api/v1/dashboard/insights' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"division":"Logística","from":"2026-01-01","to":"2026-09-30"}'
```

### Response

Exemplo ilustrativo do contrato (o texto real é produzido pelo Gemini):

```json
{
  "generatedAt": "2026-09-22T00:00:00Z",
  "analysis": {
    "summary": "Os indicadores apresentam um retrato do recorte selecionado.",
    "highlights": [],
    "attentionPoints": [
      {
        "title": "Escopos diferentes",
        "description": "As contagens de ideias são globais; os projetos seguem os filtros.",
        "severity": "low"
      }
    ],
    "trends": [
      {
        "title": "Sem comparação temporal",
        "description": "Não há série histórica para concluir crescimento ou queda."
      }
    ],
    "recommendations": [
      {
        "title": "Acompanhar resultados",
        "description": "Registrar indicadores em períodos comparáveis.",
        "priority": "medium"
      }
    ]
  }
}
```

`severity` e `priority` aceitam `low`, `medium` ou `high`. Cada lista contém no
máximo 5 itens. O backend solicita JSON Schema ao Gemini e valida tipos, campos
obrigatórios, textos e limites antes de retornar a análise. O frontend deve
renderizar esses textos como texto, usando seu escape padrão.

### Dados e limites da análise

O serviço reutiliza `DashboardService.summary(division, from, to)`, sem novas
queries ou métricas inventadas. Envia **somente dados agregados**: investimento,
retorno financeiro, médias de ROI/redução de custos/produtividade, projetos ativos
e concluídos, ideias aprovadas e em análise. Envia também as datas do recorte e
um indicador booleano de filtro por divisão; não envia o texto da divisão,
nomes, descrições de projetos, usuários, documentos, e-mails ou tokens.

A semântica atual do dashboard é preservada:

- Divisão e datas filtram projetos pela data de criação em UTC, com limites inclusivos.
- As contagens de ideias são globais e não recebem esses filtros.
- Projetos ativos e concluídos podem se sobrepor; sua soma não é o total de projetos.
- O ROI é uma média simples dos valores cadastrados, não o ROI ponderado da carteira.
- Médias iguais a zero podem indicar ausência de valores. Não há série histórica.

Essas limitações constam do prompt para que a análise não invente tendências,
unidades, causas ou taxas de conversão. Não há frontend neste projeto; o consumidor
pode chamar o endpoint ao clicar em “Gerar análise com IA”.

### Indisponibilidade

A chamada HTTPS tem timeout de conexão de 5 segundos e leitura de 30 segundos,
sem retries de aplicação. O backend retorna HTTP **503**, no formato global
`{code, message, fieldErrors, traceId}`, para:

| Código | Situação |
| --- | --- |
| `AI_NOT_CONFIGURED` | Chave ausente ou modelo inválido na configuração |
| `AI_RATE_LIMITED` | Gemini respondeu HTTP 429 |
| `AI_UNAVAILABLE` | Timeout, falha de conexão ou outro erro HTTP do Gemini |
| `AI_INVALID_RESPONSE` | JSON inválido, schema inválido, resposta vazia, bloqueada ou truncada |

Filtros inválidos retornam 400; ausência de autenticação retorna 401; outros
perfis retornam 403. Falhas de IA não afetam o endpoint de resumo. Não existe
análise fixa de fallback, e logs não contêm chave, headers, payload ou resposta bruta.

### Arquitetura e validação

A feature segue controllers, services, records DTO, injeção por construtor,
`@ConfigurationProperties`, Jakarta Validation e `ApiException` existentes.
Utiliza `RestClient` do Spring com transporte HTTP do JDK, Jackson já presente
e SLF4J. Nenhuma dependência, coleção ou migration foi adicionada.

Na raiz do repositório, com JDK 25:

```bash
bash mvnw -B test
bash mvnw -B verify
```

No Windows, use `mvnw.cmd -B test` e `mvnw.cmd -B verify`.
`verify` inclui compilação, testes e empacotamento. Não há lint/formatter
configurado no POM. Os testes simulam o Gemini com `MockRestServiceServer`,
verificam a autorização real do dashboard com MockMvc e não requerem chave,
MongoDB ou chamadas à internet. O teste de contexto substitui cliente Mongo,
serviço JWT e seeder por mocks para não acessar o banco durante sua inicialização.
