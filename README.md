# Studio Flow — Backend

API REST do **Studio Flow**, um marketplace de agendamento com pagamento integrado (Pix e cartão) para profissionais de beleza e bem-estar — barbearias, personal trainers, estúdios de pilates, manicures, podólogas, massagistas, trancistas e autônomos do setor.

Dois perfis compartilham o mesmo fluxo de login:

- **Empreendedor** — dono do estabelecimento, cadastra profissionais, serviços e horários.
- **Cliente** — busca estabelecimentos/profissionais, agenda um horário e paga na hora.

Esta API é consumida por dois frontends:

- App Android nativo (Kotlin/Compose) — `studio-flow-android`
- Web de gestão do empreendedor — `studio-flow-web`

## Stack

- **Kotlin** + **Spring Boot** (Web, Security, Validation, `spring-boot-starter-data-mongodb`)
- **Gradle** (Kotlin DSL)
- **MongoDB** — persistência definitiva do projeto. **Não há Postgres, JPA ou Flyway em lugar nenhum deste repositório.** Hospedagem alvo: [MongoDB Atlas](https://www.mongodb.com/atlas) free tier (M0, 512 MB, replica set — permite transactions multi-documento mesmo no tier gratuito), mantendo custo zero de infraestrutura.
- **JWT** (jjwt) para autenticação — perfis `EMPREENDEDOR` e `CLIENTE` via claim de role no mesmo login.
- Java 21 (toolchain do Gradle).

## Modelagem (MongoDB)

| Coleção | Descrição |
|---|---|
| `usuarios` | Conta única de login (`nome`, `email`, `senhaHash`, `tipoPerfil`) |
| `estabelecimentos` | Negócio de um empreendedor (`nome`, `categoria`, `endereco`, `planoId`) |
| `profissionais` | Profissional do estabelecimento — CPF, especialidades, comissão, periodicidade de repasse e conta bancária/Pix embutidos |
| `servicos` | Serviço oferecido (nome, duração, preço-base) |
| `horarios_disponiveis` | Janelas de disponibilidade por profissional e dia da semana |
| `agendamentos` | Reserva de horário — índice único composto `(profissionalId, dataHora)` evita overbooking na própria escrita |
| `pagamentos` | Confirmação de pagamento de um agendamento (Pix/cartão) |
| `splits_pagamento` | Divisão do valor entre plataforma, estabelecimento e profissional |
| `planos` | Standard / Black / Diamond — populados automaticamente no startup |
| `assinaturas` | Assinatura do estabelecimento a um plano |

### Planos

| Plano | Preço/mês | Taxa da plataforma | Limite de profissionais |
|---|---|---|---|
| Standard | R$ 49,90 | 5% | 3 |
| Black | R$ 89,90 | 2,5% | 10 |
| Diamond | R$ 189,90 | 1,5% | Ilimitado |

## Como rodar localmente

### Pré-requisitos

- JDK 21
- MongoDB acessível — Atlas free tier **ou** `docker-compose up` com Mongo local

### 1. Banco de dados

**Opção A — MongoDB Atlas (free tier, sem infra local):**

```bash
export MONGODB_URI="mongodb+srv://<usuario>:<senha>@<cluster>.mongodb.net/studioflow?retryWrites=true&w=majority"
```

**Opção B — Mongo local via Docker:**

```bash
docker-compose up -d
# usa mongodb://localhost:27017/studioflow por padrão, sem precisar exportar MONGODB_URI
```

### 2. Variáveis de ambiente (opcionais)

| Variável | Padrão | Descrição |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/studioflow` | String de conexão do MongoDB |
| `JWT_SECRET` | chave de desenvolvimento embutida | Segredo usado para assinar os tokens JWT |
| `JWT_EXPIRATION_MS` | `86400000` (24h) | Validade do token JWT |
| `PORT` | `8080` | Porta HTTP da aplicação |

### 3. Subir a aplicação

```bash
./gradlew bootRun
```

A API sobe em `http://localhost:8080`. No startup, os planos Standard/Black/Diamond são inseridos automaticamente (idempotente).

### 4. Build / testes

```bash
./gradlew build
```

## Autenticação

Todas as rotas protegidas esperam o header:

```
Authorization: Bearer <token>
```

O token é obtido em `/api/auth/login` ou `/api/auth/registrar` e carrega o `tipoPerfil` do usuário, usado para autorizar rotas restritas a `EMPREENDEDOR` ou `CLIENTE`.

## Endpoints implementados

### Auth (públicos)

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/auth/registrar` | Cria um usuário (Empreendedor ou Cliente) e retorna o JWT |
| POST | `/api/auth/login` | Autentica e retorna o JWT |

### Estabelecimento

| Método | Rota | Perfil | Descrição |
|---|---|---|---|
| POST | `/api/estabelecimentos` | EMPREENDEDOR | Cria o estabelecimento do usuário logado (assinatura Standard ativada automaticamente) |
| GET | `/api/estabelecimentos/me` | EMPREENDEDOR | Detalhes do próprio estabelecimento |
| PUT | `/api/estabelecimentos/me` | EMPREENDEDOR | Atualiza o próprio estabelecimento |
| PUT | `/api/estabelecimentos/me/plano` | EMPREENDEDOR | Troca de plano (Standard/Black/Diamond) |
| GET | `/api/estabelecimentos?categoria=` | público | Busca estabelecimentos, com filtro opcional por categoria |
| GET | `/api/estabelecimentos/{id}` | público | Detalhe de um estabelecimento |

### Profissional

| Método | Rota | Perfil | Descrição |
|---|---|---|---|
| POST | `/api/profissionais` | EMPREENDEDOR | Cadastra profissional no próprio estabelecimento (valida limite do plano ativo) |
| GET | `/api/profissionais/me` | EMPREENDEDOR | Lista os profissionais do próprio estabelecimento |
| PUT | `/api/profissionais/{id}` | EMPREENDEDOR | Atualiza um profissional próprio |
| DELETE | `/api/profissionais/{id}` | EMPREENDEDOR | Remove um profissional próprio |
| GET | `/api/profissionais/buscar?estabelecimentoId=&especialidade=` | público | Busca profissionais por estabelecimento ou especialidade |
| GET | `/api/profissionais/{id}` | público | Detalhe de um profissional |

### Serviço

| Método | Rota | Perfil | Descrição |
|---|---|---|---|
| POST | `/api/servicos` | EMPREENDEDOR | Cadastra serviço no próprio estabelecimento |
| GET | `/api/servicos/me` | EMPREENDEDOR | Lista os serviços do próprio estabelecimento |
| PUT | `/api/servicos/{id}` | EMPREENDEDOR | Atualiza um serviço próprio |
| DELETE | `/api/servicos/{id}` | EMPREENDEDOR | Remove um serviço próprio |
| GET | `/api/servicos/estabelecimento/{estabelecimentoId}` | público | Lista serviços de um estabelecimento |
| GET | `/api/servicos/{id}` | público | Detalhe de um serviço |

### Horário disponível

| Método | Rota | Perfil | Descrição |
|---|---|---|---|
| POST | `/api/horarios/profissional/{profissionalId}` | EMPREENDEDOR | Cadastra janela de disponibilidade de um profissional próprio |
| GET | `/api/horarios/profissional/{profissionalId}` | público | Lista disponibilidade de um profissional |
| PUT | `/api/horarios/{id}` | EMPREENDEDOR | Atualiza uma janela de disponibilidade |
| DELETE | `/api/horarios/{id}` | EMPREENDEDOR | Remove uma janela de disponibilidade |

### Agendamento

| Método | Rota | Perfil | Descrição |
|---|---|---|---|
| POST | `/api/agendamentos` | CLIENTE | Cria um agendamento; conflito de horário é rejeitado via índice único `(profissionalId, dataHora)` |
| GET | `/api/agendamentos/me` | CLIENTE | Lista os agendamentos do cliente logado |
| GET | `/api/agendamentos/profissional/{profissionalId}` | EMPREENDEDOR | Lista os agendamentos de um profissional do próprio estabelecimento |

### Pagamento

| Método | Rota | Perfil | Descrição |
|---|---|---|---|
| POST | `/api/pagamentos/confirmar` | CLIENTE | Confirma o pagamento (Pix/cartão) de um agendamento próprio e calcula o split entre plataforma, estabelecimento e profissional |

A integração com o provedor de pagamento fica atrás da interface `PaymentGateway` (`com.studioflow.backend.pagamento.gateway`), hoje implementada por `MockPaymentGateway` (aprova tudo). Trocar por Asaas/Pagar.me/Mercado Pago não exige mudanças no domínio.

## Deploy

### Por que Cloud Run

O alvo de deploy é o **Google Cloud Run** rodando a imagem Docker do `Dockerfile` deste repositório, com `--min-instances=0`: sem tráfego, a aplicação escala a zero instâncias e não há cobrança de compute. Combinado com o MongoDB Atlas free tier (M0) para persistência, o custo de operação fica próximo de zero para um volume de MVP — você paga só pelas requisições/CPU realmente consumidas quando há uso.

### Build da imagem

O `Dockerfile` é multi-stage: um estágio com `gradle:8.14.3-jdk21-alpine` compila o jar, e o estágio final usa `eclipse-temurin:21-jre-alpine`, copiando só o jar — imagem final enxuta, sem JDK nem Gradle. A aplicação escuta na porta definida por `PORT` (Cloud Run injeta essa variável automaticamente; localmente, sem `PORT` definido, cai para `8080`).

```bash
docker build -t studio-flow-backend .
docker run -p 8080:8080 -e MONGODB_URI="..." -e JWT_SECRET="..." studio-flow-backend
```

### Deploy manual via `gcloud`

Pré-requisitos: [gcloud CLI](https://cloud.google.com/sdk/docs/install) autenticado (`gcloud auth login`) e um projeto GCP com as APIs Cloud Run e Artifact Registry/Cloud Build habilitadas.

```bash
gcloud run deploy studio-flow-backend \
  --source . \
  --region southamerica-east1 \
  --platform managed \
  --allow-unauthenticated \
  --min-instances=0 \
  --max-instances=2 \
  --memory=512Mi
```

- `--min-instances=0`: escala a zero quando não há tráfego — nenhuma cobrança de compute em repouso. É o principal fator de custo mínimo.
- `--max-instances=2`: teto razoável para um MVP, evita custo inesperado em caso de pico/loop de tráfego.
- `--memory=512Mi`: suficiente para uma API Spring Boot com carga baixa; ajuste se necessário.
- `--source .` builda a imagem a partir do `Dockerfile` via Cloud Build, sem precisar configurar um registry manualmente.

### Variáveis de ambiente no Cloud Run

Configure os segredos e a connection string do banco **no serviço do Cloud Run**, não no código nem no workflow de CI:

```bash
gcloud run services update studio-flow-backend \
  --region southamerica-east1 \
  --update-env-vars MONGODB_URI="mongodb+srv://<usuario>:<senha>@<cluster>.mongodb.net/studioflow?retryWrites=true&w=majority" \
  --update-env-vars JWT_SECRET="<segredo forte e aleatório>" \
  --update-env-vars JWT_EXPIRATION_MS=86400000
```

`PORT` **não** deve ser definida manualmente — o Cloud Run já a injeta.

### MongoDB

O banco continua sendo o **MongoDB Atlas free tier (M0)**, externo ao Cloud Run e já gratuito — nenhuma configuração adicional de infraestrutura de banco é necessária para o deploy.

### Deploy automático via GitHub Actions (opcional)

Existe um workflow em `.github/workflows/deploy.yml` que builda a imagem e faz o deploy no Cloud Run a cada push na branch principal. Ele vem **desabilitado por padrão** (falha rápido na validação) até que os seguintes secrets sejam configurados em Settings → Secrets and variables → Actions do repositório:

| Secret | Descrição |
|---|---|
| `GCP_PROJECT_ID` | ID do projeto no Google Cloud |
| `GCP_WORKLOAD_IDP` | Provider da Workload Identity Federation (recomendado, evita chave estática) |
| `GCP_SERVICE_ACCOUNT` | E-mail da service account usada no deploy |
| `GCP_SA_KEY` | Alternativa à WIF: chave JSON da service account (menos recomendado) |

Nenhuma credencial real é necessária até que você decida habilitar o workflow — o comportamento de deploy manual acima funciona independentemente disso.

## Fora do escopo desta etapa

Notificações, avaliações, painel administrativo da plataforma e integração real de pagamento não foram implementados.
