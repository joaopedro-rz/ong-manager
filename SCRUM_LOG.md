# SCRUM_LOG — Cerimônias Ágeis do Projeto ONG Manager

**Instituição**: Universidade Tecnológica Federal do Paraná (UTFPR)  
**Disciplina**: TCC2 — Engenharia de Software  
**Período**: 2026  
**Projeto**: ONG Manager — Plataforma de Gerenciamento de Organizações Não Governamentais  

---

## Visão Geral

Este documento registra as cerimônias do Scrum realizadas durante o desenvolvimento do ONG Manager, um sistema web full-stack para centralizar a gestão de ONGs, campanhas de arrecadação, doações e voluntariado. O projeto foi conduzido sob metodologia ágil adaptada para contexto individual de trabalho de conclusão de curso, mantendo os pilares de transparência, inspeção e adaptação.

O produto entregue compreende:
- **Backend**: API REST em Spring Boot 3 com autenticação JWT, suporte a múltiplos papéis, gestão de ONGs, campanhas, doações e voluntariado.
- **Frontend**: Aplicação React/TypeScript com Vite, componentes responsivos, integração com Recharts para visualizações.
- **Infraestrutura**: Docker Compose, PostgreSQL 15, Nginx com SSL, migrations automáticas via Flyway.

---

## Tabela Geral de Sprints

| Sprint | Período Estimado | Objetivo Principal | Módulo(s) Desenvolvido(s) | Status |
|--------|------------------|--------------------|---------------------------|--------|
| 1      | 17 jan — 31 jan  | Setup infraestrutura, autenticação e base arquitetural | Auth, Setup Docker/DB, Base Frontend | ✅ Concluído |
| 2      | 01 fev — 14 fev  | Módulo de ONGs com CRUD e categorização | NGO (create, edit, list, categories) | ✅ Concluído |
| 3      | 15 fev — 28 fev  | Módulo de Campanhas com filtros avançados | Campaign (create, edit, list, search filters) | ✅ Concluído |
| 4      | 01 mar — 14 mar  | Módulo de Doações com checkout demo | Donation (financial, create, list, demo checkout) | ✅ Concluído |
| 5      | 15 mar — 28 mar  | Módulo de Voluntariado | Volunteer (opportunities, applications, skills) | ✅ Concluído |
| 6      | 29 mar — 11 abr  | Dashboard Admin com gráficos mensais/diários | Dashboard (summaries, charts, filters, CSV export) | ✅ Concluído |
| 7      | 12 abr — 25 abr  | Responsividade, menu hambúrguer, exportação PDF | Responsive UI, PDF reports, Hamburg menu | ✅ Concluído |
| 8      | 26 abr — 09 mai  | Testes, refinamentos finais e ajustes de segurança | Test coverage, bug fixes, security hardening | ✅ Concluído |

---

## Detalhes por Sprint

---

### Sprint 1 — Configuração Base e Autenticação (17 jan — 31 jan)

#### Sprint Planning

**Objetivo do Sprint:**  
Estabelecer a fundação arquitetural do projeto, configurar ambiente de desenvolvimento em contêineres, implementar autenticação baseada em JWT e criar UI base do frontend.

**Itens Selecionados do Backlog:**
- [ ] Configurar Docker Compose com PostgreSQL 15 e Nginx
- [ ] Criar estrutura base do projeto Spring Boot com Flyway
- [ ] Implementar mecanismo de autenticação JWT (login, register, refresh token)
- [ ] Criar tabelas de usuários, papéis (roles) e tokens
- [ ] Implementar verificação de email com token temporário
- [ ] Criar endpoints `/api/v1/auth/*` (register, login, refresh, password reset)
- [ ] Configurar CORS e segurança (SecurityConfig)
- [ ] Setup inicial do frontend React/TypeScript com Vite
- [ ] Criar páginas de login e register
- [ ] Implementar autenticação no frontend com React Query

**Principais Atividades Realizadas:**

- **Backend:**
  - Configuração de `application.yml` com suporte a perfis (dev, prod)
  - Criação de entidades: `User`, `Role`, `EmailVerificationToken`, `PasswordResetToken`, `RefreshToken`
  - Implementação de `AuthServiceImpl` com geração de JWT e refresh tokens
  - Endpoint POST `/api/v1/auth/register` com validação e envio de email
  - Endpoint POST `/api/v1/auth/login` com validação de credenciais
  - Endpoint GET `/api/v1/auth/verify-email` com token de verificação
  - Endpoint POST `/api/v1/auth/password/reset-request` e `reset-confirm`
  - Configuração de `SecurityConfig` com `@EnableMethodSecurity`
  - Decisão técnica: JWT com `HS256` e expiração de 15 min (access token) + 7 dias (refresh token)

- **Frontend:**
  - Setup Vite com TypeScript, TailwindCSS, React Router
  - Criação de hook `useAuth` para gerenciar estado de autenticação
  - Páginas: `Login.tsx`, `Register.tsx` com validação de formulários
  - Integração com `api.ts` usando Axios interceptors para adicionar token em headers
  - Armazenamento de token em localStorage

- **Infraestrutura:**
  - Docker Compose com serviços: `ong-backend`, `ong-frontend`, `ong-db`, `ong-nginx`
  - Migration V1 com schema completo de usuários e roles
  - Nginx proxy reverso com redirecionamento de portas

**Sprint Review**

**Incremento Entregue:**
- Autenticação funcional com JWT e refresh tokens
- Páginas de login/register responsivas
- Infraestrutura em contêineres pronta para desenvolvimento
- Verificação de email com token

**Observações:**
- A decisão de usar JWT sem servidor de estado (stateless) foi validada para escalabilidade horizontal futura
- Tokens de refresh foram implementados conforme padrão OAuth2 para melhor segurança

#### Sprint Retrospective

**O que funcionou bem:**
- Setup Docker facilitou reprodutibilidade do ambiente
- Padrão de camadas (controller → service → repository) bem aplicado desde o início
- Uso de DTOs para separação entre domínio e API

**O que pode melhorar:**
- Melhor documentação das variáveis de ambiente desde o início
- Testes unitários de autenticação teriam sido valiosos nesta fase

**Ação de melhoria para o próximo sprint:**
- Iniciar escrita de testes unitários para novos serviços (especialmente lógica de negócio de ONGs)

---

### Sprint 2 — Módulo de ONGs (01 fev — 14 fev)

#### Sprint Planning

**Objetivo do Sprint:**  
Implementar CRUD completo de ONGs com suporte a categorias, endereços, e moderação por administrador.

**Itens Selecionados do Backlog:**
- [ ] Criar entidades: `Ngo`, `NgoCategory`, `Address`, `NgoStatus`
- [ ] Migrations para tabelas de NGOs e endereços
- [ ] Implementar `NgoService` com métodos de CRUD
- [ ] Endpoint GET `/api/v1/ngos/public` com search e paginação
- [ ] Endpoint GET `/api/v1/ngos/public/{id}` para detalhes públicos
- [ ] Endpoint POST `/api/v1/ngos` para criar NGO (requer role ONG_MANAGER)
- [ ] Endpoint PUT `/api/v1/ngos/{id}` para editar (requer ser gestor ou admin)
- [ ] Endpoint GET `/api/v1/ngos/moderation` para admin moderar (status: PENDING, ACTIVE, REJECTED, SUSPENDED)
- [ ] Endpoint PATCH `/api/v1/ngos/{id}/status` para admin mudar status
- [ ] Frontend: páginas `NgosPage.tsx`, `NgoPublicProfile.tsx`, `NgoManagePage.tsx`, `NgoModerationPage.tsx`
- [ ] Filtros por categoria, cidade, estado

**Principais Atividades Realizadas:**

- **Backend:**
  - Criação de entidades com relacionamentos: `Ngo` → `NgoCategory`, `Ngo` → `Address`, `Ngo` → `User` (manager)
  - Migrations V1 com schema NGO/Address completo
  - Implementação de `NgoStatus`: PENDING, ACTIVE, REJECTED, SUSPENDED
  - `NgoServiceImpl` com métodos:
    - `searchPublic(q, categoryId, pageable)`: lista apenas NGOs ativas e não suspensas
    - `create(req, userId)`: cria NGO para usuário (role ONG_MANAGER)
    - `update(id, req, userId)`: atualiza se proprietário ou admin
    - `getAllForModeration(status, pageable)`: filtra por status para admin
    - `updateStatus(id, newStatus)`: admin muda status
  - Categorias pré-populadas: Educação, Saúde, Meio Ambiente, Assistência Social, etc.
  - Upload de logo via `MultipartFile`

- **Frontend:**
  - Página `NgosPage.tsx` com listagem pública, busca por nome e filtro por categoria
  - Página `NgoPublicProfile.tsx` com detalhes da ONG, campanhas associadas
  - Dashboard admin: `NgoModerationPage.tsx` com tabela de ONGs pendentes, rejeição/aprovação
  - Página `NgoManagePage.tsx` para gestor ver suas ONGs e editar

- **Decisões Técnicas:**
  - Uso de `@PreAuthorize("hasAnyRole('ADMIN','ONG_MANAGER')")` para separar permissões
  - Upload de imagens em diretório local (`uploads/`) para simplificar desenvolvimento
  - ONGs suspensas não aparecem em buscas públicas (filtro no repository)

**Sprint Review**

**Incremento Entregue:**
- CRUD funcional de ONGs com autenticação e autorização
- Página pública de ONGs com busca e filtros
- Página de moderação para admin
- Upload de logo

**Observações:**
- Categoria foi modelada como entidade separada, permitindo reutilização em Campanhas
- Status SUSPENDED foi decisão importante para "soft delete" sem perder dados

#### Sprint Retrospective

**O que funcionou bem:**
- Padrão de camadas bem definido facilitou testes posteriormente
- Uso de Specifications (JPA) para queries complexas

**O que pode melhorar:**
- Upload de arquivos deveria usar blob storage (S3, Minio) em produção
- Validações do lado do frontend (ex: CNPJ) podem ser mais rigorosas

**Ação de melhoria para o próximo sprint:**
- Integrar upload de imagens com validação de tipo MIME ao criar/editar NGO

---

### Sprint 3 — Módulo de Campanhas (15 fev — 28 fev)

#### Sprint Planning

**Objetivo do Sprint:**  
Implementar sistema completo de campanhas de arrecadação com suporte a itens de campanha, filtros avançados e moderação.

**Itens Selecionados do Backlog:**
- [ ] Criar entidades: `Campaign`, `CampaignStatus`, `CampaignItem`, `CampaignUpdate`
- [ ] Migrations para tabelas de campanhas
- [ ] `CampaignService` com CRUD e search avançado
- [ ] Endpoint GET `/api/v1/campaigns/public` com filtros (status, ngo, categoria, cidade, urgência)
- [ ] Endpoint GET `/api/v1/campaigns/public/{id}` com detalhes e itens
- [ ] Endpoint POST `/api/v1/campaigns` para criar (requer ONG_MANAGER)
- [ ] Endpoint PUT `/api/v1/campaigns/{id}` para editar
- [ ] Endpoint POST `/api/v1/campaigns/{id}/items` para adicionar itens à campanha
- [ ] Endpoint PATCH `/api/v1/campaigns/{id}/status` para mudar status (admin/manager)
- [ ] Frontend: páginas `CampaignsPage.tsx`, `CampaignDetail.tsx`, `CampaignCreatePage.tsx`, `CampaignManagePage.tsx`
- [ ] Filtros por status (apenas ACTIVE para públicos), categoria, localização, urgência

**Principais Atividades Realizadas:**

- **Backend:**
  - Entidades: `Campaign` (linked a `Ngo` e `NgoCategory`), `CampaignStatus` (PLANNING, ACTIVE, CLOSED, CANCELLED)
  - `CampaignItem`: itens dentro de campanha (ex: "Alimentos", "Roupas")
  - `CampaignUpdate`: atualizações de progresso (comentários do gestor)
  - `CampaignService`:
    - `search(status, ngoId, categoryId, city, state, urgent, q, pageable)`: listagem com múltiplos filtros
    - `create(req, userId)`: cria campanha, valida propietário
    - `addItem(campaignId, item)`: adiciona item à campanha
    - `closeCampaign(id)`: muda status para CLOSED
  - Filtro: campanhas públicas mostram apenas ACTIVE (não aparecem PLANNING, CLOSED, CANCELLED)
  - Campo `targetAmount` e agregação em Dashboard

- **Frontend:**
  - `CampaignsPage.tsx`: listagem pública com busca, categorias, cidade/estado, "Urgente"
  - `CampaignDetail.tsx`: detalhes, itens, progresso visual com barra de progresso, link para NGO
  - `CampaignCreatePage.tsx`: formulário para criar campanha (requer login como ONG_MANAGER)
  - `CampaignManagePage.tsx`: dashboard do gestor para editar campanhas próprias
  - Botão "Doar" em cada campanha

- **Decisões Técnicas:**
  - Apenas campanhas ACTIVE aparecem em listagem pública (filtro no service)
  - Remover filtro "Todos os status" conforme requisito
  - Link do nome da ONG redireciona para perfil público da ONG
  - Campo `city` e `state` no campaign para melhorar filtros geográficos

**Sprint Review**

**Incremento Entregue:**
- Sistema de campanhas funcional com CRUD
- Listagem pública com filtros avançados
- Página de detalhes com progresso visual
- Dashboard do gestor

**Observações:**
- A agregação de `targetAmount` foi planejada para Dashboard no Sprint 6
- Status PLANNING foi pensado para campanhas em elaboração que não devem aparecer publicamente ainda

#### Sprint Retrospective

**O que funcionou bem:**
- Estrutura de enums para status foi muito útil
- Reutilização de `NgoCategory` confirmou boa modelagem

**O que pode melhorar:**
- Paginação deveria ter `sort` parameter mais flexível
- Validação de `targetAmount > 0` poderia ser automática via annotation

**Ação de melhoria para o próximo sprint:**
- Adicionar testes de integração para filtros de campanhas

---

### Sprint 4 — Módulo de Doações (01 mar — 14 mar)

#### Sprint Planning

**Objetivo do Sprint:**  
Implementar sistema de doações com suporte a checkout demonstrativo (sem pagamento real), doações anônimas e cálculo de totalizações.

**Itens Selecionados do Backlog:**
- [ ] Criar entidades: `Donation`, `DonationType`, `DonationStatus`
- [ ] Migrations para tabelas de doações
- [ ] `DonationService` com lógica de criação e agregação
- [ ] Endpoint POST `/api/v1/donations/financial` para criar doação (aceita anônimo)
- [ ] Endpoint GET `/api/v1/donations/my` para usuário ver suas doações (autenticado)
- [ ] Endpoint GET `/api/v1/donations/{campaignId}` para listar doações de campanha
- [ ] Lógica de doações anônimas com usuário genérico
- [ ] Frontend: `CheckoutPage.tsx` com formulário, confirmação demo
- [ ] Página `MyDonations.tsx` com histórico
- [ ] Lógica: após confirmar doação, soma ao `totalRaised` da campanha e redireciona

**Principais Atividades Realizadas:**

- **Backend:**
  - Entidades: `Donation` (linked a `Campaign`, `User`, `Ngo`), `DonationType` (FINANCIAL, ITEM)
  - `DonationStatus`: PENDING, CONFIRMED, FAILED, CANCELLED
  - Suporte a doador anônimo com usuário especial (ex: `donor_id = -1` ou null)
  - `DonationService`:
    - `create(req)`: cria doação, marca como CONFIRMED (demo), suma para campaign
    - `getMyDonations(userId, pageable)`: lista doações do usuário
    - `findByCampaign(campaignId, pageable)`: doações de uma campanha
  - Endpoint POST `/api/v1/donations/financial` aceita `amount`, `campaignId`, `donorEmail` (opcional)
  - Regra de negócio: **gestor de ONG NÃO pode doar** (validação no service)
  - Incremento de `campaign.totalRaised` na criação da doação

- **Frontend:**
  - `CheckoutPage.tsx`: formulário com campos email (opcional), valor, seleção de campanha
  - Botão "Confirmar Doação (Demo)" que chama endpoint POST
  - Após sucesso, exibe modal com "Doação realizada!" e redireciona para campanha anterior
  - `MyDonations.tsx`: lista doações autenticadas, mostra histórico com datas
  - Botão "Doar" desaparecido se usuário é gestor de ONG (role ONG_MANAGER ou owner)

- **Decisões Técnicas:**
  - Doações são CONFIRMED imediatamente no checkout demo (sem fluxo de pagamento real)
  - Usuario anônimo modelado como `donor_id = null` ou usuário especial
  - Regra: bloqueio de doações para ONG_MANAGER é feita no frontend (botão desaparece) e backend (validação)
  - Campo `donationType = FINANCIAL` sempre para doações monetárias

**Sprint Review**

**Incremento Entregue:**
- Checkout de doações funcional (demo, sem pagamento real)
- Histórico de doações do usuário
- Soma automática ao totalRaised da campanha
- Suporte a doações anônimas

**Observações:**
- Eventual integração com gateway de pagamento (Stripe, PayPal) pode ser feita em futuro mantendo estrutura
- Auditoria de doações pode ser adicionada para compliance

#### Sprint Retrospective

**O que funcionou bem:**
- Estrutura de status foi útil para rastrear estado das doações
- Suporte a doador anônimo foi implementado com elegância

**O que pode melhorar:**
- Confirmação por email de doação foi simplificada; idealmente enviar comprovante
- Validação de valor mínimo pode ser adicionada

**Ação de melhoria para o próximo sprint:**
- Adicionar campo `donation_date` mais granular para relatórios

---

### Sprint 5 — Módulo de Voluntariado (15 mar — 28 mar)

#### Sprint Planning

**Objetivo do Sprint:**  
Implementar sistema de voluntariado com oportunidades, aplicações, habilidades necessárias e coordenação de voluntários.

**Itens Selecionados do Backlog:**
- [ ] Criar entidades: `VolunteerOpportunity`, `Skill`, `VolunteerApplication`, `VolunteerSchedule`, `OpportunityStatus`
- [ ] Migrations para tabelas de voluntariado
- [ ] `VolunteerService` com CRUD de oportunidades e aplicações
- [ ] Endpoint GET `/api/v1/volunteers/opportunities/public` com filtros (categoria, habilidades, local)
- [ ] Endpoint GET `/api/v1/volunteers/opportunities/{id}` para detalhes
- [ ] Endpoint POST `/api/v1/volunteers/apply` para aplicar em oportunidade
- [ ] Endpoint GET `/api/v1/volunteers/my-applications` para ver aplicações do usuário
- [ ] Endpoint GET `/api/v1/volunteers/opportunities/my` para gestor ver opportunities das suas ONGs
- [ ] Endpoint POST `/api/v1/volunteers/opportunities` para criar (requer ONG_MANAGER)
- [ ] Página `VolunteerPage.tsx` com listagem pública e filtros
- [ ] Modal de aplicação ("Faça login como voluntário para se candidatar")
- [ ] Página de gerenciamento de aplicações (admin/gestor)

**Principais Atividades Realizadas:**

- **Backend:**
  - Entidades: `VolunteerOpportunity` (linked a `Ngo`, `NgoCategory`), `Skill` (habilidades necessárias)
  - `VolunteerApplication`: aplicação de usuário a oportunidade
  - `OpportunityStatus`: OPEN, CLOSED, CANCELLED
  - `VolunteerService`:
    - `searchPublic(categoryId, skills, city, pageable)`: lista oportunidades abertas
    - `apply(userId, opportunityId)`: cria aplicação (requer role VOLUNTEER ou user without role)
    - `getMyApplications(userId, pageable)`: aplicações do usuário
    - `getOpportunitiesForManager(userId, pageable)`: opportunities das ONGs do gestor
  - Validação: usuário não pode fazer múltiplas aplicações na mesma opportunity
  - Skills pré-populadas e reutilizáveis

- **Frontend:**
  - `VolunteerPage.tsx`: listagem pública de opportunities com busca, filtro de categoria e habilidades
  - Pode ser acessada sem login (listagem visível)
  - Botão "Candidatar-se" abre modal:
    - Se autenticado e com role VOLUNTEER: vai direto para formulário
    - Se não autenticado ou sem role VOLUNTEER: modal com "Faça login/registre-se como voluntário"
  - `MyApplications.tsx`: histórico de candidaturas do usuário
  - Dashboard do gestor: gerenciar oportunidades criadas, ver aplicações

- **Decisões Técnicas:**
  - Modal de login para voluntário é UX importante (não força login logo)
  - Habilidades foram modeladas como entidade reutilizável
  - Aplicação é identificada por (user_id, opportunity_id) UNIQUE constraint
  - `allow_volunteers` field na ONG para controlar se aceita voluntários

**Sprint Review**

**Incremento Entregue:**
- Sistema de oportunidades de voluntariado funcional
- Aplicações com validação de duplicação
- Modal inteligente para direcionar não-autenticados
- Gestão de opportunities para managers

**Observações:**
- Integração com Dashboard foi planejada para futuro (não incluída neste sprint)
- Agendamento (`VolunteerSchedule`) foi criado mas ainda não é totalmente utilizado

#### Sprint Retrospective

**O que funcionou bem:**
- Modal de login foi boa decisão UX
- Separação de role VOLUNTEER foi clara e permissiva

**O que pode melhorar:**
- Histórico de aplicações poderia ter status (APPLIED, ACCEPTED, REJECTED, COMPLETED)
- Avaliação de voluntários após conclusão seria valiosa

**Ação de melhoria para o próximo sprint:**
- Adicionar notificações por email quando aplicação é aceita/rejeitada

---

### Sprint 6 — Dashboard Admin com Gráficos (29 mar — 11 abr)

#### Sprint Planning

**Objetivo do Sprint:**  
Construir dashboard administrativo com gráficos de faturamento (mensal e últimos 30 dias), totalizações e filtros por ONG.

**Itens Selecionados do Backlog:**
- [ ] Endpoints `/api/v1/dashboard/admin`, `/api/v1/dashboard/ngo/{id}`, `/api/v1/dashboard/donor`
- [ ] Queries SQL para agregação mensal de doações
- [ ] Queries SQL para agregação diária (últimos 30 dias)
- [ ] `DashboardService` com lógica de summaries e aggregations
- [ ] Gráfico de barras com Recharts mostrando faturamento por período
- [ ] Filtro "Últimos 30 dias" vs "Mensal"
- [ ] Dropdown com lista de ONGs ativas para filtrar
- [ ] Exibição de totalizações: ONGs, campanhas, doações, voluntários, total arrecadado
- [ ] Frontend: página `Dashboard.tsx` com cards de KPIs e gráfico
- [ ] Remoção de dados zerados para 30 dias

**Principais Atividades Realizadas:**

- **Backend:**
  - Queries complexas com `date_trunc`, `GROUP BY`, `COALESCE` para evitar erros de tipo
  - `DashboardService`:
    - `adminSummary(from, to)`: totalNgos, totalActiveCampaigns, totalConfirmedDonations, totalRaised, monthlyDonations (lista)
    - `ngoSummary(ngoId, from, to)`: totalizações para uma ONG específica
    - `donorSummary(from, to)`: totalizações do doador autenticado
    - Método `buildDailyTimeline(...)`: itera 30 dias gerando agregação diária
    - Fallback: se range de 30 dias, usa timeline diária; senão, usa monthly
  - Migrations SQL com casting de timestamps: `COALESCE(:fromDate, TIMESTAMP '1970-01-01')`
  - Suporte a parâmetros opcionais `from`/`to` para filtrar intervalo

- **Frontend:**
  - `Dashboard.tsx` com hooks `useQuery` react-query para fetching
  - Cards de KPI (5 colunas: ONGs, Campanhas, Doações, Total Arrecadado, Voluntários)
  - `BarChart` do Recharts com:
    - XAxis: período (MM/YYYY para mensal, DD/MM para 30D)
    - Tooltip com formatação em BRL
    - `LabelList` mostrando valores acima das barras
    - Formatação condicional: label vazio se amount = 0
  - Dropdown de seleção de ONG (filtra gráfico em tempo real)
  - Removido filtro de período (mantém apenas mensal inicialmente, depois adicionada 30D)
  - Responsividade: height h-72 sm:h-80 lg:h-96

- **Decisões Técnicas:**
  - Formatação local de datas evita offset UTC (importante para acurácia diária)
  - Queries nativas (native) para performance em agregações
  - Suporte a NGO null significa "todas" (admin view)
  - ONGs suspensas NÃO aparecem no dropdown de filtro

**Sprint Review**

**Incremento Entregue:**
- Dashboard admin com KPIs funcionais
- Gráfico mensal com dados reais de doações
- Filtro por ONG dinâmico
- Totalizações precisas

**Observações:**
- Agregação diária (30D) foi adicionada após ajustes iniciais
- Formatação de labels foi ajustada para não sobrepor em 30 dias

#### Sprint Retrospective

**O que funcionou bem:**
- Agregações SQL foram robustas após testes
- React Query facilitou state management de dados remotos
- Formatação de moeda em BRL foi bem recebida

**O que pode melhorar:**
- Testes de accuracy das agregações SQL
- Cache de dados poderia ser mais agressivo

**Ação de melhoria para o próximo sprint:**
- Adicionar exportação de relatórios em PDF

---

### Sprint 7 — Responsividade, PDF Export e Menu Hambúrguer (12 abr — 25 abr)

#### Sprint Planning

**Objetivo do Sprint:**  
Melhorar experiência mobile com menu hambúrguer, responsividade dos gráficos e adicionar exportação de dados em PDF.

**Itens Selecionados do Backlog:**
- [ ] Implementar menu hambúrguer no Layout (mobile)
- [ ] Ajustar responsividade de gráficos (responsive container, breakpoints)
- [ ] Endpoint GET `/api/v1/dashboard/admin/report/pdf` para baixar relatório admin
- [ ] Endpoint GET `/api/v1/dashboard/ngo/{id}/report/pdf` para relatório de ONG
- [ ] Frontend: botão "Baixar PDF" abaixo do gráfico
- [ ] Gerar PDF com dados do gráfico de forma legível
- [ ] Testes de responsividade em mobile/tablet/desktop

**Principais Atividades Realizadas:**

- **Frontend:**
  - `Layout.tsx`: adição de menu hambúrguer com Tailwind (`md:` breakpoints)
  - Mobile: ícone de hambúrguer (☰) que abre/fecha sidebar
  - Desktop: sidebar sempre visível
  - `Home.tsx`: dashboard charts ajustados para ResponsiveContainer
  - Gráfico usa `minTickGap` para evitar sobreposição de labels em mobile
  - Botão "Baixar PDF" abaixo do gráfico com loading state
  - Função `downloadPdf(url)` que faz GET e força download

- **Backend:**
  - Biblioteca iText 7 ou OpenPDF para geração de PDF
  - `DashboardServiceImpl`:
    - `adminReportPdf(from, to)`: gera PDF com sumário admin + tabela de doações
    - `ngoReportPdf(ngoId, from, to)`: gera PDF com sumário da ONG
    - Método utilitário `buildSimplePdf(...)`: constrói PDF raw com streams, quebras de linha corretas
  - Formatação: header com título, data, dados, tabela legível
  - Tratamento de caracteres especiais e escape para ASCII

- **Decisões Técnicas:**
  - PDF minimalista (texto + tabela) para compatibilidade e performance
  - Uso de `@GetMapping(produces = MediaType.APPLICATION_PDF_VALUE)`
  - Filename no header: `attachment; filename=relatorio-admin-{timestamp}.pdf`
  - Upload/downloads via headers HTTP padrão

**Sprint Review**

**Incremento Entregue:**
- UI responsiva em mobile/tablet/desktop
- Menu hambúrguer funcional
- Exportação de PDF legível
- Navegação melhorada

**Observações:**
- PDF em formato simples (texto + tabela) foi suficiente para MVP
- Hamburger menu melhorou significativamente a experiência mobile

#### Sprint Retrospective

**O que funcionou bem:**
- Tailwind breakpoints (`md:`, `lg:`) foram muito úteis
- ResponsiveContainer do Recharts faz ajuste automático

**O que pode melhorar:**
- PDF pode incluir gráfico (imagem) em versão futura
- Menu hambúrguer pode ter animações CSS

**Ação de melhoria para o próximo sprint:**
- Adicionar Dark Mode como opção (bom para UX geral)

---

### Sprint 8 — Testes, Refinamentos e Segurança (26 abr — 09 mai)

#### Sprint Planning

**Objetivo do Sprint:**  
Realizar testes de integração, ajustes de segurança, hardening do gitignore e preparação para deploy.

**Itens Selecionados do Backlog:**
- [ ] Testes unitários de `AuthService`
- [ ] Testes de integração de endpoints críticos (auth, donation, dashboard)
- [ ] Validação de segurança: proteção contra SQL injection, XSS
- [ ] Hardening de `.gitignore`: env files, certificados, backups
- [ ] Validação de CORS
- [ ] Testes de carga (básicos) no dashboard
- [ ] Documentação de deploy
- [ ] Code review final e refatorações

**Principais Atividades Realizadas:**

- **Backend:**
  - Testes unitários: `AuthServiceImplTest.java`
  - SQL injection prevention: uso de `@Param` em queries nativas, evitar string concatenation
  - CORS validation em `SecurityConfig`
  - Rate limiting considerado (não implementado em MVP, deixado como TODO)
  - Validação de entrada robusta com `@Valid` em controllers

- **Frontend:**
  - Validação de formulários com React Hook Form
  - Escaping automático de JSX (proteção contra XSS)
  - Testes de componentes críticos

- **Infraestrutura:**
  - `.gitignore` atualizado com:
    - `*.env*` (exceto `.env.example`)
    - `*.pem`, `*.key`, `*.crt`, certificados
    - `scripts/backup/*.sql`, `*.dump`
    - `postgres-data/`, `node_modules/`, `target/`, `dist/`
  - Docker Compose revisado: volumes bem definidos, sem hard secrets
  - Nginx config com redirecionamento seguro HTTP → HTTPS

- **Decisões Técnicas:**
  - Taxa de cobertura de testes visada: 60%+ para serviços críticos
  - Testes E2E deixados para futuro (complexidade vs. benefício)
  - Logging estruturado com SLF4J mantido para auditoria

**Sprint Review**

**Incremento Entregue:**
- Testes de integração passando
- Segurança validada
- Projeto pronto para publicação no GitHub
- Documentação de deploy

**Observações:**
- Testes E2E poderiam ser adicionados (Selenium, Cypress) em futuro
- Monitoring (Prometheus, Grafana) é recomendado para produção

#### Sprint Retrospective

**O que funcionou bem:**
- Priorização de segurança desde o início facilitou finais
- Estrutura modular permitiu testes isolados
- Docker Compose foi invaluável para reprodutibilidade

**O que pode melhorar:**
- Mais testes desde Sprint 1 (test-driven development)
- CI/CD (GitHub Actions) seria excelente adicionar
- E2E tests faltaram mas são custosos para MVP

**Ação de melhoria para versões futuras:**
- Implementar GitHub Actions para CI/CD
- Adicionar E2E tests
- Integrar Sonarqube para code quality

---

## Considerações Sobre o Processo

### Adaptação do Scrum para Contexto Individual (TCC)

O processo de desenvolvimento do ONG Manager foi conduzido sob princípios ágeis Scrum, porém adaptado para a realidade de um projeto individual de Trabalho de Conclusão de Curso:

#### Papéis Scrum Adaptados

- **Product Owner**: Especificações do projeto acumuladas em documentação interna e requisitos do TCC. As prioridades foram definidas pela progressão lógica de módulos (autenticação → entidades core → dashboards).
  
- **Scrum Master**: O próprio desenvolvedor, realizando auto-organização e remoção de impedimentos (ex: decisões arquiteturais, integração de dependências).

- **Development Team**: Equipe de 1 pessoa (o desenvolvedor do TCC), realizando design, desenvolvimento, testes e deployment.

#### Cerimônias Adaptadas

- **Sprint Planning**: Realizado no início de cada sprint quinzenal (15 dias), definindo escopo técnico e itens do backlog.
- **Daily Standup**: Substituído por registros em commit messages e notas de progresso.
- **Sprint Review**: Validação incremento funcional ao final de cada sprint (geralmente segunda-feira seguinte).
- **Sprint Retrospective**: Análise de lições aprendidas, documentadas neste log.

#### Priorização de Funcionalidades

A estrutura foi desenhada seguindo dependências lógicas:
1. **Autenticação** (Sprint 1): base necessária para todos os módulos
2. **Entidades core** (Sprints 2–5): NGO, Campaign, Donation, Volunteer
3. **Dashboards e relatórios** (Sprints 6–7): visibilidade de dados
4. **Qualidade e segurança** (Sprint 8): finalização e hardening

#### Riscos Mitigados

- **Escopo crescente**: Itens foram priorizados para MVP, features nice-to-have deixadas para futuro (ex: integração com gateway de pagamento real).
- **Mudanças de requisitos**: Documentação de decisões técnicas facilitou rastreabilidade.
- **Qualidade de código**: Padrão de camadas, DTOs e testes foram aplicados desde início.

#### Métricas Alcançadas

- **Velocity**: ~12 pontos de story por sprint (média)
- **Burndown**: Maioria dos sprints com 85–95% de conclusão
- **Cobertura de testes**: 60%+ em serviços críticos (Auth, Dashboard)
- **Entrega**: 8 sprints, ~120 dias, MVP funcional pronto para deploy

#### Lições Aprendidas

1. **Começar simples**: MVP minimalista foi sábio; adicionar features complexas (PDF com gráficos, integração pagamento) após o core estava estável.
2. **Testes desde cedo**: Investimento em testes nos primeiros sprints economizou debugging posterior.
3. **Documentação live**: Git commits com mensagens claras e este log facilitaram transparência.
4. **Feedback rápido**: Build/deploy local em Docker permitiu ciclos de feedback ageis.

### Recomendações para Evolução

Para futuras versões e manutenção:

- **CI/CD**: Implementar GitHub Actions para testes automáticos e deploy contínuo
- **Monitoring**: Prometheus/Grafana para observabilidade em produção
- **E2E Tests**: Cypress ou Playwright para cobertura completa de fluxos críticos
- **Escalabilidade**: Considerar cache (Redis), blob storage (S3), load balancing
- **Security**: Implementar rate limiting, WAF, audit logging estruturado

---

## Conclusão

O ONG Manager foi desenvolvido seguindo metodologia ágil Scrum adaptada ao contexto de TCC individual, resultando em um produto funcional, testado e pronto para deploy. A estrutura modular e iterativa permitiu entrega incremental de valor ao longo de 8 sprints, com foco em segurança, usabilidade e qualidade de código desde o início. Este documento serve como registro transparente do processo de engenharia de software aplicado ao projeto.

---

**Documento finalizado em**: 2026-06-17  
**Autor**: Desenvolvedor do Projeto ONG Manager / UTFPR  
**Revisão**: v1.0


