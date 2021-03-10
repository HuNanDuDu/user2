package com.example.user1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
public class UserController {
    @GetMapping("/getUserName")
    public String getUserName(String name, HttpServletRequest request){

        return request.getServerPort()+ name;
    }
}
