# Diagramas UML

Os fontes estão em PlantUML (`.puml`) e as imagens renderizadas em [`png/`](png). Para regenerar:
`java -jar plantuml.jar -Playout=smetana -tpng -o png *.puml`

## Sprint 1 · Modelagem

| Diagrama | Fonte | Imagem |
|---|---|---|
| Casos de uso | [casos-de-uso.puml](casos-de-uso.puml) | [png](png/casos-de-uso.png) |
| Atividades: detecção de EPI e emissão de alerta | [atividades-alerta-epi.puml](atividades-alerta-epi.puml) | [png](png/atividades-alerta-epi.png) |
| Classes (modelo conceitual) | [classes.puml](classes.puml) | [png](png/classes.png) |

## Sprint 4 · Solução implementada

| Diagrama | Fonte | Imagem |
|---|---|---|
| Sequência: detecção sem EPI, alerta em tempo real e validação | [sequencia-deteccao-alerta.puml](sequencia-deteccao-alerta.puml) | [png](png/sequencia-deteccao-alerta.png) |
| Classes implementadas (entidades JPA) | [classes-implementadas.puml](classes-implementadas.puml) | [png](png/classes-implementadas.png) |
| Implantação (Docker Compose) | [implantacao.puml](implantacao.puml) | [png](png/implantacao.png) |

O diagrama de atividades da Sprint 1 foi implementado tal como desenhado: as etapas aparecem na linha do tempo do
detalhe da ocorrência, e as decisões ("existe EPI ausente?", "risco crítico ou reincidente?", "regularização
validada?") correspondem a `MotorRegrasService.avaliar`, `OcorrenciaService.escalar` e
`OcorrenciaService.validar`.

![Sequência](png/sequencia-deteccao-alerta.png)
