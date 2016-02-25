package com.stormpath.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stormpath.example.model.FBMe;
import com.stormpath.example.model.StormpathTokenResponse;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.account.Accounts;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.client.Clients;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

        // Prepare a POST request against /oauth/token
        PostMethod method = new PostMethod(applicationHref + "/oauth/token");
        NameValuePair[] data = {
            new NameValuePair("grant_type", "stormpath_token"),
            new NameValuePair("token", token)
        };
        method.setRequestBody(data);

        // Use Basic auth with the api key id and api key secret
        HttpClient httpClient = new HttpClient();
        httpClient.getState().setCredentials(
            AuthScope.ANY,
            new UsernamePasswordCredentials(client.getApiKey().getId(), client.getApiKey().getSecret())
        );
        httpClient.executeMethod(method);

        BufferedReader br = new BufferedReader(
            new InputStreamReader(method.getResponseBodyAsStream())
        );
        StringBuffer buffer = new StringBuffer();
        String line;
        while(((line = br.readLine()) != null)) {
            buffer.append(line);
        }

        // convert the response to a StormpathTokenResponse
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(buffer.toString(), StormpathTokenResponse.class);
    }
}
