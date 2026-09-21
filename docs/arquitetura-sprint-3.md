# Arquitetura Técnica Refinada - Sprint 3

Este documento refina a arquitetura técnica do SafeField AI com base nas tecnologias definidas na Sprint 1 e nas decisões de UX consolidadas nas Sprints 2 e 3.

## Visão Geral dos Componentes

```text
[Câmeras/Sensores] --(MQTT)--> [Edge Gateway - Python/OpenCV/YOLO]
                                        |
                                        v (HTTPS/REST)
                                 [API - Java 21 + Spring Boot]
                                   |            |
                                   v            v
                          [PostgreSQL]   [Broker MQTT]
                                   |            |
                                   v            v
                        [WebSocket Gateway] <---
                                   |
                                   v
                    [Frontend React/TypeScript + PWA]
```

## Contratos de API (refinamento)

API REST versionada em `/api/v1`, seguindo os recursos já modelados no Diagrama de Classes da Sprint 1.

| Recurso | Endpoint | Método | Descrição |
| --- | --- | --- | --- |
| Áreas Industriais | `/api/v1/areas` | GET, POST | Lista e cadastra áreas industriais (suporta a nova tela de Configuração de Áreas e Regras) |
| Áreas Industriais | `/api/v1/areas/{id}/regras` | GET, PUT | Consulta e atualiza regras de segurança de uma área (RF04) |
| EPIs | `/api/v1/epis` | GET, POST | Cadastro e consulta de EPIs |
| Detecções | `/api/v1/deteccoes` | POST | Recebe eventos do Edge Gateway (RF05) |
| Ocorrências | `/api/v1/ocorrencias` | GET, POST, PATCH | Registro, consulta e atualização de status de ocorrências (RF07, RF11) |
| Alertas | `/api/v1/alertas` | GET | Consulta de alertas ativos por operador/área (RF08, RF09) |
| Auditoria | `/api/v1/auditoria` | GET | Consulta da trilha de eventos críticos, com filtros por período/área/autor (RF15, nova tela da Sprint 3) |
| Relatórios | `/api/v1/relatorios/conformidade` | GET | Geração de relatório de conformidade por setor/período (RF13) |

Todas as rotas exigem autenticação via token (JWT), respeitando RNF04 (canais seguros), e todo `PATCH`/`POST` em `ocorrencias` e `auditoria` gera um registro imutável de auditoria (RNF03).

## Fluxo de Eventos em Tempo Real

1. O Edge Gateway publica detecções no tópico MQTT `safefield/deteccoes/{areaId}`.
2. Um consumidor na API processa a detecção, aplica as regras de segurança da área (RF04/RF06) e, em caso de não conformidade, cria a ocorrência e o alerta.
3. O alerta é propagado por WebSocket nos canais `ws/operador/{operadorId}` e `ws/supervisor/{areaId}`, atendendo ao requisito de exibição em até 3 segundos (RNF01).
4. A regularização feita pelo operador e a validação feita pelo supervisor atualizam o mesmo registro de ocorrência, mantendo consistência transacional no PostgreSQL (RNF06).
5. Toda transição de estado da ocorrência (criada → alertada → regularizada → validada → encerrada) gera uma entrada na trilha de auditoria, consultável pela nova tela de Auditoria.

## Resiliência e Escalabilidade

- Perda temporária de conexão com sensores não deve interromper o painel: o consumidor de eventos usa fila com reprocessamento (RNF05).
- API e consumidores de eventos podem escalar horizontalmente atrás de um load balancer, com o broker MQTT como ponto de desacoplamento entre gateway e backend (RNF08).
- Parte da inferência de IA (deteção de EPI) pode rodar no próprio Edge Gateway, reduzindo dependência de latência de rede (RNF09).

## Estratégia de Deploy (MVP)

Ambiente de desenvolvimento/demonstração via Docker Compose com os serviços: `api` (Spring Boot), `frontend` (React), `postgres`, `mqtt-broker`, `edge-gateway-sim` (simulador de detecções para o MVP, já que câmeras reais estão fora do escopo) e `prometheus` + `grafana` para observabilidade.

## Decisões desta Sprint (em relação à Sprint 1)

- Definidos os endpoints de `areas` e `auditoria`, necessários para sustentar as duas novas telas do protótipo.
- Explicitado o uso de tópicos MQTT nomeados por área (`safefield/deteccoes/{areaId}`), facilitando o roteamento de regras por área definidas na nova tela de configuração.
- Confirmado que a trilha de auditoria é gerada automaticamente a partir das transições de estado da ocorrência, sem exigir cadastro manual adicional.
