package com.smart.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
//	@GetMapping("/test")
//	@ResponseBody
//	public String test() {
//		User user = new User();
//		user.setName("Sagar");
//		userRepository.save(user);
//		return " Working";
//	}
	
	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title","Home- Smart Contact Manager");
		
		return "home";
		
	}
	
	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title","about- Smart Contact Manager");
		
		return "about";
		
	}
	
	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("user", new User());
		model.addAttribute("title","Register at Smart Contact Manager");
		
		return "signup";
		
	}
	
	
	// this handle for register use /do_register
	
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result,
	                           @RequestParam(value="agreement", defaultValue = "false") boolean agreement,
	                           RedirectAttributes redirectAttributes, Model model) {
	    try {
	        if (!agreement) {
	            throw new Exception("You must agree to the terms and conditions");
	        }
	        System.out.println(result +" result ===");
	        if(result.hasErrors()) {
	        	System.out.println("ERROR"+ result.toString());
	        	model.addAttribute("user",user);
	        	return "signup";
	        }
	        user.setRole("ROLE_USER");
	        user.setEnabled(true);
	        user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));

	        this.userRepository.save(user);
	        
	        if(!result.hasErrors()) {
	        	 redirectAttributes.addFlashAttribute("message", new Message("Successfully Registered!", "alert-success"));
	        }
	       
	        return "redirect:/signup";

	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("message", new Message("Something went wrong: " + e.getMessage(), "alert-danger"));
	        return "redirect:/signup";
	    }
	}
	
	//handler for custom login
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title","Login page");
		return "login";
	}
	
	 @GetMapping("/loginfail")
	    public String loginFailed(Model model) {
	        model.addAttribute("error", "Invalid username or password!");
	        return "/normal/loginFail";
	    }

}

