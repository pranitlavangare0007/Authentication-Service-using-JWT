package com.securityImplementation.practice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {


   final PasswordEncoder bCryptPasswordEncoder;

   public HomeController(PasswordEncoder bCryptPasswordEncoder){
       this.bCryptPasswordEncoder=bCryptPasswordEncoder;
   }
    @GetMapping("/")
    public String greeting(){


        return bCryptPasswordEncoder.encode("password");
    }
}
