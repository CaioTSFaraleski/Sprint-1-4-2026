package br.com.safefield.api;

import br.com.safefield.domain.NivelRisco;
import br.com.safefield.domain.TipoEPI;
import br.com.safefield.domain.Turno;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/referencias")
@Tag(name = "Referências")
public class ReferenciaController {

    public record Opcao(String valor, String descricao) {}

    @GetMapping
    @Operation(summary = "Listas de valores usadas nos formulários (tipos de EPI, níveis de risco, turnos)")
    public Map<String, List<Opcao>> referencias() {
        return Map.of(
                "tiposEpi", Arrays.stream(TipoEPI.values()).map(t -> new Opcao(t.name(), t.getDescricao())).toList(),
                "niveisRisco", Arrays.stream(NivelRisco.values()).map(n -> new Opcao(n.name(), n.name())).toList(),
                "turnos", List.of(new Opcao(Turno.A.name(), "A (06h-14h)"), new Opcao(Turno.B.name(), "B (14h-22h)"),
                        new Opcao(Turno.C.name(), "C (22h-06h)")));
    }
}
