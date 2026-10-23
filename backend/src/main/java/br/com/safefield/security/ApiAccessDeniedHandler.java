package br.com.safefield.security;

import br.com.safefield.api.dto.Dtos.ErroDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper mapper;

    public ApiAccessDeniedHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException {
        response.setStatus(403);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getOutputStream(),
                new ErroDTO(403, "Acesso negado", "Seu perfil não tem permissão para esta ação.", List.of()));
    }
}
