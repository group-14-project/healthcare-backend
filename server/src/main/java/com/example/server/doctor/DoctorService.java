package com.example.server.doctor;

import com.example.server.common.CommonService;
import com.example.server.dto.response.*;
import com.example.server.emailOtpPassword.EmailSender;
import com.example.server.emailOtpPassword.PasswordUtil;
import com.example.server.hospital.HospitalEntity;
import com.example.server.hospital.HospitalRepository;
import com.example.server.hospitalSpecialization.HospitalSpecializationEntity;
import com.example.server.hospitalSpecialization.HospitalSpecializationRepository;
import org.json.JSONObject;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;

    private final PasswordUtil passwordUtil;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final EmailSender emailSender;

    private final HospitalRepository hospitalRepository;

    private final HospitalSpecializationRepository hospitalSpecializationRepository;

    private final CommonService commonService;

    public DoctorService(DoctorRepository doctorRepository, PasswordUtil passwordUtil, BCryptPasswordEncoder bCryptPasswordEncoder, EmailSender emailSender, HospitalRepository hospitalRepository, HospitalSpecializationRepository hospitalSpecializationRepository, CommonService commonService) {
        this.doctorRepository = doctorRepository;
        this.passwordUtil = passwordUtil;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.emailSender = emailSender;
        this.hospitalRepository = hospitalRepository;
        this.hospitalSpecializationRepository = hospitalSpecializationRepository;
        this.commonService = commonService;
    }

    public void addDoctor(DoctorEntity newDoctor) {
        doctorRepository.save(newDoctor);
    }

    public void setJwtToken(String jwtToken, String email) {
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        doctor.setJwtToken(jwtToken);
        doctorRepository.save(doctor);
    }

    public void setLastAccessTime(String email) {
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        doctor.setLastAccessTime(LocalDateTime.now());
        doctorRepository.save(doctor);
    }

    public DoctorEntity checkJWT(String email, String jwtToken) {
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        if(doctor==null || !Objects.equals(doctor.getJwtToken(), jwtToken)){
            if(doctor!=null){
                makeDoctorOffline(email);
            }
            return null;
        }
        return checkAccessTime(doctor.getEmail());
    }

    private DoctorEntity checkAccessTime(String email) {
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        if (doctor.getLastAccessTime().plusHours(2).isBefore(LocalDateTime.now())) {
            return null;
        }
        return doctor;
    }


    public DoctorEntity findDoctorByEmail(String email){
        return doctorRepository.findDoctorEntitiesByEmail(email);
    }

    public DoctorEntity registerNewDoctor(String firstName, String lastName,  String email, String registrationId, String degree, String phoneNumber){
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        if(doctor != null){
            return null;
        }

        DoctorEntity newDoctor = new DoctorEntity();
        newDoctor.setFirstName(firstName);
        newDoctor.setLastName(lastName);
        newDoctor.setEmail(email);
        newDoctor.setRegistrationId(registrationId);
        newDoctor.setDegree(degree);
        newDoctor.setSenior(false);
        newDoctor.setPhoneNumber(commonService.encrypt(phoneNumber));
        String randomPassword = passwordUtil.generateRandomPassword();
        newDoctor.setPassword(bCryptPasswordEncoder.encode(randomPassword));
        newDoctor.setFirstTimeLogin(false);
        newDoctor.setActiveStatus(0);
        newDoctor.setRole("ROLE_doctor");

        doctorRepository.save(newDoctor);

        emailSender.sendMailWithPassword(
                newDoctor.getEmail(), newDoctor.getFirstName(), randomPassword
        );
        return doctorRepository.findDoctorEntitiesByEmail(newDoctor.getEmail());

    }

    public DoctorEntity verifyDoctor(String email, String otp){
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        if(doctor==null){
            return null;
        }
        else if(doctor.getOtp().equals(otp) && Duration.between(doctor.getOtpGeneratedTime(),
                LocalDateTime.now()).getSeconds()<(2*60)) {
            doctor.setActiveStatus(1);
            return doctorRepository.save(doctor);
        }
        else{
            return null;
        }
    }

    public boolean checkDoctor(String email){
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        return doctor != null;
    }

    public DoctorEntity doctorDetails(String email){
        return doctorRepository.findDoctorEntitiesByEmail(email);
    }

    public DoctorEntity updateOtp(String otp, String email){
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        doctor.setOtp(otp);
        doctor.setOtpGeneratedTime(LocalDateTime.now());
        return doctorRepository.save(doctor);
    }

    //to get the doctors of same department
    public List<String> getDoctorsFromSameSpecialization(HospitalSpecializationEntity hospitalSpecialization) {
        return doctorRepository.findNamesByHospitalSpecialization(hospitalSpecialization);
    }

    public List<DoctorDetailsResponse> getAllDoctorDetails() {
        List<DoctorEntity> allDoctors = doctorRepository.findAll();
        return allDoctors.stream()
                .map(this::mapToDoctorDetailsResponse)
                .collect(Collectors.toList());
    }

    public List<DoctorDetailsResponse> getDoctorsInSpecialization(HospitalSpecializationEntity hospitalSpecialization) {
        List<DoctorEntity> allDoctors = doctorRepository.findAllBySpecialization(hospitalSpecialization);
        return allDoctors.stream()
                .map(this::mapToDoctorDetailsResponse)
                .collect(Collectors.toList());
    }

    private DoctorDetailsResponse mapToDoctorDetailsResponse(DoctorEntity doctorEntity) {
        DoctorDetailsResponse doctorDetailsResponse = new DoctorDetailsResponse();
        doctorDetailsResponse.setFirstName(doctorEntity.getFirstName());
        doctorDetailsResponse.setLastName(doctorEntity.getLastName());
        doctorDetailsResponse.setHospitalName(doctorEntity.getHospitalSpecialization().getHospital().getHospitalName());
        doctorDetailsResponse.setSpecialization(doctorEntity.getHospitalSpecialization().getSpecialization().getName());
        doctorDetailsResponse.setDoctorEmail(doctorEntity.getEmail());
        doctorDetailsResponse.setDegree(doctorEntity.getDegree());
        doctorDetailsResponse.setImageUrl(doctorEntity.getImageUrl());
        return doctorDetailsResponse;
    }

    public List<DoctorStatus> getDoctorStatus() {
        List<DoctorEntity> doctors = doctorRepository.findAll();
        return doctors.stream()
                .map(this::mapToDoctorStatus)
                .collect(Collectors.toList());
    }

    private DoctorStatus mapToDoctorStatus(DoctorEntity doctorEntity) {
        DoctorStatus doctorStatus = new DoctorStatus();
        doctorStatus.setDoctorId(doctorEntity.getId());
        doctorStatus.setEmail(doctorEntity.getEmail());
        doctorStatus.setDegree(doctorEntity.getDegree());
        doctorStatus.setHospitalName(doctorEntity.getHospitalSpecialization().getHospital().getHospitalName());
        doctorStatus.setFirstName(doctorEntity.getFirstName());
        doctorStatus.setLastName(doctorEntity.getLastName());
        doctorStatus.setSpecialization(doctorEntity.getHospitalSpecialization().getSpecialization().getName());
        doctorStatus.setStatus(doctorEntity.getActiveStatus() == 0 ? "offline" :
                (doctorEntity.getActiveStatus() == 1 ? "active" :
                        (doctorEntity.getActiveStatus() == 2 ? "busy" : "on Call")));
        return doctorStatus;
    }

    public List<DoctorEntity> getDoctorsUnder(DoctorEntity doctorEntity) {
        HospitalSpecializationEntity hospitalSpecializationEntity = doctorEntity.getHospitalSpecialization();
        return doctorRepository.findAllBySpecialization(hospitalSpecializationEntity);
    }

    public DoctorEntity updatePassword(String email, String password) {
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        doctor.setFirstTimeLogin(true);
        doctor.setPassword(bCryptPasswordEncoder.encode(password));
        doctor.setFirstTimeLogin(true);
        return doctorRepository.save(doctor);
    }

    public List<HospitalBranchDoctorResponse> viewAllHospitalsAndDoctors() {
        List<HospitalEntity> hospitalEntities = hospitalRepository.findAll();
        List<HospitalBranchDoctorResponse> response = new ArrayList<>();

        for (HospitalEntity hospitalEntity : hospitalEntities) {
            HospitalBranchDoctorResponse hospitalResponse = new HospitalBranchDoctorResponse();
            hospitalResponse.setHospital(hospitalEntity.getHospitalName());

            List<HospitalSpecializationEntity> specializationEntities = hospitalSpecializationRepository.findAllByHospital(hospitalEntity);
            List<SpecializationName> specializationNames = new ArrayList<>();

            for(HospitalSpecializationEntity specializationEntity : specializationEntities){
                SpecializationName specializationName = new SpecializationName();
                specializationName.setSpecialization(specializationEntity.getSpecialization().getName());

                List<DoctorEntity> doctorEntities = doctorRepository.findAllBySpecialization(specializationEntity);
                List<NameResponse> nameResponses = new ArrayList<>();
                for(DoctorEntity doctorEntity : doctorEntities){
                    NameResponse nameResponse = new NameResponse();
                    nameResponse.setFirstName(doctorEntity.getFirstName());
                    nameResponse.setLastName(doctorEntity.getLastName());
                    nameResponse.setEmail(doctorEntity.getEmail());
                    nameResponses.add(nameResponse);
                }
                specializationName.setDoctors(nameResponses);
                specializationNames.add(specializationName);
            }
            hospitalResponse.setSpecializationNames(specializationNames);
            response.add(hospitalResponse);
        }
        return response;
    }

    public void makeDoctorOffline(String email){
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        doctor.setActiveStatus(0);
        doctorRepository.save(doctor);
    }

    public void expireJWTfromTable(String email){
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        doctor.setJwtToken("Expired");
    }

    public void changeStatus(DoctorEntity doctorEntity, Integer id) {
       doctorEntity.setActiveStatus(id);
       doctorRepository.save(doctorEntity);
    }

    public void changeStatusToInACall(String doctorId) {
        Integer id = Integer.valueOf(doctorId);
        DoctorEntity doctor = doctorRepository.findByDoctorId(id);
        doctor.setActiveStatus(3);
        doctorRepository.save(doctor);
    }

    public void changeStatusToActive(String doctorId) {
        Integer id = Integer.valueOf(doctorId);
        DoctorEntity doctor = doctorRepository.findByDoctorId(id);
        doctor.setActiveStatus(1);
        doctorRepository.save(doctor);
    }

    public void passwordChange(String password, String email) {
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(email);
        doctor.setPassword(bCryptPasswordEncoder.encode(password));
        doctorRepository.save(doctor);
    }

    public List<CallDetailsToSeniorDr> callDetailsToSeniorDr(DoctorEntity seniorDoctorEntity, List<String> calls) {
        List<Integer> DoctorIds = new ArrayList<>();
        for(String call : calls){
            JSONObject jsonObject = new JSONObject(call);
            JSONObject jsonObjectTo = new JSONObject(jsonObject.getString("callTo"));
            String doctorId = jsonObjectTo.getString("remoteId");
            int intValue = Integer.parseInt(doctorId);
            DoctorIds.add(intValue);
        }
        HospitalSpecializationEntity hospitalSpecializationEntity = seniorDoctorEntity.getHospitalSpecialization();
        List<DoctorEntity> doctorEntities = doctorRepository.getDoctorsUnderInCall(hospitalSpecializationEntity, DoctorIds);
        List<Integer> NewDoctorIds = new ArrayList<>();
        for(DoctorEntity doctorEntity: doctorEntities){
            Integer id = doctorEntity.getId();
            NewDoctorIds.add(id);
        }

        Iterator<String> iterator = calls.iterator();

        while (iterator.hasNext()) {
            String call = iterator.next();
            JSONObject jsonObject = new JSONObject(call);
            JSONObject jsonObjectTo = new JSONObject(jsonObject.getString("callTo"));
            String doctorId = jsonObjectTo.getString("remoteId");

            try {
                int intValue = Integer.parseInt(doctorId);

                if (!NewDoctorIds.contains(intValue)) {
                    iterator.remove();
                }
            } catch (NumberFormatException e) {
                System.err.println("Error converting to integer: " + doctorId);
            }
        }
        List<CallDetailsToSeniorDr> callDetailsToSeniorDrList = new ArrayList<>();
        for (String call : calls) {
            JSONObject jsonObject = new JSONObject(call);
            JSONObject jsonObjectTo = new JSONObject(jsonObject.getString("callTo"));
            JSONObject jsonObjectFrom = new JSONObject(jsonObject.getString("callFrom"));

            String doctorId = jsonObjectTo.getString("remoteId");
            String patientId = jsonObjectFrom.getString("localId");
            String patientName = jsonObjectFrom.getString("patientName");
            String doctorName = jsonObjectTo.getString("doctorName");

            int docId = Integer.parseInt(doctorId);
            int patId = Integer.parseInt(patientId);
            CallDetailsToSeniorDr callDetailsToSeniorDr = new CallDetailsToSeniorDr();
            callDetailsToSeniorDr.setDoctorId(docId);
            callDetailsToSeniorDr.setDoctorName(doctorName);
            callDetailsToSeniorDr.setPatientName(patientName);
            callDetailsToSeniorDr.setPatientId(patId);
            callDetailsToSeniorDrList.add(callDetailsToSeniorDr);
        }
        return callDetailsToSeniorDrList;
    }

    public void loginStatusChange(DoctorEntity newDoctor) {
        DoctorEntity doctor = doctorRepository.findDoctorEntitiesByEmail(newDoctor.getEmail());
        doctor.setActiveStatus(2);
        doctorRepository.save(doctor);
    }
}
