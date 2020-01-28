package sb.jafu.app.security;

import io.jsonwebtoken.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import sb.jafu.app.handler.error.CommonErrorResponse;
import sb.jafu.app.handler.error.CommonErrorResponseBuilder;
import sb.jafu.app.handler.error.ErrorResultUtil;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author SAROY on 1/24/2020
 */
public class SecurityHandlerFunction {

    private static final List<String> clients = Collections.unmodifiableList(Arrays.asList("xc-api-gateway", "xc-auth-server"));

    @SuppressWarnings("unchecked")
    public static HandlerFilterFunction<ServerResponse, ServerResponse> hasAnyAuth(String... scopes) {
        return (request, next) -> {
            // check for authentication headers
            if (scopes.length == 0) {
                return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            } else {
                List<String> auth = request.headers().header("Authorization");
                List<String> idToken = request.headers().header("X-IDTOKEN");
                if (auth.size() == 1 && idToken.size() == 1) {
                    Claims accessTokenClaims = getAccessTokenClaims(auth.get(0));
                    Claims idTokenClaims = getIdTokenClaims(idToken.get(0));
                    if (isAnyAuthorized((List<String>) accessTokenClaims.get("scope"), scopes)) {
                        JafuJwtAuthentication authentication = new JafuJwtAuthentication(idToken.get(0), idTokenClaims, accessTokenClaims);
                        request.servletRequest().setAttribute("authentication", authentication);
                        return next.handle(request);
                    } else {
                        return ServerResponse.status(HttpStatus.UNAUTHORIZED).body(getUnAuthorizedResponse(request.path()));
                    }
                }
                return ServerResponse.status(HttpStatus.UNAUTHORIZED).body(getUnAuthorizedResponse(request.path()));
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static HandlerFilterFunction<ServerResponse, ServerResponse> hasAllAuth(String... scopes) {
        return (request, next) -> {
            // check for authentication headers
            if (scopes.length == 0) {
                return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            } else {
                List<String> auth = request.headers().header("Authorization");
                List<String> idToken = request.headers().header("X-IDTOKEN");
                if (auth.size() != 1 && idToken.size() != 1) {
                    return ServerResponse.status(HttpStatus.UNAUTHORIZED).body(getUnAuthorizedResponse(request.path()));
                }
                Claims accessTokenClaims = getAccessTokenClaims(auth.get(0));
                Claims idTokenClaims = getIdTokenClaims(idToken.get(0));
                if (isAllAuthorized((List<String>) accessTokenClaims.get("scope"), scopes)) {
                    JafuJwtAuthentication authentication = new JafuJwtAuthentication(idToken.get(0), idTokenClaims, accessTokenClaims);
                    return next.handle(ServerRequest.from(request).attribute("authentication", authentication).build());
                } else {
                    return ServerResponse.status(HttpStatus.UNAUTHORIZED).body(getUnAuthorizedResponse(request.path()));
                }
            }
        };
    }

    private static boolean isAnyAuthorized(List<String> grantedScopes, String [] scopes) {
        if (scopes.length == 0 || grantedScopes.isEmpty()) {
            return false;
        } else {
            return Arrays.stream(scopes).anyMatch(grantedScopes::contains);
        }
    }

    private static boolean isAllAuthorized(List<String> grantedScopes, String [] scopes) {
        if (scopes.length == 0 || grantedScopes.isEmpty()) {
            return false;
        } else {
            return Arrays.stream(scopes).allMatch(grantedScopes::contains);
        }
    }

    private static Claims getAccessTokenClaims(String authorization) {
        //boolean accessTokenSigned = Jwts.parser().isSigned(authorization);
        Claims accessTokenClaims = validateAndParseJwtClaims(authorization);
        validateOtherTokenDetails(accessTokenClaims);
        return accessTokenClaims;
    }

    private static Claims getIdTokenClaims(String xIdToken) {
        return validateAndParseJwtClaims(xIdToken);
    }

   /* *//**
     * Performs signature check, issuer check, expiration check and parses
     * @param rawToken
     * @return
     *//*
    private static Claims validateAndParseJwsClaims(String rawToken){
        return Jwts.parser()
                .setAllowedClockSkewSeconds(60)
                .requireIssuer("xc-auth-server")
                .setSigningKeyResolver(rsaKeyResolver)
                .parseClaimsJws(rawToken)
                .getBody();
    }*/

    /**
     * Performs issuer check, expiration check and parses
     * @param rawToken
     * @return
     */
    private static Claims validateAndParseJwtClaims(String rawToken){
        return Jwts.parser()
                .setAllowedClockSkewSeconds(60)
                .requireIssuer("xc-auth-server")
                .parseClaimsJwt(rawToken)
                .getBody();
    }

    /**
     * Performs client_id check
     * @param claims
     * @return
     */
    private static void validateOtherTokenDetails(Claims claims) throws InvalidClaimException {
        InvalidClaimException invalidClaimException = null;
        // client check
        String client_id = (String)claims.get("client_id");
        if(client_id == null || client_id.isEmpty()){
            //logger.error("no client found in access token");
            String msg = String.format(
                    ClaimJwtException.MISSING_EXPECTED_CLAIM_MESSAGE_TEMPLATE,
                    "client_id", "<some_allowed_client_id>"
            );
            invalidClaimException = new IncorrectClaimException(null, claims, msg);
            throw invalidClaimException;
        }
        if(clients == null || clients.isEmpty()){
            //logger.error("no clients configured on this service. Look at authorization.clients config in application.yml.");
            String msg = "authorization.clients configuration incomplete";
            invalidClaimException = new IncorrectClaimException(null, claims, msg);
            throw invalidClaimException;
        }
        boolean clientAllowed = clients.stream().anyMatch(item -> {
            return item.equalsIgnoreCase(client_id);
        });

        if(!clientAllowed) {
            //logger.error("client is not allowed: " + client_id);
            String msg = String.format(
                    ClaimJwtException.INCORRECT_EXPECTED_CLAIM_MESSAGE_TEMPLATE,
                    "client_id", "one_of_allowed_client_id",  client_id
            );
            invalidClaimException = new IncorrectClaimException(null, claims, msg);
            throw invalidClaimException;
        }
    }

    private static CommonErrorResponse getUnAuthorizedResponse(String path) {
        return CommonErrorResponseBuilder.builder()
                .instanceDetails("Not Authorized to access requested resource")
                .instanceDebugDetails(path)
                .status(HttpStatus.UNAUTHORIZED)
                .timestamp(ZonedDateTime.now(ZoneOffset.UTC))
                .build();
    }

}
