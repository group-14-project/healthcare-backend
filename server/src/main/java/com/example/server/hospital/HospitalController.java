package com.example.server.hospital;

import com.example.server.doctor.DoctorEntity;
import com.example.server.doctor.DoctorService;
import com.example.server.dto.request.AddSpecializationRequest;
import com.example.server.hospitalSpecialization.HospitalSpecializationEntity;
import com.example.server.hospitalSpecialization.HospitalSpecializationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hospital")
@CrossOrigin
public class HospitalController {

    private final HospitalService hospitalService;

    private final DoctorService doctorService;

    private final HospitalSpecializationService hospitalSpecialization;

    public HospitalController(HospitalService hospitalService, HospitalSpecializationService hospitalSpecialization, DoctorService doctorService){
        this.hospitalService = hospitalService;
        this.hospitalSpecialization = hospitalSpecialization;
        this.doctorService = doctorService;
    }

    @PostMapping("/specialization")
    ResponseEntity<Void> registerNewSpecialization(@RequestBody AddSpecializationRequest body){
        System.out.println("Heloooooo");
        DoctorEntity newDoctor = doctorService.registerNewDoctor(
                body.getSpecialization().getDoctorFirstName(),
                body.getSpecialization().getDoctorLastName(),
                body.getSpecialization().getDoctorEmail(),
                body.getSpecialization().getDoctorRegistrationId()
        );

        HospitalSpecializationEntity newSpecialization = hospitalSpecialization.registerNewSpecialization(
                body.getSpecialization().getName(),
                body.getSpecialization().getHospitalId(),
                body.getSpecialization().getDoctorEmail()
        );

        newDoctor.setHospitalSpecialization(newSpecialization);
        doctorService.addDoctor(newDoctor);


        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
