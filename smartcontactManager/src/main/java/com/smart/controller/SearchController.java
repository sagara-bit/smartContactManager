package com.smart.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;

@RestController
public class SearchController {
 
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	//search handler
	@GetMapping("/search/{Searchquery}")
  public ResponseEntity<?> search(@PathVariable("Searchquery") String query, Principal principal){
	  User userByUserName = this.userRepository.getUserByUserName(principal.getName());
	  List<Contact> contacts = this.contactRepository.findByNameContainingAndUser(query,userByUserName);
	return ResponseEntity.ok(contacts);
	  
  }
	
}
