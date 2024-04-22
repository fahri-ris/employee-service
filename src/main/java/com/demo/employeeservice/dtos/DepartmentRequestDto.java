package com.demo.employeeservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepartmentRequestDto {
    private String departmentName;
    private String departmentAddress;
    private String departmentCode;
}
