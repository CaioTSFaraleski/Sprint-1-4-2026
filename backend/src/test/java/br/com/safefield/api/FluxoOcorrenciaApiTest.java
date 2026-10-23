package br.com.safefield.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Fluxo crítico do diagrama de atividades, de ponta a ponta pela API:
 * detecção do gateway -> ocorrência + alertas -> regularização do operador -> validação do supervisor.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"safefield.simulador.ativo=false", "safefield.seed.dias-historico=3"})
class FluxoOcorrenciaApiTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private String login(String email) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"safefield123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(body).get("token").asText();
    }

    @Test
    void deteccaoSemEpiAbreOcorrenciaQuePassaPorRegularizacaoEValidacao() throws Exception {
        String supervisor = login("supervisor@metaindustria.com.br");
        String operador = login("operador@metaindustria.com.br");

        JsonNode marcos = json.readTree(mvc.perform(get("/api/v1/auth/me").header("Authorization", operador))
                .andReturn().getResponse().getContentAsString());
        long operadorId = marcos.get("id").asLong();
        long areaId = marcos.get("areaId").asLong();

        // edge gateway envia detecção sem luvas e sem óculos (Montagem Final exige luvas, óculos e botina)
        String deteccao = mvc.perform(post("/api/v1/deteccoes").header("X-Gateway-Key", "gateway-dev-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"operadorId\":" + operadorId + ",\"areaId\":" + areaId
                                + ",\"episDetectados\":[\"BOTA\"],\"confianca\":0.93,\"dispositivo\":\"CAM-TESTE\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.conforme").value(false))
                .andReturn().getResponse().getContentAsString();
        long ocorrenciaId = json.readTree(deteccao).get("ocorrenciaId").asLong();

        mvc.perform(get("/api/v1/ocorrencias/" + ocorrenciaId).header("Authorization", supervisor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.status").value("ABERTA"))
                .andExpect(jsonPath("$.resumo.episAusentes.length()").value(2))
                .andExpect(jsonPath("$.alertas.length()").value(2))
                // abertura + alerta emitido, sem duplicar por EPI ausente
                .andExpect(jsonPath("$.acoes.length()").value(2));

        // operador não pode validar
        mvc.perform(post("/api/v1/ocorrencias/" + ocorrenciaId + "/validacao").header("Authorization", operador)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"aprovada\":true}"))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/v1/ocorrencias/" + ocorrenciaId + "/regularizacao").header("Authorization", operador)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"observacao\":\"Coloquei as luvas e os óculos\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.status").value("AGUARDANDO_VALIDACAO"));

        // rejeitar sem motivo é recusado (regra de negócio)
        mvc.perform(post("/api/v1/ocorrencias/" + ocorrenciaId + "/validacao").header("Authorization", supervisor)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"aprovada\":false}"))
                .andExpect(status().isUnprocessableEntity());

        mvc.perform(post("/api/v1/ocorrencias/" + ocorrenciaId + "/validacao").header("Authorization", supervisor)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"aprovada\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.status").value("ENCERRADA"));

        String trilha = mvc.perform(get("/api/v1/auditoria").param("ocorrenciaId", String.valueOf(ocorrenciaId))
                        .header("Authorization", supervisor))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(trilha).contains("OCORRENCIA_CRIADA", "ALERTA_EMITIDO", "REGULARIZACAO_REGISTRADA",
                "VALIDACAO_APROVADA", "OCORRENCIA_ENCERRADA");
    }

    @Test
    void deteccaoComTodosOsEpisNaoAbreOcorrencia() throws Exception {
        String supervisor = login("supervisor@metaindustria.com.br");
        String operador = login("operador@metaindustria.com.br");
        JsonNode marcos = json.readTree(mvc.perform(get("/api/v1/auth/me").header("Authorization", operador))
                .andReturn().getResponse().getContentAsString());
        mvc.perform(post("/api/v1/deteccoes").header("Authorization", supervisor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"operadorId\":" + marcos.get("id").asLong() + ",\"areaId\":" + marcos.get("areaId").asLong()
                                + ",\"episDetectados\":[\"BOTA\",\"LUVAS\",\"OCULOS\"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.conforme").value(true))
                .andExpect(jsonPath("$.ocorrenciaId").doesNotExist());
    }

    @Test
    void controleDeAcessoPorPerfil() throws Exception {
        mvc.perform(get("/api/v1/dashboard")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/deteccoes").header("X-Gateway-Key", "chave-errada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"operadorId\":1,\"areaId\":1,\"episDetectados\":[]}"))
                .andExpect(status().isForbidden());

        String operador = login("operador@metaindustria.com.br");
        mvc.perform(get("/api/v1/dashboard").header("Authorization", operador)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/alertas").header("Authorization", operador)).andExpect(status().isOk());

        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"gestor@metaindustria.com.br\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void dashboardERelatorioRefletemOHistorico() throws Exception {
        String gestor = login("gestor@metaindustria.com.br");
        mvc.perform(get("/api/v1/dashboard").header("Authorization", gestor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertasAtivos").isNumber())
                .andExpect(jsonPath("$.areas.length()").value(6))
                .andExpect(jsonPath("$.serie7d.length()").value(7));

        mvc.perform(get("/api/v1/relatorios/conformidade").header("Authorization", gestor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDeteccoes").isNumber())
                .andExpect(jsonPath("$.porArea").isArray());

        String csv = mvc.perform(get("/api/v1/relatorios/conformidade.csv").header("Authorization", gestor))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(csv).contains("Relatório de Conformidade", "Índice de conformidade");
    }

    @Test
    void cadastroDeAreaERegraFicaNaTrilhaDeAuditoria() throws Exception {
        String gestor = login("gestor@metaindustria.com.br");
        String area = mvc.perform(post("/api/v1/areas").header("Authorization", gestor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Laboratório de Testes\",\"setor\":\"Qualidade\",\"nivelRisco\":\"MEDIO\",\"turnos\":[\"A\"]}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long areaId = json.readTree(area).get("id").asLong();
        long epiId = json.readTree(mvc.perform(get("/api/v1/epis").header("Authorization", gestor))
                .andReturn().getResponse().getContentAsString()).get(0).get("id").asLong();

        mvc.perform(post("/api/v1/areas/" + areaId + "/regras").header("Authorization", gestor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"epiId\":" + epiId + ",\"descricao\":\"Uso obrigatório\",\"severidadePadrao\":\"ALTO\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.regras.length()").value(1));

        String trilha = mvc.perform(get("/api/v1/auditoria").param("areaId", String.valueOf(areaId))
                        .header("Authorization", gestor))
                .andReturn().getResponse().getContentAsString();
        assertThat(trilha).contains("AREA_CRIADA", "REGRA_CRIADA");
    }
}
