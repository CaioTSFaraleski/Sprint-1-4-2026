# Evolução do Protótipo - Sprint 3

Arquivo Figma (mesmo link, versão evoluída):
https://www.figma.com/design/d6wRQPPhz9r50kVu5zJFOe/SafeField-AI--Metaind%C3%BAstria-Dashboard?node-id=0-1&t=gfM8x4VZIW0N3Z67-1

## Objetivo

A Sprint 3 evolui o protótipo high-fidelity da Sprint 2 com telas que cobrem requisitos funcionais ainda não representados na interface (RF02, RF03, RF04 e RF15), mantendo o padrão visual e a preocupação com o uso em campo (tablet industrial, alto contraste, botões grandes para operadores com luvas) definidos na Sprint 2.

## Telas Novas

### 1. Configuração de Áreas Industriais e Regras de Segurança

**Requisitos cobertos:** RF02 (cadastro de áreas industriais), RF03 (cadastro de EPIs obrigatórios por área), RF04 (regras de segurança por área/turno/atividade), RN01.

**Caso de uso da Sprint 1:** "Configurar regras de segurança por área" (ator: Supervisor de Segurança / Gestor Industrial).

**Justificativa da mudança:** requisito não coberto na Sprint 2 — o protótipo anterior permitia consultar e cadastrar EPI por colaborador, mas não permitia configurar quais EPIs são obrigatórios por área nem as regras associadas a turno/atividade, o que é pré-requisito para o próprio motor de não conformidade (RF06).

**Layout e elementos:**
- Barra lateral com item de menu novo "Áreas e Regras", acessível a partir do Dashboard.
- Lista de áreas industriais (nome, setor, turno padrão, quantidade de EPIs obrigatórios, status ativo/inativo), com busca e filtro por setor.
- Botão "Nova área" abre formulário lateral (drawer) com: nome da área, setor, turnos aplicáveis, lista de EPIs obrigatórios (multi-seleção a partir do cadastro de EPI já existente) e regras adicionais em texto estruturado (ex.: "óculos obrigatório apenas no turno noturno").
- Ao selecionar uma área na lista, abre o detalhe da área com abas "EPIs obrigatórios" e "Regras de segurança", replicando a hierarquia de informação usada em "Colaboradores e EPIs".
- Estado de sucesso ao salvar: tela de confirmação curta, retornando à lista (mesmo padrão de "Sucesso de cadastro de EPI").

**Navegação:**
- Menu "Áreas e Regras" (novo item) → Configuração de Áreas e Regras de Segurança.
- Botão "Nova área" → formulário de cadastro → tela de sucesso → retorna à lista.
- Botão "Cancelar"/"Voltar" → retorna à tela anterior.

**Consideração de campo:** botões de ação principais (Salvar, Nova área) mantidos grandes e no topo/rodapé fixo da tela, para leitura e toque rápidos em tablet industrial.

### 2. Trilha de Auditoria de Eventos Críticos

**Requisitos cobertos:** RF15 (consulta de trilha de auditoria para eventos críticos), RNF03 (logs de auditoria).

**Caso de uso da Sprint 1:** "Consultar trilha de auditoria" (ator: Supervisor de Segurança / Gestor Industrial).

**Justificativa da mudança:** requisito não coberto na Sprint 2 — o protótipo anterior registrava o fluxo de uma ocorrência (detecção → alerta → regularização → validação → encerramento), mas não expunha uma trilha de auditoria consultável de eventos críticos, item explicitamente citado nos critérios de avaliação da Sprint 3 (coerência com requisitos não cobertos anteriormente).

**Layout e elementos:**
- Novo item de menu "Auditoria", acessível a partir do Dashboard e também via atalho no Relatório de Conformidade ("Ver trilha de auditoria completa").
- Tabela cronológica de eventos com colunas: data/hora, tipo de evento (criação, validação, escalonamento, encerramento de ocorrência), autor (operador/supervisor/sistema), área, ocorrência vinculada (link para o Detalhe da Ocorrência já existente).
- Filtros por período, área, tipo de evento e autor, seguindo o mesmo padrão visual dos filtros da tela de Relatórios.
- Ao clicar em uma linha, abre o mesmo componente de "Detalhe da Ocorrência" criado na Sprint 2, garantindo reaproveitamento de componente e coerência visual.
- Indicador de severidade reaproveitando as cores já definidas (verde/laranja/vermelho).

**Navegação:**
- Menu "Auditoria" (novo item) → Trilha de Auditoria.
- Relatórios de Conformidade → botão "Ver trilha de auditoria completa" → Trilha de Auditoria (filtrada pelo setor/período já selecionado no relatório).
- Linha de evento → Detalhe da Ocorrência (tela reaproveitada da Sprint 2).

**Consideração de campo:** tabela com linhas de altura generosa e leitura de status por cor/ícone, para consulta rápida por supervisores em campo.

## Sistema Visual — Sem Alterações

A paleta de cores, tipografia, iconografia e componentes reutilizáveis definidos na Sprint 2 foram mantidos integralmente nas duas novas telas, reforçando a consistência do sistema de design entre sprints.

## Mapeamento com Casos de Uso e Diagramas da Sprint 1

| Tela nova | Caso de uso | Classes envolvidas |
| --- | --- | --- |
| Configuração de Áreas e Regras de Segurança | Configurar regras de segurança por área | `AreaIndustrial`, `EPI`, `RegraSeguranca` |
| Trilha de Auditoria de Eventos Críticos | Consultar trilha de auditoria | `OcorrenciaRisco`, `Alerta`, `DeteccaoEPI` |

## Como Implementar no Figma (checklist para o time)

1. Duplicar o frame "Dashboard de Monitoramento" como base de layout (mesma sidebar/top bar).
2. Adicionar os dois novos itens de menu ("Áreas e Regras" e "Auditoria") em todos os frames existentes, para manter a navegação global coerente.
3. Criar os dois frames novos usando os componentes já existentes na biblioteca do arquivo (cards, tabelas, botões, badges de severidade).
4. Conectar as interações listadas em "Navegação" acima usando o Prototype mode do Figma.
5. Atualizar a tela "Documentação UX/UI" dentro do próprio Figma com o mapa de telas incluindo as duas novas.
