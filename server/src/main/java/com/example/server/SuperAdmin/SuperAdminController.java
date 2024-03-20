package com.example.server.SuperAdmin;


import com.example.server.dto.request.HospitalRequestDto;
import com.example.server.dto.request.ResponseDto;
import com.example.server.hospital.HospitalEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/")
public class SuperAdminController
{
    @Autowired
    SuperAdminService superAdminService;

    @PostMapping("/hospital/register")
    public ResponseEntity<ResponseDto> registerHospital(@RequestBody HospitalRequestDto hospitalRequestDto)
    {
        ResponseDto res=this.superAdminService.registerHospital(hospitalRequestDto);
        return new ResponseEntity<ResponseDto>(res, HttpStatus.OK);
    }
}