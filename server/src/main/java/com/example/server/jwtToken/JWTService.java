package com.example.server.jwtToken;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JWTService {
    //TODO: Make the JWT_KEY and Expiry hidden somewhere
    private static final String JWT_KEY = "dL9nLwtKCjFfgj9gwuhJwqw4R4iepkfzC5XGdLfpyFTKuWDcpGCeZDBghzitHUjn";
    private static final Integer JWT_EXPIRY_DATE = 604800000;

    private final String encodedKey = Base64.getEncoder().encodeToString(JWT_KEY.getBytes(StandardCharsets.UTF_8));
    private final SecretKey key = new SecretKeySpec(encodedKey.getBytes(StandardCharsets.UTF_8), 0, encodedKey.length(), "HmacSHA256");

    //Creating a JWTToken for a user
    public String createJwt(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRY_DATE))
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }

    //Decoding a JWT Token of a Request
    public Map<String, String> decodeJWT(String jwts){
        Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(jwts).getBody();

        String subject = claims.getSubject();
        String role = claims.get("role", String.class);

        Map<String, String> result = new HashMap<>();
        result.put("subject", subject);
        result.put("role", role);

        return result;
    }
}
