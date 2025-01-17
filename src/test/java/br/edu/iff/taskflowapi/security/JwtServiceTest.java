package br.edu.iff.taskflowapi.security;

import br.edu.iff.taskflowapi.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private JwtDecoder jwtDecoder;

    @InjectMocks
    private JwtService jwtService;

    private final long expiry = 3600L;

    @BeforeEach
    void setUp() {
        // Manually inject the field
        ReflectionTestUtils.setField(jwtService, "expiry", expiry);
    }

    @Test
    void generateToken_withValidUser() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");

        Jwt mockJwt = new Jwt(
            "mock-token-value",
            Instant.now(),
            Instant.now().plusSeconds(expiry),
            Map.of("alg", "HS256"),
            Map.of("sub", user.getEmail())
        );

        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(mockJwt);

        // When
        String token = jwtService.generateToken(user);

        // Then
        assertThat(token).isNotEmpty().isEqualTo(mockJwt.getTokenValue());

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());

        JwtEncoderParameters passedParameters = captor.getValue();
        JwtClaimsSet claimsSet = passedParameters.getClaims();

        assertThat(claimsSet.getSubject()).isEqualTo("test@example.com");
        assertThat(claimsSet.getExpiresAt()).isNotNull();
        assertThat(claimsSet.getIssuedAt()).isNotNull();
    }

    @Test
    void generateToken_userWithoutEmail() {
        // Given
        User user = new User();
        user.setEmail(null);

        // When / Then
        assertThatThrownBy(() -> jwtService.generateToken(user))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("User email cannot be null");
    }

    @Test
    void getEmailFromToken_withValidToken() {
        // Given
        String token = "valid-jwt";
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(3600);
        String subject = "test@example.com";

        Jwt mockJwt = new Jwt(
            token,
            now,
            expiry,
            Map.of("alg", "HS256"),
            Map.of("sub", subject)
        );
        when(jwtDecoder.decode(token)).thenReturn(mockJwt);

        // When
        String email = jwtService.getEmailFromToken(token);

        // Then
        assertThat(email).isEqualTo(subject);
    }

    @Test
    void getEmailFromToken_withInvalidToken() {
        // Given
        String invalidToken = "invalid-jwt";

        when(jwtDecoder.decode(invalidToken))
            .thenThrow(new JwtException("Invalid JWT"));

        // When / Then
        assertThatThrownBy(() -> jwtService.getEmailFromToken(invalidToken))
            .isInstanceOf(JwtException.class)
            .hasMessage("Invalid JWT");
    }

    @Test
    void getEmailFromToken_withNullToken() {
        // Given
        String nullToken = null;

        // When / Then
        assertThatThrownBy(() -> jwtService.getEmailFromToken(nullToken))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Token cannot be null");
    }
}
