# Studio Schedulle — Backend

API REST do **Studio Schedulle**, um marketplace de agendamento com pagamento integrado (Pix e cartão) para profissionais de beleza e bem-estar — barbearias, personal trainers, estúdios de pilates, manicures, podólogas, massagistas, trancistas e autônomos do setor.

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

## Domínios

| Host | Uso |
|---|---|
| `studioschedulle.com.br` / `www.studioschedulle.com.br` | Site institucional — porta de entrada de novos clientes |
| `api.studioschedulle.com.br` | Esta API (backend) |
| `app.studioschedulle.com.br` | Web de gestão do empreendedor (`studio-flow-web`) |

O app Android consome `api.studioschedulle.com.br` diretamente (CORS não se aplica a apps nativos). As origens de navegador liberadas em CORS são configuráveis via `CORS_ALLOWED_ORIGINS` (ver seção de variáveis de ambiente).

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
| `CORS_ALLOWED_ORIGINS` | `https://studioschedulle.com.br,https://www.studioschedulle.com.br,https://app.studioschedulle.com.br,http://localhost:3000,http://localhost:5173` | Origens de navegador liberadas (lista separada por vírgula) |
| `PORT` | `8080` | Porta HTTP da aplicação |
| `RESEND_API_KEY` | *(vazio)* | Chave de API do [Resend](https://resend.com) usada para enviar e-mail de verificação de conta e de redefinição de senha. Sem essa variável, o envio é apenas logado (aviso) e o cadastro/reset de senha continuam funcionando normalmente — o usuário só não recebe o e-mail. |
| `EMAIL_FROM` | `Studio Schedulle <naoresponda@studioschedulle.com.br>` | Remetente usado nos e-mails transacionais. **Precisa ser um endereço de um domínio verificado no Resend** — configure `studioschedulle.com.br` no painel do Resend antes de usar em produção. |
| `FRONTEND_URL` | `https://app.studioschedulle.com.br` | Base usada para montar os links de verificação de e-mail (`/verify-email?token=...`) e redefinição de senha (`/reset-password?token=...`) enviados por e-mail, apontando para o `studio-flow-web` (web de gestão). |

Veja também `.env.example` para uma lista pronta para copiar.

### 3. Subir a aplicação

```bash
./gradlew bootRun
```

A API sobe em `http://localhost:8080`. No startup, os planos Standard/Black/Diamond são inseridos automaticamente (idempotente).

### 4. Build / testes

```bash
./gradlew build
```

### 5. Testes unitários e cobertura

```bash
./gradlew test                          # roda os testes unitários (JUnit 5 + MockK + Kotest)
./gradlew jacocoTestReport               # gera o relatório HTML em build/reports/jacoco/test/html/index.html
./gradlew jacocoTestCoverageVerification # falha o build se a cobertura da autenticação cair abaixo de 95%
```

O fluxo de autenticação (`auth`, `security`, `email`, `common.exception`) tem cobertura mínima de **95%** (linha e instrução) garantida pelo Gradle — `./gradlew build`/`check` já roda `jacocoTestCoverageVerification` e falha caso a cobertura caia abaixo do limite. DTOs, documentos do Mongo e classes de configuração (`SecurityConfig` etc.) ficam fora dessa métrica por serem apenas dados/wiring, sem lógica a testar.

## Autenticação

Todas as rotas protegidas esperam o header:

```
Authorization: Bearer <token>
```

O token é obtido em `/api/auth/login` ou `/api/auth/verify-email` e carrega o `tipoPerfil` do usuário (`iat` incluso), usado para autorizar rotas restritas a `EMPREENDEDOR` ou `CLIENTE`.

### Verificação de e-mail

Toda conta nasce com `status = PENDING_VERIFICATION` e recebe um e-mail com link de confirmação. `POST /api/auth/login` rejeita contas ainda não verificadas. Fluxo:

1. `POST /api/auth/registrar` → cria a conta, envia o e-mail de verificação, **não** retorna JWT.
2. Usuário clica no link (`${FRONTEND_URL}/verify-email?token=...`) → o frontend chama `GET /api/auth/verify-email?token=...`, que ativa a conta (`status = ACTIVE`) e já retorna o JWT (login automático).
3. Se o e-mail não chegou ou o link expirou, `POST /api/auth/resend-verification` reenvia — a resposta é sempre a mesma mensagem genérica, para não revelar se o e-mail existe na base.

### Recuperação de senha

1. `POST /api/auth/forgot-password` → se o e-mail existir, gera um token válido por 2h e envia o link (`${FRONTEND_URL}/reset-password?token=...`). Resposta sempre genérica, mesmo se o e-mail não existir.
2. `GET /api/auth/reset-password/validate?token=...` → usado pela tela de redefinição para avisar cedo se o link é inválido/expirado, sem alterar nada.
3. `POST /api/auth/reset-password` → troca a senha (mínimo 8 caracteres) e invalida todas as sessões antigas: qualquer JWT emitido antes da troca (`iat` < `passwordChangedAt`) passa a ser rejeitado pelo filtro de autenticação.

### E-mail transacional

Os e-mails de verificação de conta e redefinição de senha são enviados via [Resend](https://resend.com) (`EmailService`, usando `RestClient` do Spring — sem SDK adicional). Ver variáveis `RESEND_API_KEY`, `EMAIL_FROM` e `FRONTEND_URL` na seção de variáveis de ambiente acima.

## Endpoints implementados

### Auth (públicos)

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/auth/registrar` | Cria um usuário (Empreendedor ou Cliente) com status `PENDING_VERIFICATION` e envia e-mail de verificação. Não retorna JWT. |
| POST | `/api/auth/login` | Autentica e retorna o JWT. Rejeita contas `PENDING_VERIFICATION`. |
| GET | `/api/auth/verify-email?token=` | Ativa a conta a partir do token enviado por e-mail e retorna o JWT (login automático) |
| POST | `/api/auth/resend-verification` | Reenvia o e-mail de verificação se a conta existir e estiver pendente (resposta sempre genérica) |
| POST | `/api/auth/forgot-password` | Envia e-mail com link de redefinição de senha, válido por 2h (resposta sempre genérica) |
| GET | `/api/auth/reset-password/validate?token=` | Retorna `{"valid": true/false}` sem alterar nada — usado pela tela de redefinição |
| POST | `/api/auth/reset-password` | Redefine a senha (mínimo 8 caracteres) e invalida sessões (JWTs) emitidas antes da troca |

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

### Por que AWS EC2

O alvo de deploy é uma instância **EC2 free tier** (`t3.micro`, `us-east-1`) rodando o jar diretamente via `systemd`, atrás do **Caddy** como reverse proxy com HTTPS automático (Let's Encrypt). Combinado com o MongoDB Atlas free tier (M0), o custo de operação fica próximo de zero para um volume de MVP — Elastic IP é gratuito enquanto associado a uma instância em execução.

O `Dockerfile` deste repositório continua disponível (multi-stage: `gradle:8.14.3-jdk21-alpine` compila o jar, `eclipse-temurin:21-jre-alpine` roda só o jar) caso o deploy migre para containers no futuro, mas **não é o caminho usado hoje** — rodar o jar direto via `systemd` evita compilar dentro da própria instância `t3.micro` (1 GB de RAM, insuficiente para um build Gradle+Kotlin) e evita o custo/complexidade de manter um registry de imagens.

### Provisionamento da instância

Pré-requisitos: usuário IAM com permissões escopadas a EC2 (RunInstances, Security Groups, Key Pairs, Elastic IP — sem acesso a billing, IAM ou outros serviços).

1. EC2 `t3.micro`, Ubuntu 22.04, região `us-east-1`.
2. Security group: porta 22 (SSH) restrita ao IP do administrador; portas 80/443 públicas; nenhuma outra porta exposta.
3. Elastic IP associado à instância (grátis enquanto a instância estiver rodando).
4. DNS: registro **A** de `api.studioschedulle.com.br` apontando para o Elastic IP, criado no provedor de DNS do domínio.

### Instalação na instância

```bash
# Java 21
sudo apt-get update && sudo apt-get install -y openjdk-21-jre-headless

# Copiar o jar já compilado (build/libs/studio-flow-backend-0.0.1-SNAPSHOT.jar) para /opt/studioschedulle/app.jar

# Variáveis de ambiente do serviço em /etc/studioschedulle.env (permissão 600, não versionado):
MONGODB_URI=mongodb+srv://<usuario>:<senha>@<cluster>.mongodb.net/studioflow?retryWrites=true&w=majority
JWT_SECRET=<segredo forte e aleatório>
JWT_EXPIRATION_MS=86400000
CORS_ALLOWED_ORIGINS=https://studioschedulle.com.br,https://www.studioschedulle.com.br,https://app.studioschedulle.com.br
EMAIL_FROM=Studio Schedulle <naoresponda@studioschedulle.com.br>
FRONTEND_URL=https://app.studioschedulle.com.br
RESEND_API_KEY=<opcional>
PORT=8080
```

O serviço `systemd` (`studioschedulle-backend.service`) sobe o jar com `EnvironmentFile=/etc/studioschedulle.env` e reinicia automaticamente em caso de falha ou reboot da instância.

### HTTPS via Caddy

Caddy roda na porta 443, emite/renova o certificado Let's Encrypt automaticamente assim que o DNS aponta pro Elastic IP, e faz proxy reverso para `localhost:8080`:

```
api.studioschedulle.com.br {
	reverse_proxy localhost:8080
}
```

### MongoDB

MongoDB Atlas free tier (M0). Como a EC2 tem IP fixo (o Elastic IP), o **Network Access do Atlas pode ser restrito a esse IP específico** — mais seguro do que liberar `0.0.0.0/0`.

## Fora do escopo desta etapa

Notificações, avaliações, painel administrativo da plataforma e integração real de pagamento não foram implementados.
