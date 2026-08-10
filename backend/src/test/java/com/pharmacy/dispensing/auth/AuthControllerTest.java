package com.pharmacy.dispensing.auth;

import com.pharmacy.dispensing.auth.dto.AuthResponse;
import com.pharmacy.dispensing.auth.dto.LoginRequest;
import com.pharmacy.dispensing.auth.dto.UserDto;
import com.pharmacy.dispensing.auth.service.AuthService;
import com.pharmacy.dispensing.common.security.JwtTokenProvider;
import com.pharmacy.dispensing.common.security.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void shouldLoginSuccessfullyAndReturnJwtTokens() throws Exception {
        LoginRequest loginRequest = new LoginRequest("pharmacist", "Password123!");
        UserDto userDto = new UserDto(1L, "pharmacist", "pharmacist@pharmacy.com", "Dr. Sarah Jenkins", true, Set.of("ROLE_PHARMACIST"));
        AuthResponse authResponse = new AuthResponse("mock.access.token", "mock-refresh-token-uuid", 900000L, userDto);

        when(authService.login(any(LoginRequest.class), any())).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock.access.token"))
                .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token-uuid"))
                .andExpect(jsonPath("$.user.username").value("pharmacist"));
    }
}
