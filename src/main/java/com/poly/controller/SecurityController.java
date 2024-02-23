package com.poly.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


import com.poly.entity.Account;

import com.poly.service.impl.AccountServiceImpl;

@Controller
public class SecurityController {
	  
	@Autowired
    private AccountServiceImpl accountService;
	
	
	@RequestMapping("/security/login/form")
	public String loginForm(Model model) {
		model.addAttribute("message", "Vui lòng đăng nhập");
		return "security/login";
	}

	@RequestMapping("/security/login/success")
	public String loginSuccess(Model model) {
		model.addAttribute("message", "Đăng nhập thành công!");
		return "index";
	}

	@RequestMapping("/security/login/error")
	public String loginError(Model model) {
		model.addAttribute("loginError","Sai thông tin đăng nhập!");
		
		return "security/login";
	}
	@RequestMapping("/security/logoff/success")
	public String logoffSucess(Model model) {
		model.addAttribute("message","Bạn đã đăng xuất thành công!");
		return "security/login";
	}
	

	
	@RequestMapping("/security/unauthoried")
	public String unauthoried(Model model) {
		model.addAttribute("message", "Không có quyền truy xuất");
		return "security/login";
	}
	@GetMapping("/register")
    public String register(Model model) {
    	model.addAttribute("title", "Register Pages");
    	model.addAttribute("account", new Account());
    	return "security/register";
    }
	@PostMapping("/register-new")
    public String newUser(@Valid @ModelAttribute("account") Account account,
                          Model model,
                          BindingResult result) {
        try {
            if (result.hasErrors()) {
                model.addAttribute("account", account);
                result.toString();
                return "security/register";
            }
            String username = account.getUsername();
            String email = account.getEmail();
            Account user = accountService.findByUsername(username);
            Account users = accountService.findByEmail(email);
            if (user != null) {
                model.addAttribute("account", account);
                model.addAttribute("usernameError","username đã tồn tại");
                System.out.println("Username trùng");
                
                return "security/register";
            }if(users != null) {
            	 model.addAttribute("account", account);
            	 model.addAttribute("emailError","email đã tồn tại");
            	 System.out.println("Email trùng");
            	   return "security/register";
            }
            else {
                // Gán giá trị trả về từ accountService.findByUsername(username) vào user     
            	
                user = accountService.findByEmail(email);
                // Thực hiện các xử lý tạo tài khoản ở đây, ví dụ:
                accountService.create(account);
//                authoService.create(1, "av", "dc"); 
                model.addAttribute("account", account);
                return "security/login";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "security/login";
    }
	
}
