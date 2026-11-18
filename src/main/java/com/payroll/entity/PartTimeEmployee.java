package com.payroll.entity;

import com.payroll.enums.Role;

public class PartTimeEmployee extends BaseEmployee {
    private final int hoursWorked;

    public PartTimeEmployee(int id, String name, String email, Role role, double hourlyRate, int hoursWorked) {
        super(id, name, email, role, hourlyRate);
        this.hoursWorked = hoursWorked;
    }

    public PartTimeEmployee(String name, String email, Role role, double hourlyRate) {
        this(0, name, email, role, hourlyRate, 80);
    }

    @Override
    public double calculateSalary() {
        double gross = this.basePay * this.hoursWorked;
        return gross - (gross * (TAX_RATE / 2.0));
    }

    @Override
    public String getEmployeeType() { return "PART_TIME"; }

    @Override
    public double getAllowanceOrHours() { return this.hoursWorked; }
}
