# SafeField AI - Metaindustria

## Integrantes

| Nome | RM |
| --- | --- |
| Pedro Alvarez Certo | 554603 |
| Caio Tadeu Da Silva Faraleski | 558795 |
| Gustavo Demeis Peres | 555143 |
| Rodrigo Caruzzo Benevides | 554665 |
| Eduardo do Nascimento Souza | 558819 |

## Contexto do Challenge

O Challenge 2026 FIAP x SPI Integracao propõe uma visão de Metaindustria em que ambientes industriais físicos e digitais operam de forma integrada. Nesse contexto, a segurança do trabalho precisa sair de um modelo reativo e punitivo, baseado apenas em auditorias posteriores, para um modelo proativo, contínuo e orientado por dados.

O problema central tratado neste projeto é a dificuldade de acompanhar, em tempo real, o uso correto de EPIs e a exposição dos operadores a situações de risco em campo. Em ambientes industriais reais, a não conformidade pode ocorrer por pressa operacional, falha de comunicação, troca de área, esquecimento ou baixa visibilidade do supervisor sobre múltiplas frentes de trabalho.

## Problema Abordado

A gestão tradicional de segurança costuma identificar falhas somente depois que uma infração ocorreu ou depois de um incidente. Isso reduz a capacidade de prevenção e reforça uma cultura punitiva, na qual o trabalhador é cobrado após o erro, em vez de receber apoio operacional antes que o risco se concretize.

No ambiente Metaindustria, esse problema deve ser tratado como uma questão operacional de campo:

- operadores precisam receber orientação rápida e contextual;
- supervisores precisam enxergar riscos em tempo real;
- gestores precisam acompanhar indicadores de conformidade e reincidência;
- o sistema precisa registrar evidências e trilhas de auditoria sem transformar a solução em uma ferramenta genérica de RH.

## Proposta de Solucao

O SafeField AI é uma aplicação para monitoramento proativo de EPIs e riscos operacionais em campo industrial. A solução combina visão computacional, sensores industriais e alertas em tempo real para identificar situações como ausência de capacete, óculos, luvas, colete, protetor auricular ou entrada em área restrita sem autorização.

Quando uma não conformidade é detectada, o sistema registra o evento, calcula o nível de risco, notifica o operador e o supervisor, acompanha a regularização e consolida os dados em relatórios de conformidade. O objetivo não é punir automaticamente o operador, mas antecipar riscos, orientar a correção e gerar inteligência para melhoria da cultura de segurança.

## Escopo

### Atores Atendidos

- **Operador de chão de fábrica:** recebe alertas, registra regularização e consulta orientações de segurança.
- **Supervisor de segurança:** acompanha eventos em tempo real, valida ocorrências, aciona equipes e analisa conformidade por área.
- **Gestor industrial:** consulta indicadores consolidados, relatórios e tendências de risco.
- **Sistema de visão computacional/sensores:** envia detecções automáticas para a aplicação.

### Funcionalidades Cobertas

- cadastro de operadores, áreas industriais, EPIs e regras de segurança;
- monitoramento contínuo do uso de EPI por área;
- emissão de alertas em tempo real;
- registro de ocorrências e evidências;
- validação e encerramento de alertas pelo supervisor;
- geração de relatórios de conformidade;
- painel de indicadores por área, turno, tipo de EPI e reincidência.

### Fora do Escopo da Sprint 1

- implementação funcional da aplicação;
- treinamento real de modelo de IA;
- integração com câmeras físicas ou CLPs reais;
- autenticação corporativa completa;
- aplicação de sanções disciplinares automáticas.

## Tecnologias Selecionadas

| Camada | Tecnologia | Justificativa |
| --- | --- | --- |
| Backend | Java 21 + Spring Boot | Ecossistema robusto para APIs corporativas, alta confiabilidade, validação, segurança e integração com sistemas industriais. |
| Frontend | React + TypeScript | Interface responsiva para painéis operacionais em tempo real, com tipagem forte e boa manutenção. |
| Mobile/PWA | React PWA | Permite alertas e interação em tablets industriais ou smartphones sem exigir aplicativo nativo no MVP. |
| Banco relacional | PostgreSQL | Consistência transacional para ocorrências, regras, usuários, auditoria e relatórios. |
| Mensageria | MQTT | Protocolo leve e comum em IoT industrial, adequado para sensores, gateways e eventos em tempo real. |
| Tempo real | WebSocket | Atualização imediata do painel do supervisor e notificações sem polling excessivo. |
| IA/Visão computacional | Python + OpenCV + modelo YOLO | Detecção de EPIs em imagens com baixa latência e possibilidade de execução em edge gateway. |
| Infraestrutura | Docker | Padroniza ambientes de desenvolvimento, testes e implantação. |
| Observabilidade | Prometheus + Grafana | Monitoramento de latência, disponibilidade e volume de eventos críticos. |

## Requisitos e Modelagem

O levantamento formal de requisitos está disponível em [docs/requisitos.md](docs/requisitos.md).

Diagramas UML:

- [Diagrama de Casos de Uso](docs/diagramas/casos-de-uso.puml)
- [Diagrama de Atividades](docs/diagramas/atividades-alerta-epi.puml)
- [Diagrama de Classes](docs/diagramas/classes.puml)

## Coerencia entre os Diagramas

O caso de uso central "Monitorar uso de EPI" inclui a identificação automática de não conformidade, o registro da ocorrência e a emissão de alerta. O diagrama de atividades detalha esse fluxo crítico, desde a captura do evento pelo sistema de visão computacional até a regularização e encerramento do alerta pelo supervisor.

O diagrama de classes sustenta esse comportamento com entidades como `Operador`, `AreaIndustrial`, `EPI`, `RegraSeguranca`, `DeteccaoEPI`, `OcorrenciaRisco`, `Alerta` e `RelatorioConformidade`, garantindo alinhamento entre requisitos, processos e estrutura de dados.

## Estrutura do Repositorio

```text
metaindustria-safefield/
├── README.md
├── ENTREGA.txt
└── docs/
    ├── requisitos.md
    └── diagramas/
        ├── atividades-alerta-epi.puml
        ├── casos-de-uso.puml
        └── classes.puml
```

## Como Visualizar os Diagramas

Os diagramas estão em formato PlantUML (`.puml`). Eles podem ser visualizados por extensões de IDE, como PlantUML para VS Code ou IntelliJ, ou renderizados por ferramentas compatíveis com PlantUML.
