package com.sandeep.api_gateway.filter;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;


//this filter applies to all except api/v1/auth

//FLOW:
 //1. Extract JWT from authentication header.
 //2. Validate JWT signature and expiry.
 //3. Extract userId from toke claims.
 //4. Add userId to request header for downstream services
 //5. Forward request to correct service

// If JWT is invalid or missing --> 401 unauthorized
@Component
@Slf4j
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    @Value("${jwt.secret-key}")
    private String secretKey;

      public JwtAuthFilter(){
          super(Config.class);
      }
    @Override
    public GatewayFilter apply(JwtAuthFilter.Config config) {
        return (exchange, chain) -> {
              String authHeader=exchange.getRequest()
                      .getHeaders()
                      .getFirst(HttpHeaders.AUTHORIZATION);


              if(authHeader==null || !authHeader.startsWith("Bearer ")){
                  log.warn("Missing or invalid Authorization Header");
                  return unauthorized(exchange);
              }

              String token=authHeader.substring(7);

              try{

                  Claims claims=extractClaims(token);

                  String userId=claims.get("userId",String.class);
                  String email=claims.getSubject();

                  log.info("JWT Validated for user: {}",userId);

                  //Add userId to request header for downstream services
                    ServerWebExchange modifiedExchange= exchange.mutate()
                            .request(r->r.header("X-User-Id",userId))
                            .build();

                    return chain.filter(modifiedExchange);
              } catch (Exception e) {
                 log.error("JWT Validation failed: {}",e.getMessage());
                 return unauthorized(exchange);
              }
        };
    }

    private Claims extractClaims(String token){

          return Jwts.parser()
                  .setSigningKey(getSignInKey())
                  .build()
                  .parseClaimsJws(token)
                  .getBody();
    }

    private Key getSignInKey(){

          byte[] keyBytes= Decoders.BASE64.decode(secretKey);
          return Keys.hmacShaKeyFor(keyBytes);

    }


    private Mono<Void> unauthorized(ServerWebExchange exchange){
          exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
          return exchange.getResponse().setComplete();

    }

    public static class Config{
        //for filter factory
    }
}
