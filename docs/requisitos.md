# Levantamento de Requisitos - SafeField AI

## Fontes Utilizadas

- Analise do contexto do Challenge 2026 FIAP x SPI Integracao.
- Entrevistas ficticias com personas industriais.
- Estudo do dominio de segurança operacional em ambiente de Metaindustria.

## Personas

### Persona 1 - Operador de Chao de Fabrica

**Nome ficticio:** Marcos Lima  
**Perfil:** operador de linha de montagem em turno rotativo.  
**Necessidades:** receber alertas objetivos, saber qual EPI esta pendente, corrigir rapidamente a situacao e evitar exposicao a risco.  
**Dores:** excesso de comunicacao manual, baixa clareza sobre regras por area e demora para saber se uma regularizacao foi aceita.

### Persona 2 - Supervisora de Seguranca

**Nome ficticio:** Renata Oliveira  
**Perfil:** responsavel por acompanhar varias areas produtivas simultaneamente.  
**Necessidades:** visualizar alertas em tempo real, priorizar riscos criticos, validar ocorrencias e acompanhar reincidencias.  
**Dores:** dependencia de rondas presenciais, registros manuais e falta de dados consolidados por turno.

### Persona 3 - Gestor Industrial

**Nome ficticio:** Marcelo Torres  
**Perfil:** gestor responsavel por produtividade, conformidade e indicadores de seguranca.  
**Necessidades:** consultar relatorios, medir tendencias de risco, comparar areas e justificar investimentos preventivos.  
**Dores:** dificuldade para transformar ocorrencias isoladas em decisoes estrategicas.

## Requisitos Funcionais

| Codigo | Requisito | Prioridade |
| --- | --- | --- |
| RF01 | O sistema deve permitir o cadastro de operadores, supervisores e gestores. | Alta |
| RF02 | O sistema deve permitir o cadastro de areas industriais monitoradas. | Alta |
| RF03 | O sistema deve permitir o cadastro de EPIs obrigatorios por area industrial. | Alta |
| RF04 | O sistema deve permitir a configuracao de regras de seguranca por area, turno e tipo de atividade. | Alta |
| RF05 | O sistema deve receber eventos de deteccao enviados por cameras, sensores ou gateways industriais. | Alta |
| RF06 | O sistema deve identificar nao conformidade quando um EPI obrigatorio nao for detectado para um operador em area monitorada. | Alta |
| RF07 | O sistema deve registrar ocorrencias de risco com data, hora, area, operador, evidencia, EPI ausente e nivel de severidade. | Alta |
| RF08 | O sistema deve emitir alerta em tempo real para o operador quando houver nao conformidade. | Alta |
| RF09 | O sistema deve emitir alerta em tempo real para o supervisor responsavel pela area. | Alta |
| RF10 | O sistema deve permitir que o operador registre a regularizacao do uso de EPI. | Media |
| RF11 | O sistema deve permitir que o supervisor valide, comente, escale ou encerre uma ocorrencia. | Alta |
| RF12 | O sistema deve manter historico de ocorrencias e acoes tomadas. | Alta |
| RF13 | O sistema deve gerar relatorio de conformidade por area, periodo, turno, operador e tipo de EPI. | Alta |
| RF14 | O sistema deve apresentar dashboard com indicadores de alertas ativos, tempo medio de regularizacao e reincidencia. | Alta |
| RF15 | O sistema deve permitir consulta de trilha de auditoria para eventos criticos. | Media |

## Requisitos Nao Funcionais

| Codigo | Requisito | Categoria | Prioridade |
| --- | --- | --- | --- |
| RNF01 | Alertas criticos devem ser exibidos ao supervisor em ate 3 segundos apos a deteccao. | Desempenho | Alta |
| RNF02 | A aplicacao deve manter disponibilidade minima de 99,5% durante turnos produtivos. | Confiabilidade | Alta |
| RNF03 | O sistema deve registrar logs de auditoria para criacao, alteracao, validacao e encerramento de ocorrencias. | Auditabilidade | Alta |
| RNF04 | A comunicacao entre dispositivos, API e frontend deve utilizar canais seguros. | Seguranca | Alta |
| RNF05 | O sistema deve tratar perda temporaria de conexao com sensores sem interromper o painel operacional. | Resiliencia | Alta |
| RNF06 | O banco de dados deve preservar consistencia das ocorrencias e alertas, evitando perda de eventos confirmados. | Integridade | Alta |
| RNF07 | A interface deve ser responsiva para uso em desktop, tablet industrial e dispositivos moveis. | Usabilidade | Media |
| RNF08 | O sistema deve permitir escalabilidade horizontal da API e dos consumidores de eventos. | Escalabilidade | Media |
| RNF09 | A solucao deve permitir execucao parcial de inferencia de IA em edge gateway para reduzir latencia. | Arquitetura | Media |
| RNF10 | Dados pessoais devem ser limitados ao necessario para operacao e conformidade, respeitando principios da LGPD. | Privacidade | Alta |

## Restricoes do Sistema

- A solucao depende da qualidade das imagens ou sensores disponiveis nas areas monitoradas.
- A deteccao automatica de EPI nao deve ser usada como unica evidencia disciplinar sem validacao humana.
- O MVP deve simular entradas de visao computacional quando cameras reais nao estiverem disponiveis.
- O sistema deve suportar operacao em ambiente industrial com conectividade instavel.
- A aplicacao deve priorizar prevencao e orientacao, evitando desenho funcional voltado a punicao automatica.
- A modelagem considera integracao futura com sistemas industriais, mas nao implementa ERP, RH ou controle de acesso fisico na Sprint 1.

## Regras de Negocio

| Codigo | Regra |
| --- | --- |
| RN01 | Cada area industrial pode ter uma lista propria de EPIs obrigatorios. |
| RN02 | Uma ocorrencia deve ser aberta quando uma regra de seguranca obrigatoria for violada. |
| RN03 | Alertas criticos devem ser enviados simultaneamente ao operador e ao supervisor. |
| RN04 | O encerramento de uma ocorrencia critica exige validacao do supervisor. |
| RN05 | Relatorios devem considerar apenas ocorrencias registradas e classificadas pelo sistema. |
| RN06 | Evidencias de deteccao devem ser associadas a ocorrencia correspondente para auditoria. |

---

## Rastreabilidade (Sprint 4)

Como cada requisito levantado na Sprint 1 foi implementado na aplicação final. Caminhos de código relativos a
`backend/src/main/java/br/com/safefield/` (API) e `frontend/src/` (interface).

### Requisitos funcionais

| Código | Status | Tela | Endpoint | Implementação | Teste |
|---|---|---|---|---|---|
| RF01 | Implementado | Login, Usuários, Colaboradores | `POST /auth/login`, `GET/POST /usuarios` | `domain/Usuario` (+3 perfis), `service/UsuarioService`, `security/` | `controleDeAcessoPorPerfil` |
| RF02 | Implementado | Áreas e Regras | `GET/POST/PUT /areas` | `domain/AreaIndustrial`, `service/AreaService` | `cadastroDeAreaERegraFicaNaTrilhaDeAuditoria` |
| RF03 | Implementado | Áreas e Regras, Cadastro de EPI | `POST /areas/{id}/regras`, `POST /epis` | `domain/RegraSeguranca`, `domain/EPI`, `service/EpiService` | `cadastroDeAreaERegraFicaNaTrilhaDeAuditoria` |
| RF04 | Implementado | Áreas e Regras (turno, atividade, severidade) | `POST/DELETE /areas/{id}/regras` | `AreaIndustrial.obterRegrasAtivas(turno)` | `regraDeOutroTurnoNaoEhCobrada` |
| RF05 | Implementado | Dashboard (simulador) | `POST /deteccoes` (`X-Gateway-Key`) | `service/DeteccaoService`, `service/SimuladorService`, `gateway/simulador_gateway.py` | `deteccaoSemEpi…`, `deteccaoComTodosOsEpisNaoAbreOcorrencia` |
| RF06 | Implementado | Detalhe da Ocorrência (evidência) | `POST /deteccoes` | `service/MotorRegrasService` | `MotorRegrasServiceTest` (6 cenários) |
| RF07 | Implementado | Alertas e Ocorrências, Detalhe | `GET /ocorrencias`, `GET /ocorrencias/{id}` | `domain/OcorrenciaRisco` (data/hora, área, operador, evidência, EPIs ausentes, severidade) | `deteccaoSemEpi…` |
| RF08 | Implementado | Meus alertas (operador) | `/ws/alertas?operadorId=`, `GET /alertas` | `OcorrenciaService.abrir`, `realtime/AlertasWebSocketHandler` | `deteccaoSemEpi…` (2 alertas) |
| RF09 | Implementado | Dashboard, aviso "Nova ocorrência" | `/ws/alertas`, `GET /alertas` | idem (alerta ao supervisor da área) | `deteccaoSemEpi…` |
| RF10 | Implementado | Meus alertas, Detalhe | `POST /ocorrencias/{id}/regularizacao` | `OcorrenciaService.regularizar` | `deteccaoSemEpi…` |
| RF11 | Implementado | Detalhe (validar, não validar, escalar, comentar) | `/validacao`, `/escalonamento`, `/comentarios` | `OcorrenciaService.validar/escalar/comentar` | `deteccaoSemEpi…` |
| RF12 | Implementado | Detalhe (histórico de ações), Colaboradores (histórico) | `GET /ocorrencias/{id}`, `GET /colaboradores/{id}` | `domain/AcaoOcorrencia` | `deteccaoSemEpi…` |
| RF13 | Implementado | Relatórios (CSV, impressão) | `GET /relatorios/conformidade[.csv]` | `service/RelatorioService`, `service/Indicadores` | `dashboardERelatorioRefletemOHistorico` |
| RF14 | Implementado | Dashboard | `GET /dashboard` | `service/DashboardService` | `dashboardERelatorioRefletemOHistorico` |
| RF15 | Implementado | Auditoria | `GET /auditoria` | `domain/EventoAuditoria` (`@Immutable`), `service/AuditoriaService` | `deteccaoSemEpi…`, `cadastroDeArea…` |

Além do levantado na Sprint 1, a Sprint 4 incluiu: **fichas de entrega de EPI** por colaborador (com validade e
bloqueio de entrega de EPI com CA vencido), **reincidência** calculada por tipo de EPI e o **simulador de
câmeras** controlável pelo painel.

### Requisitos não funcionais

| Código | Status | Como foi atendido |
|---|---|---|
| RNF01 | Atendido no MVP | Evento WebSocket publicado logo após o commit; nos testes locais o alerta aparece no painel em menos de 1 s |
| RNF02 | Parcial (arquitetura) | Health check em `/actuator/health`; alta disponibilidade depende da infraestrutura de produção |
| RNF03 | Atendido | Toda transição e todo cadastro geram `EventoAuditoria` imutável |
| RNF04 | Atendido no MVP | JWT em todas as rotas, chave do gateway, perfis; em produção, TLS no Nginx |
| RNF05 | Atendido no MVP | A interface reconecta o WebSocket sozinha (indicador Ao vivo/Offline); falha do simulador não derruba a API |
| RNF06 | Atendido | Transações JPA; ocorrência, ações, alertas e auditoria gravados na mesma transação |
| RNF07 | Atendido | Interface responsiva verificada em desktop, tablet (820 px) e celular (390 px) |
| RNF08 | Parcial | API sem estado (JWT); o WebSocket em memória limita a uma instância (ver arquitetura, seção 9) |
| RNF09 | Previsto | O contrato `POST /deteccoes` recebe detecções já processadas no edge gateway |
| RNF10 | Atendido | Só dados necessários à operação; evidência guardada como referência |

### Regras de negócio

| Código | Implementação |
|---|---|
| RN01 | `RegraSeguranca` pertence a uma `AreaIndustrial`; cada área tem sua lista de EPIs obrigatórios |
| RN02 | `DeteccaoService` abre ocorrência sempre que o motor encontra uma regra obrigatória violada |
| RN03 | `OcorrenciaService.abrir` emite os alertas ao operador e ao supervisor no mesmo instante |
| RN04 | Só `SupervisorSeguranca` valida; o encerramento só acontece pela validação aprovada |
| RN05 | Relatórios consideram apenas ocorrências registradas e classificadas (com severidade) |
| RN06 | Cada `OcorrenciaRisco` referencia a `DeteccaoEPI` com a evidência (`evidenciaUrl`, dispositivo, confiança) |
