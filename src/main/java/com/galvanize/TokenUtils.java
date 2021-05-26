package com.galvanize;

import com.galvanize.security.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TokenUtils {

    JwtProperties jwtProperties;

    public TokenUtils(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public TokenDetails getTokenDetails(String token){

        if(token!=null && token.startsWith("Bearer")) {
            token = token.replace("Bearer", "");
        } else {
            return null;
        }

        Claims claims = getTokenClaims(token);
        TokenDetails details = new TokenDetails();
        details.setGuid((Integer)claims.get("guid"));
        details.setUsername(claims.getSubject());
        details.setEmail((String)claims.get("email"));
        List<String> authorities = (List<String>) claims.get("authorities");
        details.setAuthorities(authorities);

        return details;
    }

    private Claims getTokenClaims(String token) {

        return Jwts.parser()
                .setSigningKey(jwtProperties.getSecret().getBytes())
                .parseClaimsJws(token)
                .getBody();

    }
}
