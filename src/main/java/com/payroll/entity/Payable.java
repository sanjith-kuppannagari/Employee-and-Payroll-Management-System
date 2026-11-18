package com.payroll.entity;

public interface Payable {
    double TAX_RATE = 0.10;
    double calculateSalary();
    String getEmployeeType();
}
