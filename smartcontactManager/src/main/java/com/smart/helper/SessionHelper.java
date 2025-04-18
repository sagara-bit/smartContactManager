package com.smart.helper;

import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component("sessionHelper")  // Make sure it's accessible in Thymeleaf
public class SessionHelper {

    // No parameters needed, get session inside the method
    public String removeMessageFromSession() {  
        try {
            // Get current request attributes
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attr != null) {
                HttpSession session = attr.getRequest().getSession();
                if (session != null) {
                    session.removeAttribute("message");
                }
            }
        } catch (Exception e) {
            return "Error removing message";  // Avoid Thymeleaf errors
        }
        return ""; // Return empty string to prevent rendering issues
    }
}
