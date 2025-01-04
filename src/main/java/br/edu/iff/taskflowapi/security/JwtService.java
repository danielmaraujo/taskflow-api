package br.edu.iff.taskflowapi.security;

import br.edu.iff.taskflowapi.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {

    private final JwtEncoder encoder;

    @Value("${jwt.expiration}")
    private long expiry;

    public JwtService(JwtEncoder encoder) {
        this.encoder = encoder;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("spring-security-jwt")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expiry))
            .subject(user.getEmail())
            .build();

        return encoder.encode(
                JwtEncoderParameters.from(claims))
            .getTokenValue();
    }
}
