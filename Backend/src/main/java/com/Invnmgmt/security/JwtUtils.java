package com.Invnmgmt.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;

@Service
public class JwtUtils {

	 private static final long EXPIRATION_TIME_IN_MILLISEC = 1000L * 60 * 60 * 24 * 30 * 6;

	    private SecretKey key;

	    @Value("${jwt.secret}") // Make sure this matches your properties file
	    private String jwtSecret;

	    @PostConstruct
	    public void init() {
	        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
	        this.key = new SecretKeySpec(keyBytes, "HmacSHA256");
	    }

	    public String generateToken(String email) {
	        return Jwts.builder()
	                .subject(email)
	                .issuedAt(new Date(System.currentTimeMillis()))
	                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_IN_MILLISEC))
	                .signWith(key)
	                .compact();
	    }

	    public String getUsernameFromToken(String token) {
	        return extractClaim(token, Claims::getSubject);
	    }

	    public boolean isTokenValid(String token, UserDetails userDetails) {
	        final String username = getUsernameFromToken(token);
	        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
	    }

	    private boolean isTokenExpired(String token) {
	        Date expiration = extractClaim(token, Claims::getExpiration);
	        return expiration.before(new Date());
	    }

	    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
	        Claims claims = Jwts.parser()
	                .verifyWith(key)
	                .build()
	                .parseSignedClaims(token)
	                .getPayload();
	        return claimsResolver.apply(claims);
	    }
	
}
