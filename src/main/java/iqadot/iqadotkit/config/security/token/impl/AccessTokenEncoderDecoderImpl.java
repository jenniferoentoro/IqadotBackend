package iqadot.iqadotkit.config.security.token.impl;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.*;
import io.jsonwebtoken.security.*;
import iqadot.iqadotkit.config.security.token.*;
import iqadot.iqadotkit.config.security.token.exception.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;

import java.security.*;
import java.time.*;
import java.time.temporal.*;
import java.util.*;

@Service
public class AccessTokenEncoderDecoderImpl implements AccessTokenEncoder, AccessTokenDecoder {
    private final Key key;




    public AccessTokenEncoderDecoderImpl() {
        byte[] keyBytes = Decoders.BASE64.decode("c7a04e8f8139cd222bf963f6ebc9fa1381834fd1de7a17e0c7f2f5391a685f93");
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String encode(AccessToken accessToken) {
        Map<String, Object> claimsMap = new HashMap<>();
//        if (!CollectionUtils.isEmpty(accessToken.getRoles())) {
//            claimsMap.put("roles", accessToken.getRoles());
//        }
        if (accessToken.getUserId() != null) {
            claimsMap.put("user_id", accessToken.getUserId());
        }

        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(accessToken.getSubject())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(30, ChronoUnit.MINUTES)))
                .addClaims(claimsMap)
                .signWith(key)
                .compact();
    }

    @Override
    public AccessToken decode(String accessTokenEncoded) {
        try {
            Jwt<?, Claims> jwt = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(accessTokenEncoded);
            Claims claims = jwt.getBody();

//            List<String> roles = claims.get("roles", List.class);
            Long userId = claims.get("user_id", Long.class);

            return new AccessTokenImpl(claims.getSubject(), userId);
        } catch (JwtException e) {
            throw new InvalidAccessTokenException(e.getMessage());
        }
    }
}
