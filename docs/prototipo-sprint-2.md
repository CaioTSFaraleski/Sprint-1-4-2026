# Prototipo Navegavel - Sprint 2

Arquivo Figma:
https://www.figma.com/design/d6wRQPPhz9r50kVu5zJFOe/SafeField-AI--Metaind%C3%BAstria-Dashboard?node-id=0-1&t=gfM8x4VZIW0N3Z67-1

## Objetivo

O prototipo high-fidelity da Sprint 2 refina a experiencia do SafeField AI para uso em tablet industrial, mantendo coerencia com a modelagem da Sprint 1. O foco esta nos fluxos de consulta e cadastro de EPI, emissao e acompanhamento de alertas, validacao de ocorrencias pelo supervisor e geracao de relatorios de conformidade por setor.

## Telas Criadas ou Refinadas

- Login
- Dashboard de Monitoramento - Sprint 2
- Colaboradores e EPIs
- Cadastro de EPI
- Sucesso de cadastro de EPI
- Alertas e Ocorrencias
- Detalhe da Ocorrencia
- Sucesso de validacao
- Relatorios de Conformidade
- Relatorio gerado com sucesso
- Documentacao UX/UI

## Fluxos Cobertos

### Cadastro e consulta de EPI por colaborador

Este fluxo usa as telas **Colaboradores e EPIs**, **Cadastro de EPI** e **Sucesso de cadastro de EPI**.

Na tela de colaboradores, o supervisor pode buscar por nome, matricula ou setor, consultar a lista de colaboradores e abrir o detalhe de Joao Almeida. O painel do colaborador mostra:

- EPIs obrigatorios: capacete, oculos de protecao, luvas, protetor auricular e bota de seguranca.
- EPIs detectados: capacete, luvas e bota de seguranca.
- EPIs pendentes: oculos de protecao e protetor auricular.

O cadastro de EPI contem os campos de nome, tipo, codigo CA, validade, setor obrigatorio, colaborador vinculado, status e observacoes.

### Emissao e visualizacao de alerta de risco

Este fluxo usa as telas **Dashboard de Monitoramento**, **Alertas e Ocorrencias**, **Detalhe da Ocorrencia** e **Sucesso de validacao**.

A tela de alertas apresenta filtros por todos, criticos, em validacao e regularizados. Cada alerta exibe colaborador, setor, EPI ausente, horario, origem por camera/sensor e status.

O detalhe da ocorrencia segue o fluxo de atividades da Sprint 1:

1. Deteccao automatica recebida.
2. Regra de seguranca verificada.
3. Nao conformidade identificada.
4. Alerta emitido ao operador.
5. Aguardando regularizacao.
6. Validacao do supervisor.
7. Ocorrencia encerrada.

### Geracao de relatorio de conformidade por setor

Este fluxo usa as telas **Relatorios de Conformidade** e **Relatorio gerado com sucesso**.

A tela de relatorios permite filtrar por setor, periodo, tipo de EPI e severidade. Ela apresenta cards de taxa de conformidade, total de ocorrencias, ocorrencias criticas e tempo medio de regularizacao, alem de graficos e resumo por setor.

### Documentacao UX/UI no proprio prototipo

A tela **Documentacao UX/UI** registra o mapa de telas, as decisoes de design e o mapeamento com os casos de uso e classes da Sprint 1.

## Interacoes Configuradas no Figma

- Botao Entrar: Login para Dashboard.
- Menu Dashboard: navega para Dashboard.
- Menu Colaboradores e EPIs: navega para Colaboradores e EPIs.
- Menu Alertas: navega para Alertas e Ocorrencias.
- Menu Relatorios: navega para Relatorios de Conformidade.
- Menu Documentacao UX/UI: navega para Documentacao UX/UI.
- Card Alertas ativos: Dashboard para Alertas e Ocorrencias.
- Botao Cadastrar novo EPI: Colaboradores e EPIs para Cadastro de EPI.
- Botao Salvar EPI: Cadastro de EPI para Sucesso de cadastro de EPI.
- Tela de sucesso de cadastro: retorna para Colaboradores e EPIs.
- Alerta critico de Joao Almeida: Alertas para Detalhe da Ocorrencia.
- Botao Validar regularizacao: Detalhe da Ocorrencia para Sucesso de validacao.
- Botao Gerar relatorio: Relatorios para Relatorio gerado com sucesso.
- Botoes de voltar/cancelar: retornam para a tela anterior correspondente.

Observacao: os itens de menu que apontariam para a propria tela ativa foram deixados sem navegacao redundante, pois o Figma rejeita conexoes de um frame para ele mesmo.

## Decisoes de UX

- Interface pensada para tablet industrial.
- Botoes grandes para uso em campo e com luvas.
- Alto contraste e leitura rapida.
- Cores de severidade: verde para conforme, laranja para atencao e vermelho para critico.
- Dashboard prioriza KPIs, alertas ativos e setores criticos.
- Fluxo de alerta segue o processo da Sprint 1: deteccao automatica, alerta, regularizacao, validacao do supervisor e encerramento.
- Relatorios permitem analise por setor para gestores industriais.

## Mapeamento com Casos de Uso da Sprint 1

- Cadastro e consulta de EPI: Colaboradores e EPIs + Cadastro de EPI.
- Emissao e visualizacao de alerta: Alertas + Detalhe da Ocorrencia.
- Geracao de relatorio de conformidade: Relatorios.
- Monitoramento geral: Dashboard.

## Mapeamento com Classes da Sprint 1

- Usuario, Operador, SupervisorSeguranca e GestorIndustrial.
- AreaIndustrial, EPI e RegraSeguranca.
- DeteccaoEPI, OcorrenciaRisco e Alerta.
- RelatorioConformidade e Dashboard.
