package com.demo.employeeservice.controllers;

import com.demo.employeeservice.dtos.EmployeeRequestDto;
import com.demo.employeeservice.dtos.EmployeeResponseDto;
import com.demo.employeeservice.services.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee")
public class EmployeeController {
    EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<EmployeeResponseDto> getEmployee(@PathVariable("employeeId") Long employeeId) {
        return ResponseEntity.ok(employeeService.getEmployee(employeeId));
    }

    @PostMapping()
    public ResponseEntity<EmployeeResponseDto> createEmployee(@RequestBody EmployeeRequestDto employeeRequestDto){
        return ResponseEntity.ok(employeeService.createEmployee(employeeRequestDto));
    }
}
