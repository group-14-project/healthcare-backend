package com.example.server.specialization;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class SpecializationService {
    private final SpecializationRepository specializationRepository;

    public SpecializationService(SpecializationRepository specializationRepository) {
        this.specializationRepository = specializationRepository;
    }

    public List<String> findAll() {
        return specializationRepository.findAllNames();
    }


    public static class SpecializationNotFoundException extends SecurityException{
        public SpecializationNotFoundException(){
            super("Specialization not found");
        }
    }
    public SpecializationEntity getSpecializationId(String name){
        SpecializationEntity newspecializationEntity = specializationRepository.findSpecializationEntityByName(name);
        return newspecializationEntity;
    }

    public SpecializationEntity addSpecialization(String name, String symptoms){
        SpecializationEntity newspecializationEntity = new SpecializationEntity();
        newspecializationEntity.setName(name);
        newspecializationEntity.setSymptoms(symptoms);

        return specializationRepository.save(newspecializationEntity);
    }

}
