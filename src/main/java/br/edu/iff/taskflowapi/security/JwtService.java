package br.edu.iff.taskflowapi.security;

import br.edu.iff.taskflowapi.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;

@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;

    @Value("${jwt.expiration}")
    private long expiry;

    public JwtService(JwtEncoder encoder, JwtDecoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public String generateToken(User user) {
        Objects.requireNonNull(user.getEmail(), "User email cannot be null");

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

    public String getEmailFromToken(String token){
        Objects.requireNonNull(token, "Token cannot be null");

        if(token.startsWith("Bearer")){
            token = token.substring(7);
        }

        return decoder.decode(token).getSubject();
    }
}
