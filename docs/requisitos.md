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
