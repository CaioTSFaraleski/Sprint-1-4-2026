package br.com.safefield.service;

import br.com.safefield.api.dto.Dtos.AlertaDTO;
import br.com.safefield.api.dto.Mapper;
import br.com.safefield.domain.Alerta;
import br.com.safefield.domain.StatusAlerta;
import br.com.safefield.domain.Usuario;
import br.com.safefield.repository.AlertaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;

/** Caixa de alertas de cada usuário (RF08 operador / RF09 supervisor). */
@Service
@Transactional
public class AlertaService {

    private final AlertaRepository alertas;

    public AlertaService(AlertaRepository alertas) {
        this.alertas = alertas;
    }

    @Transactional(readOnly = true)
    public List<AlertaDTO> meus(Usuario usuario, boolean incluirLidos) {
        EnumSet<StatusAlerta> status = incluirLidos
                ? EnumSet.of(StatusAlerta.ENVIADO, StatusAlerta.LIDO) : EnumSet.of(StatusAlerta.ENVIADO);
        return alertas.findByDestinatarioIdAndStatusInOrderByDataEnvioDesc(usuario.getId(), status).stream()
                .limit(50).map(Mapper::alerta).toList();
    }

    public AlertaDTO marcarComoLido(Long id, Usuario usuario) {
        Alerta a = alertas.findById(id).orElseThrow(() -> new NaoEncontradoException("Alerta", id));
        if (!a.getDestinatario().getId().equals(usuario.getId())) {
            throw new AcessoNegadoException("Este alerta é de outro usuário.");
        }
        a.marcarComoLido();
        return Mapper.alerta(a);
    }
}
