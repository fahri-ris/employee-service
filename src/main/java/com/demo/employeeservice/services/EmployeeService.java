package com.demo.employeeservice.services;

import com.demo.employeeservice.dtos.EmployeeRequestDto;
import com.demo.employeeservice.dtos.EmployeeResponseDto;

public interface EmployeeService {
    EmployeeResponseDto getEmployee(Long departmentId);
    EmployeeResponseDto createEmployee(EmployeeRequestDto employeeRequestDto);
}
