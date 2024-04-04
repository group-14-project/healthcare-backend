package com.example.server.consent;

import org.springframework.stereotype.Service;

@Service
public class ConsentService {
    private final ConsentRepository consentRepo;

    public ConsentService(ConsentRepository consentRepo) {
        this.consentRepo = consentRepo;
    }


}
