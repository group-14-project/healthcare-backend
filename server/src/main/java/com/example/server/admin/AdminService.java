package com.example.server.admin;

import org.springframework.stereotype.Service;

@Service
public class AdminService {
    private AdminRepository adminRepo;

    public AdminService(AdminRepository adminRepo){
        this.adminRepo = adminRepo;
    }
}
