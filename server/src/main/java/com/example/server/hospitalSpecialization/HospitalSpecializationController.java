package com.example.server.hospitalSpecialization;

import com.example.server.doctor.DoctorEntity;
import com.example.server.doctor.DoctorService;
import com.example.server.dto.request.AddSpecializationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/hospital-specialization")
public class HospitalSpecializationController {
    private final HospitalSpecializationService hospitalSpecialization;

    private final DoctorService doctorService;

    public HospitalSpecializationController(HospitalSpecializationService hospitalSpecialization, DoctorService doctorService) {
        this.hospitalSpecialization = hospitalSpecialization;
        this.doctorService = doctorService;
    }

    public static class SrDoctorNotCreatedException extends SecurityException{
        public SrDoctorNotCreatedException(){ super("Senior Doctor Not Created");}
    }
    public static class SpecializationNotCreatedException extends SecurityException{
        public SpecializationNotCreatedException(){ super("Specialization Not Created");}
    }

    @PostMapping("/add")
    ResponseEntity<Void>  registerNewSpecialization(@RequestBody AddSpecializationRequest body) {
        DoctorEntity newDoctor = doctorService.registerNewDoctor(
                body.getSpecialization().getDoctorFirstName(),
                body.getSpecialization().getDoctorLastName(),
                body.getSpecialization().getDoctorEmail(),
                body.getSpecialization().getDoctorRegistrationId(),
                body.getSpecialization().getDegree(),
                body.getSpecialization().getPhoneNumber()
        );
        if (newDoctor == null) {
            throw new SrDoctorNotCreatedException();
        }

        HospitalSpecializationEntity newSpecialization = hospitalSpecialization.registerNewSpecialization(
                body.getSpecialization().getName(),
                body.getSpecialization().getHospitalEmail(),
                body.getSpecialization().getDoctorEmail()
        );

        newDoctor.setHospitalSpecialization(newSpecialization);
        doctorService.addDoctor(newDoctor);


        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
