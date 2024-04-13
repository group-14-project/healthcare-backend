package com.example.server.jwtToken;


import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JWTService {
    //TODO: Make the JWT_KEY and Expiry hidden somewhere
    private static final String JWT_KEY = "dL9nLwtKCjFfgj9gwuhJwqw4R4iepkfzC5XGdLfpyFTKuWDcpGCeZDBghzitHUjn";
    private static final Integer JWT_EXPIRY_DATE = 86400000;

    private final String encodedKey = Base64.getEncoder().encodeToString(JWT_KEY.getBytes(StandardCharsets.UTF_8));
    private final SecretKey key = new SecretKeySpec(encodedKey.getBytes(StandardCharsets.UTF_8), 0, encodedKey.length(), "HmacSHA256");

    //Creating a JWTToken for a user
    public String createJwt(String email, String role) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", email); // Subject
        claims.put("role", role);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRY_DATE))
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();

    }

    //Decoding a JWT Token of a Request
    public Map<String, String> decodeJWT(String jwtToken){
        Map<String, String> result = new HashMap<>();

        try {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(key).parseClaimsJws(jwtToken);

            Claims claims = claimsJws.getBody();

            String subject = claims.getSubject();
            String role = claims.get("role", String.class);
            Date expirationDate = claims.getExpiration();

            boolean isExpired = expirationDate != null && expirationDate.before(new Date());

            result.put("role", role);
            result.put("email", subject);
            result.put("isExpired", String.valueOf(isExpired));

        } catch (ExpiredJwtException e) {
            // Token has expired
            result.put("role", null);
            result.put("email", null);
            result.put("isExpired", "true");

        } catch (Exception e) {
            // Exception occurred, likely due to token parsing error
            result.put("role", null);
            result.put("email", null);
            result.put("isExpired", "false");
        }

        return result;
    }

    public String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
