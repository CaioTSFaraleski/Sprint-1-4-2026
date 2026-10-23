# Cerimônias Scrum · Sprint 4

Board Trello: https://trello.com/b/LvmH9MTx/safefield-ai-metaind%C3%BAstria-sprint-3-scrum

## Ata de Planning

**Data:** __/__/2026
**Participantes:** Pedro Alvarez Certo, Caio Tadeu Da Silva Faraleski, Gustavo Demeis Peres, Rodrigo Caruzzo
Benevides, Eduardo do Nascimento Souza

**Objetivo da Sprint 4:** entregar a solução completa e funcional do SafeField AI, ou seja, backend e frontend
cobrindo os fluxos principais com dados simulados da Metaindústria, executável pelo avaliador seguindo só o
README, com a documentação consolidada e o vídeo de pitch.

**Itens puxados para o Sprint Backlog:** os 17 itens listados em [scrum-sprint-4.md](scrum-sprint-4.md#sprint-backlog--sprint-4),
priorizados nesta ordem:

1. Backend do fluxo crítico (detecção → ocorrência → alerta → regularização → validação), que é o coração do
   produto e o fluxo do diagrama de atividades da Sprint 1.
2. Telas do protótipo ligadas à API, começando por Dashboard, Alertas e Detalhe da Ocorrência.
3. Cadastros (áreas, regras, EPIs, usuários), relatórios e auditoria.
4. Ambiente de execução (Docker e sem Docker), testes e documentação.
5. Vídeo de pitch e entregável.

**Decisões técnicas acordadas:**

- Manter a stack da Sprint 1 (Java 21 + Spring Boot, React + TypeScript, PostgreSQL, Docker).
- Simular as câmeras com um simulador interno e um script de gateway externo, já que não há hardware.
- Publicar as detecções por HTTP no MVP e deixar o MQTT para uma fase seguinte, reduzindo o risco no ambiente
  de avaliação.

**Riscos identificados:** prazo para implementar todas as telas do protótipo com integração real; o avaliador
pode não ter Docker (mitigado com a execução sem Docker usando o Maven Wrapper e `iniciar.sh`/`iniciar.cmd`);
diferença de fuso horário entre servidor e navegador (mitigada exibindo tudo no horário da planta).

## Ata da Review final

**Data:** __/__/2026
**Participantes:** Pedro Alvarez Certo, Caio Tadeu Da Silva Faraleski, Gustavo Demeis Peres, Rodrigo Caruzzo
Benevides, Eduardo do Nascimento Souza

**Incremento entregue na Sprint 4:**

- **API Spring Boot** com 13 controladores REST em `/api/v1`, motor de não conformidade por área e turno,
  ciclo completo da ocorrência, alertas em tempo real via WebSocket, trilha de auditoria imutável,
  dashboard, relatório de conformidade com exportação CSV, autenticação JWT por perfil e chave do edge
  gateway.
- **Interface React + TypeScript** com todas as telas do protótipo (Login, Dashboard, Alertas e Ocorrências,
  Detalhe da Ocorrência, Colaboradores e EPIs, Cadastro de EPI, Áreas e Regras, Relatórios, Auditoria) e mais
  as telas Usuários e **Meus alertas** (operador, para tablet).
- **Dados simulados da Metaindústria:** 24 colaboradores, 6 áreas, 10 EPIs com CA, 26 regras por área e turno,
  fichas de entrega e 30 dias de histórico, além do simulador de câmeras ao vivo.
- **Ambiente de avaliação:** `docker compose up --build` (PostgreSQL + API + Nginx) ou execução sem Docker com
  Java 21 e Node.
- **Documentação:** README âncora, [arquitetura final](arquitetura-final.md), [manual de uso](manual-de-uso.md),
  [matriz de rastreabilidade](requisitos.md#rastreabilidade-sprint-4) e diagramas de sequência, classes
  implementadas e implantação.

**Demonstração realizada:** roteiro de avaliação do README: painel ao vivo → simular operador sem EPI → alerta
em tempo real → detalhe com evidência → escalar → validar regularização → visão do operador confirmando a
regularização → cadastro de EPI e regra → relatório com exportação → trilha de auditoria.

**Métricas da Sprint:**

| Indicador | Resultado |
| --- | --- |
| Requisitos funcionais da Sprint 1 implementados | 15 de 15 |
| Itens do Sprint Backlog concluídos | 15 de 17 (vídeo e entregável em andamento no momento da Review) |
| Testes automatizados | 11 no backend e 3 no frontend, todos passando |
| Fluxos principais verificados no navegador | Desktop, tablet e celular, sem erros no console |

**Feedback do grupo:** _(registrar na reunião)_

**O que volta para o Product Backlog:** integração com câmeras reais e YOLO no edge gateway, broker MQTT,
notificação push ao operador, migrações com Flyway, configuração de notificações por perfil, SSO corporativo e
escalabilidade horizontal do WebSocket.

**Próximos passos:** gravar e publicar o vídeo de pitch (até 5 min, com protagonismo dos integrantes), incluir o
link no README e no arquivo de entrega e mover os cards restantes para "Concluído".
