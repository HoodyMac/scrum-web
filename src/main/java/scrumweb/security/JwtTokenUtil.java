package scrumweb.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static scrumweb.common.ApplicationConstants.ACCESS_TOKEN_VALIDITY_SECONDS;
import static scrumweb.common.ApplicationConstants.SIGNING_KEY;

@Service
public class JwtTokenUtil {

    @Value("6048000")
    private Long expiration;
    private static final String CREATED_AT = "created_at";

    public String getUsernameFromToken(String token) {
        try {
            final Claims claims = getClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public Date getCreatedDateFromToken(String token) {
        try {
            final Claims claims = getClaimsFromToken(token);
            return new Date((Long) claims.get(CREATED_AT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public Date getExpirationDateFromToken(String token) {
        try {
            final Claims claims = getClaimsFromToken(token);
            return claims.getExpiration();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(SIGNING_KEY)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        claims.put("sub", userDetails.getUsername());
        claims.put(CREATED_AT, new Date());

        return doGenerateToken(claims);
    }

    private String doGenerateToken(Map<String, Object> claims) {
        final Date createdDate = (Date) claims.get(CREATED_AT);
        final Date expirationDate = new Date(createdDate.getTime() + ACCESS_TOKEN_VALIDITY_SECONDS);

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, SIGNING_KEY)
                .compact();
    }

    public Boolean canTokenBeRefreshed(String token) {
        return !isTokenExpired(token);
    }

    public String refreshToken(String token) {
        final Claims claims = getClaimsFromToken(token);
        claims.put(CREATED_AT, new Date());
        return doGenerateToken(claims);
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        JwtUser user = (JwtUser) userDetails;

        final String username = getUsernameFromToken(token);

        return username.equals(user.getUsername())
                && !isTokenExpired(token);
    }

}
