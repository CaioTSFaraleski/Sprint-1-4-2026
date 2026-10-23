# Gestão Ágil · Sprint 4 (Artefatos Scrum)

Board Trello: https://trello.com/b/LvmH9MTx/safefield-ai-metaind%C3%BAstria-sprint-3-scrum

O mesmo board da Sprint 3 continua em uso, com o fluxo **Product Backlog → Sprint Backlog → Em andamento →
Em revisão → Concluído**. Os cards da Sprint 4 têm o prefixo **[S4]**.

## Equipe

| Nome | RM |
| --- | --- |
| Pedro Alvarez Certo | 554603 |
| Caio Tadeu Da Silva Faraleski | 558795 |
| Gustavo Demeis Peres | 555143 |
| Rodrigo Caruzzo Benevides | 554665 |
| Eduardo do Nascimento Souza | 558819 |

## Objetivo da Sprint 4

Transformar o protótipo aprovado nas Sprints 2 e 3 em uma **aplicação funcional**: backend Spring Boot e
frontend React cobrindo todos os fluxos principais com dados simulados da Metaindústria, ambiente executável
pelo avaliador seguindo só o README, documentação consolidada (arquitetura, manual de uso e rastreabilidade) e
vídeo de pitch.

## Product Backlog atualizado

| Prioridade | Item | Origem | Situação após a Sprint 4 |
| --- | --- | --- | --- |
| Alta | Implementação do backend (Spring Boot + PostgreSQL) | Backlog da Sprint 3 ("fora da Sprint 3") | **Concluído na Sprint 4** |
| Alta | Aplicação web com as telas do protótipo | Sprints 2 e 3 | **Concluído na Sprint 4** |
| Média | Tela de cadastro de operadores e permissões (RF01) | Backlog da Sprint 3 | **Concluído na Sprint 4** (tela Usuários) |
| Média | Integração real com câmeras e visão computacional (YOLO no edge gateway) | Sprint 1 (RNF09) | Backlog: depende de hardware; o contrato `POST /deteccoes` já está pronto |
| Média | Broker MQTT entre gateway e API | Arquitetura da Sprint 3 | Backlog: adiado no MVP (ver arquitetura final, seção 3) |
| Média | Notificação push ao operador (Web Push / PWA) | Sprint 4 | Backlog |
| Média | Migrações versionadas (Flyway) para produção | Sprint 4 | Backlog |
| Baixa | Configuração de notificações por perfil | Backlog da Sprint 3 | Backlog |
| Baixa | Autenticação corporativa (SSO) | Fora do escopo desde a Sprint 1 | Backlog |
| Baixa | Escalabilidade horizontal do WebSocket (broker de mensagens) | RNF08 | Backlog |

## Sprint Backlog · Sprint 4

| # | Item | Requisitos | Responsável | Status |
| --- | --- | --- | --- | --- |
| 1 | Backend: modelo de domínio, persistência JPA (H2/PostgreSQL) e dados simulados da Metaindústria | RF01–RF04, RN01 | Eduardo | Concluído |
| 2 | Backend: motor de regras, recebimento de detecções e ciclo da ocorrência | RF05–RF07, RF10, RF11, RN02–RN04, RN06 | Eduardo | Concluído |
| 3 | Backend: autenticação JWT e controle de acesso por perfil | RF01, RNF04 | Eduardo | Concluído |
| 4 | Backend: alertas em tempo real (WebSocket) e simulador do edge gateway | RF08, RF09, RNF01, RNF05 | Pedro | Concluído |
| 5 | Backend: dashboard, relatório de conformidade (CSV) e trilha de auditoria | RF12–RF15, RNF03, RN05 | Gustavo | Concluído |
| 6 | Frontend: login, layout por perfil e Dashboard de Monitoramento | RF01, RF14 | Caio | Concluído |
| 7 | Frontend: Alertas e Ocorrências, Detalhe da Ocorrência e tela do operador | RF07–RF12 | Rodrigo | Concluído |
| 8 | Frontend: Colaboradores e EPIs, Cadastro de EPI, Áreas e Regras | RF01–RF04 | Caio | Concluído |
| 9 | Frontend: Relatórios, Auditoria e Usuários | RF01, RF13, RF15 | Rodrigo | Concluído |
| 10 | Docker Compose, Maven Wrapper e scripts de execução sem Docker | Ambiente de avaliação | Pedro | Concluído |
| 11 | Testes automatizados (motor de regras e fluxo completo pela API) e verificação dos fluxos no navegador | DoD | Gustavo | Concluído |
| 12 | Documento de arquitetura final (decisões por Sprint, stack, API, modelo, segurança) | Entrega da Sprint 4 | Eduardo | Concluído |
| 13 | Diagramas da Sprint 4: sequência, classes implementadas, implantação (PNG) | Rastreabilidade | Caio e Gustavo | Concluído |
| 14 | README âncora, manual de uso e matriz de rastreabilidade | Entrega da Sprint 4 | Pedro | Concluído |
| 15 | Atualizar board Trello e registrar a ata da Review final | Entrega da Sprint 4 | Pedro | Concluído |
| 16 | Vídeo de pitch e demonstração (até 5 min) | Entrega da Sprint 4 | Todos | Em andamento |
| 17 | Arquivo de entrega `.txt` (integrantes, repositório, Trello, vídeo) | Entrega da Sprint 4 | Pedro | Em andamento |

## Definição de Pronto (Definition of Done) · Sprint 4

Evolui a DoD da Sprint 3 para itens de código. Um item é considerado **pronto** quando:

1. O artefato está publicado no local combinado (repositório GitHub, Trello ou YouTube).
2. Há correspondência explícita com pelo menos um requisito, caso de uso ou regra de negócio da Sprint 1
   (ver [matriz de rastreabilidade](requisitos.md#rastreabilidade-sprint-4)).
3. **Código:** compila, os testes automatizados passam (`./mvnw test`, `npm test`) e o fluxo funciona seguindo
   apenas o README, com Docker e sem Docker.
4. **Interface:** o fluxo funciona em desktop e tablet sem erros no console do navegador e mantém o sistema
   visual do protótipo (alto contraste, cores de severidade, botões grandes).
5. O README ou o documento pertinente foi atualizado.
6. O item foi revisado por, no mínimo, um integrante além do responsável (comentário no card ou aprovação na
   Review).
