package com.stormpath.example.controller;

import com.stormpath.example.model.FBAuth;
import com.stormpath.example.model.FBMe;
import com.stormpath.example.model.StormpathTokenResponse;
import com.stormpath.example.service.FBCommunicationService;
import com.stormpath.example.service.StormpathCommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
public class HomeController {

    @Autowired
    FBCommunicationService fbCommunicationService;

    @Autowired
    StormpathCommunicationService stormpathCommunicationService;

    @RequestMapping("/")
    public String home() {
        return "home";
    }

    @RequestMapping("/exchange")
    public @ResponseBody StormpathTokenResponse exchange(@RequestBody FBAuth auth) throws IOException {

        FBMe fbMe = fbCommunicationService.getEmailAddressFromFBAccessToken(auth);

        return stormpathCommunicationService.getTokenResponseFromEmail(fbMe.getEmail());
    }
}
