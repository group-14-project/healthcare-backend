package com.example.server.jwtToken;

import com.example.server.doctor.DoctorEntity;
import com.example.server.doctor.DoctorService;
import com.example.server.errorOrSuccessMessageResponse.ErrorMessage;
import com.example.server.hospital.HospitalEntity;
import com.example.server.hospital.HospitalService;
import com.example.server.patient.PatientEntity;
import com.example.server.patient.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class JWTTokenReCheck {

    private final JWTService jwtService;
    private final PatientService patient;
    private final DoctorService doctor;
    private final HospitalService hospital;

    public JWTTokenReCheck(JWTService jwtService, PatientService patient, DoctorService doctorService, HospitalService hospital) {
        this.jwtService = jwtService;
        this.patient = patient;
        this.doctor = doctorService;
        this.hospital = hospital;
    }

    public PatientEntity checkJWTAndSessionPatient(HttpServletRequest request){
        String jwtToken = jwtService.extractToken(request);

        Map<String, String> jwtMap = jwtService.decodeJWT(jwtToken);

        String role = jwtMap.get("role");
        String email = jwtMap.get("email");
        String isExpired = jwtMap.get("isExpired");

        if(role == null || email==null || isExpired==null || !role.equals("ROLE_patient")){
            return null;
        }

        return patient.checkJWT(email, jwtToken);
    }

    public DoctorEntity checkJWTAndSessionDoctor(HttpServletRequest request){
        String jwtToken = jwtService.extractToken(request);

        Map<String, String> jwtMap = jwtService.decodeJWT(jwtToken);

        String role = jwtMap.get("role");
        String email = jwtMap.get("email");
        String isExpired = jwtMap.get("isExpired");

        if(role == null || email==null || isExpired==null || (!role.equals("ROLE_doctor") && !role.equals("ROLE_seniorDoctor"))){
            doctor.makeDoctorOffline(email);
            return null;
        }

        return doctor.checkJWT(email, jwtToken);
    }

    public HospitalEntity checkJWTAndSessionHospital(HttpServletRequest request) {
        String jwtToken = jwtService.extractToken(request);

        Map<String, String> jwtMap = jwtService.decodeJWT(jwtToken);

        String role = jwtMap.get("role");
        String email = jwtMap.get("email");
        String isExpired = jwtMap.get("isExpired");

        if(role == null || email==null || isExpired==null || !role.equals("ROLE_hospital")){
            return null;
        }

        return hospital.checkJWT(email, jwtToken);
    }

    public DoctorEntity checkJWTAndSessionSeniorDoctor(HttpServletRequest request) {
        String jwtToken = jwtService.extractToken(request);

        Map<String, String> jwtMap = jwtService.decodeJWT(jwtToken);

        String role = jwtMap.get("role");
        String email = jwtMap.get("email");
        String isExpired = jwtMap.get("isExpired");

        if(role == null || email==null || isExpired==null || !role.equals("ROLE_seniorDoctor")){
            doctor.makeDoctorOffline(email);
            return null;
        }
        return doctor.checkJWT(email, jwtToken);
    }

}
