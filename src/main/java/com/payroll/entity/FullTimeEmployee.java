package com.payroll.entity;

import com.payroll.enums.Role;

public class FullTimeEmployee extends BaseEmployee {
    private final double bonus;

    public FullTimeEmployee(int id, String name, String email, Role role, double basePay, double bonus) {
        super(id, name, email, role, basePay);
        this.bonus = bonus;
    }

    public FullTimeEmployee(String name, String email, Role role, double basePay) {
        this(0, name, email, role, basePay, basePay * 0.15);
    }

    @Override
    public double calculateSalary() {
        double hra = this.basePay * 0.20;
        double tax = (this.basePay + hra + this.bonus) * TAX_RATE;
        return this.basePay + hra + this.bonus - tax;
    }

    @Override
    public String getEmployeeType() { return "FULL_TIME"; }

    @Override
    public double getAllowanceOrHours() { return this.bonus; }
}
