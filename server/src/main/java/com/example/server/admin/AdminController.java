package com.example.server.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    public AdminRepository adminRepository;

    @Autowired  // This annotation is needed for dependency injection
    public AdminController(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @PostMapping("/insert")
    AdminEntity addAdmin(@RequestBody AdminEntity admin){
        return adminRepository.save(admin);
    }

    @GetMapping("/adminget")
    public List<AdminEntity> getAdmin(){
        return adminRepository.findAll();
    }

}
