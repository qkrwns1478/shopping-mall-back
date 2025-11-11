package com.example.shoppingmall.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "Welcome!";
    }
    @GetMapping("/hello")
    public String hello() {
        return "HelloWorld";
    }
}
