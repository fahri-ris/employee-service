package com.demo.employeeservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentResponseDto {
    private Long departmentId;
    private String departmentName;
    private String departmentAddress;
    private String departmentCode;
}
