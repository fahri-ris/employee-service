package com.demo.employeeservice.repositories;

import com.demo.employeeservice.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmployeeName(String employeeName);
}
