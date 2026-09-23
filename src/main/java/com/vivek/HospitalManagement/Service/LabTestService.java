package com.vivek.HospitalManagement.Service;



import com.vivek.HospitalManagement.DTO.Auth.Response.LabTestResponse;
import com.vivek.HospitalManagement.Repository.LabTestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LabTestService {

    private final LabTestRepository labTestRepository;


    public LabTestService(LabTestRepository labTestRepository) {
        this.labTestRepository = labTestRepository;
    }

    public List<LabTestResponse> getActiveTests() {
        return labTestRepository.findActiveTests();
    }

}
