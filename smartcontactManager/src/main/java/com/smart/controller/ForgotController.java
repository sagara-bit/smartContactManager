package com.smart.controller;

import java.security.Principal;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {


    @Autowired
     private EmailService emailService;
    @Autowired
     private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder bCrypt;
    
    
	@RequestMapping("/forgot")
  public String openEmailForm() {
	  
	  return "forget_email_form";
  }
	
	

	@PostMapping("/send-otp")
  public String sendOtp(@RequestParam("email") String email, HttpSession session) {
	  
		System.out.println("EMail" +email);
		 User user = this.userRepository.getUserByUserName(email);
		 String Name = user.getName();
		System.out.println(Name +"Name");
		//genration of 4 digit otp
		 // Generate a 6-digit OTP
	    Random random = new Random(); // ✅ Don't use fixed seed
	    int otp = 100000 + random.nextInt(900000); // ensures 6-digit number
		System.out.println("OTP"+otp);
		// logic to send mail
		String subject = "OTP from SCM";
		String message = "<div style='font-family: Arial, sans-serif; padding: 20px; background-color: #f4f4f4;'>"
		        + "<div style='max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);'>"
		        + "<h2 style='color: #333; text-align: center;'>Password Reset OTP</h2>"
		        + "<p style='font-size: 16px; color: #555;'>Hi " + Name + ",</p>"
		        + "<p style='font-size: 16px; color: #555;'>We received a request to reset your password. Use the OTP below to proceed. This OTP is valid for the next 10 minutes.</p>"
		        + "<div style='text-align: center; margin: 30px 0;'>"
		        + "<span style='display: inline-block; font-size: 28px; background: #007bff; color: white; padding: 10px 25px; border-radius: 6px;'>"
		        + otp
		        + "</span>"
		        + "</div>"
		        + "<p style='font-size: 14px; color: #999;'>If you did not request this, please ignore this message.</p>"
		        + "<p style='font-size: 14px; color: #999;'>Thank you, <br/> YourApp Team</p>"
		        + "</div></div>";


		String to = email;
		boolean sendEmail = this.emailService.sendEmail(to, subject, message);
		if(sendEmail) {
			session.setAttribute("myotp",otp);
			session.setAttribute("email",email);
			return "VerifyOtp";
		}else {
			Message m = new Message("error", "Please Enter Correct Email Id");
	    	session.setAttribute("message", m);
			 return "forget_email_form";
		}

  }
	
	//verify Otp handler
	
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") Integer otp, HttpSession session) {
	    Integer myOtp = (Integer) session.getAttribute("myotp");
	    String email = (String) session.getAttribute("email");

	    if (myOtp != null && myOtp.equals(otp)) {
	     User userByUserName = this.userRepository.getUserByUserName(email);
	     if(userByUserName ==null) {
	    	 //send error message
	    	 Message m = new Message("error", "User does'not exist");
		    	session.setAttribute("message", m);
	     }else {
	    	 
	     }
	        return "password_change_form";
	    } else {
	    	Message m = new Message("error", "You have entered a wrong OTP.");
	    	session.setAttribute("message", m);
	        return "VerifyOtp";
	    }
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newPassword") String newPassword, HttpSession session) {
	    try {
	        String email = (String) session.getAttribute("email");

	        if (email == null) {
	            session.setAttribute("message", new Message("Session expired. Please try again.", "alert-danger"));
	            return "redirect:/forgot";
	        }

	        User user = userRepository.getUserByUserName(email);
	        if (user == null) {
	            session.setAttribute("message", new Message("User not found with the provided email.", "alert-danger"));
	            return "redirect:/forgot";
	        }

	        // Encode and update password
	        userRepository.updatePasswordByEmail(bCrypt.encode(newPassword), email);

	        session.removeAttribute("email");
	        return "redirect:/signin?change=Password changed successfully.";

	    } catch (Exception e) {
	        e.printStackTrace();
	        session.setAttribute("message", new Message("Something went wrong. Please try again.", "alert-danger"));
	        return "redirect:/forgot";
	    }
	}

	 
	
	
	
	
	
	
}
