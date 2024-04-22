package com.demo.employeeservice.services;

import com.demo.employeeservice.config.WebClientConfig;
import com.demo.employeeservice.dtos.DepartmentRequestDto;
import com.demo.employeeservice.dtos.DepartmentResponseDto;
import com.demo.employeeservice.dtos.EmployeeRequestDto;
import com.demo.employeeservice.dtos.EmployeeResponseDto;
import com.demo.employeeservice.models.Employee;
import com.demo.employeeservice.repositories.EmployeeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    WebClient webClient;
    EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeServiceImpl(WebClient webClient, EmployeeRepository employeeRepository) {
        this.webClient = webClient;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public EmployeeResponseDto getEmployee(Long employeeId) {
        try{
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

            DepartmentResponseDto departmentResponseDto = webClient
                    .get()
                    .uri("http://localhost:8081/department/" + employee.getDepartmentId())
                    .retrieve()
                    .bodyToMono(DepartmentResponseDto.class)
                    .block();

            EmployeeResponseDto employeeResponseDto = new EmployeeResponseDto().builder()
                    .employeeId(employee.getEmployeeId())
                    .employeeName(employee.getEmployeeName())
                    .department(departmentResponseDto)
                    .build();
            return employeeResponseDto;
        } catch(EntityNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch(WebClientResponseException e){
            // kode ini tidak menangkap message error yg di return oleh webclient dari service lain
//            if(e.getStatusCode() == HttpStatus.NOT_FOUND){
//                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
//            } else {
//                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
//            }

            // gunakan ini, parsing dari Json nya untuk mendapatkan error message nya
            if(e.getStatusCode().is4xxClientError() || e.getStatusCode().is5xxServerError()){
                String responseBody = e.getResponseBodyAsString();
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    String errorMessage = jsonNode.get("message").asText();
                    throw new ResponseStatusException(e.getStatusCode(), errorMessage);
                } catch (JsonProcessingException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        } catch(Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    @Transactional
    public EmployeeResponseDto createEmployee(EmployeeRequestDto employeeRequestDto) {
        try {
            DepartmentRequestDto departmentRequestDto = employeeRequestDto.getDepartment();
            DepartmentResponseDto departmentResponseDto =  new DepartmentResponseDto();

            if(!employeeRepository.existsByEmployeeName(employeeRequestDto.getEmployeeName())){
                departmentResponseDto = webClient
                        .post()
                        .uri("http://localhost:8081/department")
                        .body(Mono.just(departmentRequestDto), DepartmentRequestDto.class)
                        .retrieve()
                        .bodyToMono(DepartmentResponseDto.class)
                        .block();
            } else{
                throw new BadRequestException("Employee name already exists");
            }

            Employee employee = new Employee().builder()
                    .employeeName(employeeRequestDto.getEmployeeName())
                    .employeeEmail(employeeRequestDto.getEmployeeEmail())
                    .departmentId(departmentResponseDto.getDepartmentId())
                    .build();
            Employee savedEmployee = employeeRepository.save(employee);

            return EmployeeResponseDto.builder()
                    .employeeId(savedEmployee.getEmployeeId())
                    .employeeName(savedEmployee.getEmployeeName())
                    .department(departmentResponseDto)
                    .build();
        } catch (WebClientResponseException e){
            if(e.getStatusCode().is4xxClientError() || e.getStatusCode().is5xxServerError()){
                String responseBody = e.getResponseBodyAsString();
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    String errorMessage = jsonNode.get("message").asText();
                    throw new ResponseStatusException(e.getStatusCode(), errorMessage);
                } catch (JsonProcessingException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        } catch (BadRequestException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch(Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
