package io.github.johntortoise.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


@Controller
@RequestMapping("/")
public class TortoiseWebController {

    
    @GetMapping
    public String index(HttpServletRequest request) {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            
            return "redirect:/login";
        }
        return "layout"; 
    }
    
    
    @GetMapping("/index")
    public String indexPage(HttpServletRequest request) {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            
            return "redirect:/login";
        }
        return "layout"; 
    }
    
    
    @GetMapping("/login")
    public String loginPage() {
        return "login"; 
    }
}