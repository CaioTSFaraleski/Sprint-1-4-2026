package br.com.safefield.service;

import br.com.safefield.api.dto.Dtos.UsuarioDTO;
import br.com.safefield.api.dto.Mapper;
import br.com.safefield.api.dto.Requests.UsuarioRequest;
import br.com.safefield.domain.*;
import br.com.safefield.repository.AreaIndustrialRepository;
import br.com.safefield.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/** Cadastro de operadores, supervisores e gestores (RF01). */
@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final AreaIndustrialRepository areas;
    private final PasswordEncoder encoder;
    private final AuditoriaService auditoria;

    public UsuarioService(UsuarioRepository usuarios, AreaIndustrialRepository areas, PasswordEncoder encoder,
                          AuditoriaService auditoria) {
        this.usuarios = usuarios;
        this.areas = areas;
        this.encoder = encoder;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listar(Perfil perfil) {
        return usuarios.findAll().stream()
                .filter(u -> perfil == null || u.getPerfil() == perfil)
                .sorted(Comparator.comparing(Usuario::getPerfil).reversed().thenComparing(Usuario::getNome))
                .map(Mapper::usuario).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioDTO buscar(Long id) {
        return Mapper.usuario(usuarios.findById(id).orElseThrow(() -> new NaoEncontradoException("Usuário", id)));
    }

    public UsuarioDTO criar(UsuarioRequest req, Usuario autor) {
        String email = req.email().trim().toLowerCase();
        if (usuarios.existsByEmailIgnoreCase(email)) throw new NegocioException("E-mail já cadastrado.");
        if (usuarios.existsByMatricula(req.matricula().trim())) throw new NegocioException("Matrícula já cadastrada.");
        String hash = encoder.encode(req.senha());
        Usuario novo = switch (req.perfil()) {
            case OPERADOR -> {
                if (req.areaId() == null || req.turno() == null) {
                    throw new NegocioException("Operador precisa de área e turno.");
                }
                AreaIndustrial area = areas.findById(req.areaId())
                        .orElseThrow(() -> new NaoEncontradoException("Área", req.areaId()));
                yield new Operador(req.nome().trim(), req.matricula().trim(), email, hash, req.turno(),
                        req.funcao(), area);
            }
            case SUPERVISOR -> new SupervisorSeguranca(req.nome().trim(), req.matricula().trim(), email, hash);
            case GESTOR -> new GestorIndustrial(req.nome().trim(), req.matricula().trim(), email, hash,
                    req.unidadeIndustrial());
        };
        novo = usuarios.save(novo);
        auditoria.registrar(TipoEventoAuditoria.USUARIO_CRIADO, autor,
                novo instanceof Operador o ? o.getArea() : null, null, null,
                "Usuário cadastrado: " + novo.getNome() + " (" + novo.getPerfil() + ", matrícula "
                        + novo.getMatricula() + ").");
        return Mapper.usuario(novo);
    }
}
