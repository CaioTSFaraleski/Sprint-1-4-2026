# Cerimônias Scrum - Sprint 3

Board Trello: https://trello.com/b/LvmH9MTx/safefield-ai-metaind%C3%BAstria-sprint-3-scrum

Cerimônias realizadas de forma assíncrona (registro em texto), conforme previsto no enunciado da Sprint 3.

## Ata de Planning

**Data:** 21/09/2026
**Participantes:** Pedro Alvarez Certo, Caio Tadeu Da Silva Faraleski, Gustavo Demeis Peres, Rodrigo Caruzzo Benevides, Eduardo do Nascimento Souza
**Duração:** assíncrona, consolidada em texto no board Trello

**Objetivo da Sprint 3:** evoluir o protótipo navegável do SafeField AI com telas que cobrem requisitos ainda não representados (configuração de áreas/regras de segurança e trilha de auditoria), configurar a gestão ágil do time no Trello e refinar a arquitetura técnica da solução.

**Itens puxados do Product Backlog para o Sprint Backlog:**

1. Evoluir protótipo: Configuração de Áreas e Regras de Segurança (Caio)
2. Evoluir protótipo: Trilha de Auditoria de Eventos Críticos (Rodrigo)
3. Configurar board Trello com Product Backlog e Sprint Backlog (Pedro)
4. Documentar cerimônias Scrum da Sprint 3 (Gustavo)
5. Refinar arquitetura técnica (Eduardo)
6. Atualizar README com os entregáveis da Sprint 3 (Pedro)
7. Revisar consistência entre protótipo e diagramas da Sprint 1 (Caio e Gustavo)

**Definição de pronto acordada:** ver `docs/scrum-sprint-3.md`, seção "Definição de Pronto".

**Riscos identificados:** prazo curto para evoluir o Figma mantendo fidelidade ao sistema visual da Sprint 2; dependência entre a tela de Áreas/Regras e a tela de Auditoria (ambas usam os mesmos componentes de tabela e badge de severidade).

## Dailies (registro assíncrono)

| Data | Integrante | Feito | Fazendo | Impedimentos |
| --- | --- | --- | --- | --- |
| 22/09/2026 | Pedro | Board Trello criado (5 colunas, Product Backlog e Sprint Backlog) | Início da seção Sprint 3 no README | Nenhum |
| 22/09/2026 | Caio | Wireframe da tela de Áreas e Regras de Segurança | Definição dos campos do formulário de nova área | Nenhum |
| 22/09/2026 | Rodrigo | Levantamento dos eventos que compõem a trilha de auditoria (RF15) | Layout da tabela de eventos | Nenhum |
| 22/09/2026 | Gustavo | Estrutura do documento de cerimônias | Ata de Planning | Nenhum |
| 22/09/2026 | Eduardo | Levantamento dos endpoints necessários para a arquitetura refinada | Diagrama de fluxo de eventos MQTT/WebSocket | Nenhum |
| 24/09/2026 | Pedro | Seção Sprint 3 do README com links do Trello e protótipo | Revisão geral da entrega | Nenhum |
| 24/09/2026 | Caio | Tela de Configuração de Áreas e Regras finalizada no Figma | Conexões de navegação no Prototype mode | Nenhum |
| 24/09/2026 | Rodrigo | Tela de Trilha de Auditoria finalizada no Figma | Conexão com Detalhe da Ocorrência existente | Nenhum |
| 24/09/2026 | Gustavo | Dailies e ata de Review em rascunho | Revisão de consistência com diagramas da Sprint 1 | Nenhum |
| 24/09/2026 | Eduardo | Documento de arquitetura refinada concluído | Revisão final com o time | Nenhum |

> Registro-modelo para a semana da Sprint 3: cada integrante deve confirmar/ajustar as linhas correspondentes ao seu trabalho real antes da entrega final, mantendo o board Trello como fonte de verdade do progresso.

## Ata de Review

**Data:** 25/09/2026
**Participantes:** Pedro Alvarez Certo, Caio Tadeu Da Silva Faraleski, Gustavo Demeis Peres, Rodrigo Caruzzo Benevides, Eduardo do Nascimento Souza

**O que foi entregue na Sprint 3:**

- Duas novas telas no protótipo Figma: Configuração de Áreas Industriais e Regras de Segurança, e Trilha de Auditoria de Eventos Críticos, ambas navegáveis e coerentes com o sistema visual da Sprint 2.
- Board Scrum no Trello com Product Backlog priorizado e Sprint Backlog da Sprint 3, refletindo o trabalho real do grupo.
- Documento de arquitetura técnica refinada (`docs/arquitetura-sprint-3.md`).
- README atualizado com todos os links e justificativas da Sprint 3.

**Feedback do grupo:** as duas novas telas resolvem as lacunas de RF02/RF03/RF04 e RF15 identificadas após a Sprint 2, sem introduzir inconsistência com os diagramas de caso de uso, atividades e classes da Sprint 1.

**O que volta para o Product Backlog:** tela de Cadastro de Operadores e Permissões (RF01) e tela de Configuração de Notificações por Perfil, priorizadas para uma eventual próxima sprint.

**Próximos passos:** manter o board Trello atualizado conforme o time avança nos itens restantes e usar a Definition of Done como critério de aceite antes de mover qualquer card para "Concluído".
