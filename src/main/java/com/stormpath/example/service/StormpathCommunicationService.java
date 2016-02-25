package com.stormpath.example.service;

import com.stormpath.example.model.StormpathTokenResponse;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.account.Accounts;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.client.Clients;
import com.stormpath.sdk.oauth.Authenticators;
import com.stormpath.sdk.oauth.IdSiteAuthenticationRequest;
import com.stormpath.sdk.oauth.Oauth2Requests;
import com.stormpath.sdk.oauth.OauthGrantAuthenticationResult;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Date;

@Service
public class StormpathCommunicationService {

    @Value("#{ @environment['stormpath.application.href'] }")
    String applicationHref;

    private Client client;
    private Application application;

    @PostConstruct
    void setupClient() {
        client = Clients.builder().build();
        application = client.getResource(applicationHref, Application.class);
    }

    // this is trusting that the email has been verified as valid prior to calling this method
    public StormpathTokenResponse getTokenResponseFromEmail(String email) throws IOException {
        Account account = application.getAccounts(Accounts.where(Accounts.email().eqIgnoreCase(email))).single();

        // create a JWT for the internal soon-to-be gone grant_type=stormpath_token request
        JwtBuilder jwtBuilder = Jwts.builder()
            .setSubject(account.getHref())
            .setIssuer(applicationHref)
            .setAudience(client.getApiKey().getId())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + (1000 * 60)))
            .claim("status", "AUTHENTICATED");

        String secret = client.getApiKey().getSecret();
        String token = jwtBuilder.signWith(
            SignatureAlgorithm.HS512, secret.getBytes("UTF-8")
        ).compact();

        // It's not really ID Site
        IdSiteAuthenticationRequest request =
            Oauth2Requests.IDSITE_AUTHENTICATION_REQUEST.builder().setToken(token).build();

        OauthGrantAuthenticationResult result =
            Authenticators.ID_SITE_AUTHENTICATOR.forApplication(application).authenticate(request);

        return new StormpathTokenResponse(
            result.getAccessTokenString(),
            result.getRefreshTokenString(),
            result.getTokenType(),
            result.getExpiresIn()
        );
    }
}
