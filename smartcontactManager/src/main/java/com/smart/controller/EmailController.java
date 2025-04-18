package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smart.Model.EmailRequest;
import com.smart.service.EmailService;

@RestController
@RequestMapping("/api") // Base URL for all endpoints
@CrossOrigin(origins = "*") // Allow all origins for frontend calls
public class EmailController {
    
    @Autowired
    private EmailService emailService;

    // Welcome Test API
    @RequestMapping("/welcome")
    public String welcome() {
        return "Hello, this is my email API!";
    }

    // API to Send Email
    @PostMapping("/sendemail")
    public ResponseEntity<?> sendEmail(@RequestBody EmailRequest request) {
        System.out.println("Email Request: " + request);
        
     boolean result = this.emailService.sendEmail(request.getTo(), request.getSubject(), request.getMessage());
     	 if(result) {
     		 return ResponseEntity.ok("Email sent Sucessfull");
     	 }else {
     		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Email not sent"); 
     	 }
       
    }
}