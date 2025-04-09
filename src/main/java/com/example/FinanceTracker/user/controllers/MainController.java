package com.example.FinanceTracker.user.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class MainController {
    @GetMapping("/unsecured")
    public String unsecuredData() {
        return "unsecuredData";
    }

    @GetMapping("/secured")
    public String secured(){
        return "securedData";
    }

    @GetMapping("/admin")
    public String admin() {
        return "adminData";
    }

    @GetMapping("/info")
    public String userData(Principal principal) {
        return principal.getName();
    }
}
