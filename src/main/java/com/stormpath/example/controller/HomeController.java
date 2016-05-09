package com.stormpath.example.controller;

import com.stormpath.example.model.FBAuth;
import com.stormpath.example.model.StormpathTokenResponse;
import com.stormpath.example.service.StormpathCommunicationService;
import com.stormpath.sdk.account.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
public class HomeController {

    @Value("#{ @environment['facebook.app.id'] }")
    String fbAppId;

    @Autowired
    StormpathCommunicationService stormpathCommunicationService;

    @RequestMapping("/")
    public String home(Model model) {
        model.addAttribute("fbAppId", fbAppId);

        return "home";
    }

    @RequestMapping("/exchange")
    public @ResponseBody StormpathTokenResponse exchange(@RequestBody FBAuth auth) throws IOException {

        Account account = stormpathCommunicationService.getAccountFromFBToken(auth);

        return stormpathCommunicationService.getTokenResponseFromEmail(account.getEmail());
    }
}
