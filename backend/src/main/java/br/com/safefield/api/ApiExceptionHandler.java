package br.com.safefield.api;

import br.com.safefield.api.dto.Dtos.ErroDTO;
import br.com.safefield.service.AcessoNegadoException;
import br.com.safefield.service.AuthService;
import br.com.safefield.service.NaoEncontradoException;
import br.com.safefield.service.NegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NaoEncontradoException.class)
    public ResponseEntity<ErroDTO> naoEncontrado(NaoEncontradoException e) {
        return erro(HttpStatus.NOT_FOUND, "Não encontrado", e.getMessage(), List.of());
    }

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ErroDTO> negocio(NegocioException e) {
        return erro(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negócio", e.getMessage(), List.of());
    }

    @ExceptionHandler(AuthService.CredenciaisInvalidasException.class)
    public ResponseEntity<ErroDTO> credenciais(AuthService.CredenciaisInvalidasException e) {
        return erro(HttpStatus.UNAUTHORIZED, "Não autenticado", e.getMessage(), List.of());
    }

    @ExceptionHandler({AcessoNegadoException.class, AccessDeniedException.class})
    public ResponseEntity<ErroDTO> acessoNegado(RuntimeException e) {
        String msg = e instanceof AcessoNegadoException ? e.getMessage() : "Seu perfil não tem permissão para esta ação.";
        return erro(HttpStatus.FORBIDDEN, "Acesso negado", msg, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroDTO> validacao(MethodArgumentNotValidException e) {
        List<String> detalhes = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage()).toList();
        return erro(HttpStatus.BAD_REQUEST, "Dados inválidos", "Confira os campos destacados.", detalhes);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErroDTO> formato(Exception e) {
        return erro(HttpStatus.BAD_REQUEST, "Dados inválidos", "Requisição em formato inválido.", List.of());
    }

    private static ResponseEntity<ErroDTO> erro(HttpStatus status, String erro, String mensagem, List<String> detalhes) {
        return ResponseEntity.status(status).body(new ErroDTO(status.value(), erro, mensagem, detalhes));
    }
}
