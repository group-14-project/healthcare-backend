package com.example.server.doctor;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/doctor")
public class DoctorController {
    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

//    @GetMapping("/showdoc/{id}")
//    ResponseEntity<Optional<DoctorEntity>> getDoc(@PathVariable Integer id) {
//        Optional<DoctorEntity> doctor = doctorService.getDoc(id);
//        if (doctor.isPresent()) {
//            return ResponseEntity.ok(doctor); // Returning ResponseEntity with status OK and doctor as body
//        } else {
//            return ResponseEntity.notFound().build(); // Returning ResponseEntity with status NOT_FOUND
//        }
//
//    }
}
