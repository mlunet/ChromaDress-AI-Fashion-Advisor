package com.app.ChromaDress.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

  @Value("${jwt.secret}")
  private String secretString;
  @Value("${jwt.access-token-expiration}")
  private Duration accessTokenExpiration;
  @Value("${jwt.refresh-token-expiration}")
  private Duration refreshTokenExpiration;


  public String generateToken(String username) {
    return buildToken(username, accessTokenExpiration);
  }

  public String generateRefreshToken(String username) {
    return buildToken(username, refreshTokenExpiration);
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public boolean validateToken(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretString));
  }

  private String buildToken(String username, Duration expiration) {
    return Jwts.builder().subject(username).issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expiration.toMillis()))
        .signWith(getSigningKey()).compact();
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
  }
}
