package com.pharmacy.dispensing.auth;

import com.pharmacy.dispensing.common.security.JwtTokenProvider;
import com.pharmacy.dispensing.common.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationInMs", 900000L);
    }

    @Test
    void shouldGenerateAndValidateJwtToken() {
        UserPrincipal userPrincipal = new UserPrincipal(
                1L,
                "pharmacist",
                "pharmacist@pharmacy.com",
                "hashedpassword",
                "Dr. Sarah Jenkins",
                true,
                List.of(new SimpleGrantedAuthority("ROLE_PHARMACIST"))
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());

        String token = tokenProvider.generateAccessToken(auth);

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals("pharmacist", tokenProvider.getUsernameFromJWT(token));
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        assertFalse(tokenProvider.validateToken("invalid.jwt.token"));
    }
}
