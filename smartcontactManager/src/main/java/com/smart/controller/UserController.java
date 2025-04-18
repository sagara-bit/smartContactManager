package com.smart.controller;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.MyOrderRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;

import org.json.JSONObject;
import com.razorpay.*;
import com.razorpay.RazorpayClient;


@Controller
@RequestMapping("/user")
public class UserController {

    private final AuthenticationManager authenticationManager;
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private MyOrderRepository myOrderRepository;

    UserController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
	
	//method for adding common data to response
	@ModelAttribute
	public void addCommanData(Model model, Principal principal) {
		
		String userName = principal.getName();
		
		System.out.println(userName + "userName");
		
		//get the user using username
		
		User user = userRepository.getUserByUserName(userName);
		System.out.println("user"+user);
		
		model.addAttribute(user);
		
	}
	
	//dashboardhome
	@RequestMapping("/index")
	public String dasboard(Model model, Principal principal) {
		model.addAttribute("title","User Dashboard");
		return "normal/userDashboard";
	}
	
	//opem add form handler
	
	@RequestMapping("/addContact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title","add Contact");
		model.addAttribute("contact",new Contact());
		return "normal/addContactForm";
	}
	
	
	/*processing at contact form*/
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Principal principal,HttpSession session) {
		try {
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		contact.setUser(user);
		
		// processing and uploading file 
		
		if(file.isEmpty()) {
			System.out.println("Error file is emptty");
			contact.setImageUrl("contact.png");
		}else {
			 // file the file to folder and update name to the contact;
			contact.setImageUrl(file.getOriginalFilename());
			File saveFile = new ClassPathResource("static/img").getFile();
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(),path ,StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Image uploaded sucessFully");
		}
		
		
		
		user.getContacts().add(contact);
		this.userRepository.save(user);
		System.out.println("data of " + contact);
		System.out.println("added to the database");
		//message success
		session.setAttribute("message", new Message("Your contact is added !! Add more..","success"));
		
		}catch (Exception e) {
			System.out.println("ERROR" + e.getMessage());
			e.printStackTrace();
			// error message
			session.setAttribute("message", new Message("Something Went Wrong","danger"));
			
		}
		return "normal/contactSucess";
	}
	
	
	//show contact handler
	//per page = 5[n]
	//cuurent page = 0 [page]
	@RequestMapping("/viewContacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,  Model m, Principal principal) {
		m.addAttribute("title","Show user Contacts");
		/*
		 * // send the list of contact String username= principal.getName(); User user =
		 * this.userRepository.getUserByUserName(username); List<Contact> contacts =
		 * user.getContacts();
		 */
		String userName = principal.getName();
		User userByUserName = this.userRepository.getUserByUserName(userName);
		 Pageable pageable= PageRequest.of(page, 5);
		  // Clear cache before fetching
		    contactRepository.flush();
		Page<Contact> contactByUser = this.contactRepository.findContactByUser(userByUserName.getId(),pageable);
		
		m.addAttribute("contacts",contactByUser);
		m.addAttribute("currentPage",page);
		m.addAttribute("totalPages",contactByUser.getTotalPages());
		return "normal/showContact";
	}
	
	
	//showing particular Contact Details
	@RequestMapping("/contact/{cId}")
	public String showContactDetails(@PathVariable("cId") Integer cId,Model model ,Principal principal) {
		
		
		System.out.println("CID" + cId);
		
		Optional<Contact> contact = this.contactRepository.findById(cId);
		
		Contact contact2 = contact.get();
		
		//fixing the security bug 
		
		String userName = principal.getName();
		
		User user = this.userRepository.getUserByUserName(userName);
		if(user.getId() ==  contact2.getUser().getId()) {
			model.addAttribute("contact",contact2);
			model.addAttribute("title",contact2.getName());
		}
		
		
		
		return "normal/contactDetail";
	}
	
	
	//deleteContact Handler
	
	@GetMapping("/delete/{cid}/{curentPage}")
	public String deleteConact(@PathVariable("cid") Integer cId,@PathVariable("curentPage") int page, Model model, HttpSession session,Principal principal) {
		
		/*
		 * Contact contact = this.contactRepository.findById(cId).get();
		 * 
		 * System.out.println("Contact" + contact.getCid());
		 * 
		 * contact.setUser(null);
		 * 
		 * //remove //img //contact.getImage() this.contactRepository.delete(contact);
		 */
		  Contact contact = this.contactRepository.findById(cId).get();
		  User user = this.userRepository.getUserByUserName(principal.getName());
		  user.getContacts().remove(contact);
		this.userRepository.save(user);
		
		session.setAttribute("message", new Message("contact Deleted", "success"));
		
		 // Redirect to the same page after deletion
	    return "redirect:/user/viewContacts/" + page;
	}
	
	
	// open update from  the contact handler
	@PostMapping("/updateContact/{cid}")
	public String updateContact(Model model,@PathVariable("cid") int cid) {
		model.addAttribute("title","update Contact");
		Contact contact = this.contactRepository.findById(cid).get();
		model.addAttribute("contact",contact);
		return "/normal/updateContact";
	}
	
	//update form handler
	
	@PostMapping("/processContact")
	public String updateContactHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model model , 
			HttpSession session, Principal principal) {
		try {	
			
			/*
			 * Contact oldContactDetail =
			 * this.contactRepository.findById(contact.getCid()).get();
			 */
			Contact oldContactDetail = this.contactRepository.findById(contact.getCid())
				    .orElseThrow(() -> new RuntimeException("Contact not found for ID: " + contact.getCid()));

			if(!file.isEmpty()) {
				
				//file work 
				//rewrite
				//delete old photo
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile,oldContactDetail.getImageUrl());
				file1.delete();
				///update new photo
				///
			File saveFile = new ClassPathResource("static/img").getFile();
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(),path ,StandardCopyOption.REPLACE_EXISTING);
			contact.setImageUrl(file.getOriginalFilename());
				
			}else {
				contact.setImageUrl(oldContactDetail.getImageUrl());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			
			System.out.println("USER" + user.getName());
			contact.setUser(user);
			System.out.println("concta is sett");
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("your contact is updated", "success"));
			System.out.println("concta is saved sucessFully");
			
		}catch (Exception e) {
			// TODO: handle exception
		}
		/* /user/contact/102 */
		return "redirect:/user/contact/"+contact.getCid();
	}
	
	
	// view your profile handler
	
	@GetMapping("/profile")
	public String yourProfile(Model model) {

		model.addAttribute("title","Profile Page");
		return "/normal/viewContact";
	}

	 // open setiing handler
	@GetMapping("/settings")
	public String openSettings() {
		
		return "normal/settings";
	}
	
	// chnange password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,
			Principal principal,HttpSession session) {
		System.out.println("old password"+ oldPassword);
		System.out.println("new password"+ newPassword);
		String name = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(name);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword,currentUser.getPassword())) {
			//change the password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message",new Message("your password is saved successfully", "alert-success"));
		}else {
			session.setAttribute("message",new Message("Please Enter correct old password", "danger"));
		}
		
		
		return "normal/settings";
	}
	
	
	// for transaction using razorpay
	@ResponseBody
	@PostMapping("/create_order")
	public String createOrder(@RequestBody Map<String, Object> data,Principal principal) throws Exception {
		System.out.println("Hey order function ex." + data);
		int amount =  Integer.parseInt(data.get("amount").toString());
		RazorpayClient razorpayClient = new RazorpayClient("rzp_test_CnhZtQfiw22XZ8","xZrWZyk16zbopxej9eD2KXan");
		JSONObject ob = new JSONObject();
		ob.put("amount", amount*100);
		ob.put("currency", "INR");
		ob.put("receipt", "txn_235425");
		
		// creating new order
	 Order order = razorpayClient.orders.create(ob);
	 System.out.println("order"+order);
	 
	 // we can save order detail to our database
	 
	 MyOrder myOrder = new MyOrder();
	
	 myOrder.setAmount(order.get("amount")+"");
	 myOrder.setOrderId(order.get("id"));
	 myOrder.setPaymentId(null);
	 myOrder.setStatus("created");
	 myOrder.setUser(this.userRepository.getUserByUserName(principal.getName()));
	 myOrder.setReceipt(order.get("receipt"));
	 this.myOrderRepository.save(myOrder);

		return order.toString();
	}
	
	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data) {

	    System.out.println(data);  // Logging the received data

	    MyOrder myorder = this.myOrderRepository.findByOrderId(data.get("order_id").toString());

	    if (myorder == null) {
	        return ResponseEntity.badRequest().body(Map.of("error", "Order not found"));
	    }

	    myorder.setPaymentId(data.get("payment_id").toString());
	    myorder.setStatus(data.get("PaymentStatus").toString()); // Fix key name here!

	    this.myOrderRepository.save(myorder);

	    return ResponseEntity.ok(Map.of("msg", "updated"));
	}

	
	
}
