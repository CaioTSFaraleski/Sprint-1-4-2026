package br.com.safefield.realtime;

import br.com.safefield.api.dto.Dtos.DeteccaoDTO;
import br.com.safefield.api.dto.Dtos.OcorrenciaResumoDTO;

/**
 * Evento publicado para os painéis conectados. Disparado só depois do commit da transação,
 * para que o painel nunca receba algo que acabou desfeito.
 */
public record EventoTempoReal(String tipo, OcorrenciaResumoDTO ocorrencia, DeteccaoDTO deteccao, String mensagem) {

    public static final String OCORRENCIA_ABERTA = "OCORRENCIA_ABERTA";
    public static final String OCORRENCIA_ATUALIZADA = "OCORRENCIA_ATUALIZADA";
    public static final String DETECCAO_CONFORME = "DETECCAO_CONFORME";

    Long areaId() {
        if (ocorrencia != null) return ocorrencia.areaId();
        return deteccao == null ? null : deteccao.areaId();
    }

    Long operadorId() {
        if (ocorrencia != null) return ocorrencia.operadorId();
        return deteccao == null ? null : deteccao.operadorId();
    }
}
