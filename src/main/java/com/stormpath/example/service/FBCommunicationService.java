package com.stormpath.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stormpath.example.model.FBAuth;
import com.stormpath.example.model.FBMe;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class FBCommunicationService {
    public FBMe getEmailAddressFromFBAccessToken(FBAuth auth) throws IOException {
        GetMethod method = new GetMethod(
            "https://graph.facebook.com/me?access_token=" + auth.getAccessToken() + "&fields=id,name,email"
        );

        HttpClient httpClient = new HttpClient();
        httpClient.executeMethod(method);

        BufferedReader br = new BufferedReader(
            new InputStreamReader(method.getResponseBodyAsStream())
        );
        StringBuffer buffer = new StringBuffer();
        String line;
        while(((line = br.readLine()) != null)) {
            buffer.append(line);
        }

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(buffer.toString(), FBMe.class);
    }
}
