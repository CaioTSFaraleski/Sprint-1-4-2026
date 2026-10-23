package br.com.safefield.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Canal WebSocket /ws/alertas. Cada conexão pode filtrar o que recebe pela query string:
 * ?areaId=3 (painel de uma área), ?operadorId=12 (tablet do operador) ou nada (painel geral).
 * Equivale aos canais ws/supervisor/{areaId} e ws/operador/{operadorId} da arquitetura da Sprint 3.
 */
@Component
public class AlertasWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(AlertasWebSocketHandler.class);

    private record Assinatura(WebSocketSession sessao, Long areaId, Long operadorId) {}

    private final Map<String, Assinatura> sessoes = new ConcurrentHashMap<>();
    private final ObjectMapper mapper;

    public AlertasWebSocketHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        var params = UriComponentsBuilder.fromUri(session.getUri()).build().getQueryParams();
        Long areaId = parse(params.getFirst("areaId"));
        Long operadorId = parse(params.getFirst("operadorId"));
        var segura = new ConcurrentWebSocketSessionDecorator(session, 5_000, 256 * 1024);
        sessoes.put(session.getId(), new Assinatura(segura, areaId, operadorId));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessoes.remove(session.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publicar(EventoTempoReal evento) {
        if (sessoes.isEmpty()) return;
        String json;
        try {
            json = mapper.writeValueAsString(evento);
        } catch (IOException e) {
            log.warn("Falha ao serializar evento em tempo real", e);
            return;
        }
        TextMessage mensagem = new TextMessage(json);
        for (Assinatura a : sessoes.values()) {
            if (!interessa(a, evento)) continue;
            try {
                a.sessao().sendMessage(mensagem);
            } catch (Exception e) {
                sessoes.remove(a.sessao().getId());
            }
        }
    }

    public int conexoesAtivas() {
        return sessoes.size();
    }

    private boolean interessa(Assinatura a, EventoTempoReal e) {
        if (a.areaId() != null && !a.areaId().equals(e.areaId())) return false;
        return a.operadorId() == null || a.operadorId().equals(e.operadorId());
    }

    private static Long parse(String valor) {
        try {
            return valor == null || valor.isBlank() ? null : Long.valueOf(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
