package com.payroll.entity;

import com.payroll.enums.Role;

abstract class Person {
    protected String name;
    protected String email;

    protected Person(String name, String email) {
        this.name = name;
        this.email = email;
    }
}

public abstract class BaseEmployee extends Person implements Payable {
    public static final String COMPANY_NAME = "TechCorp Payroll Systems";
    private static int totalInstances = 0;

    protected int id;
    protected Role role;
    protected double basePay;

    public BaseEmployee(int id, String name, String email, Role role, double basePay) {
        super(name, email);
        this.id = id;
        this.role = role;
        this.basePay = basePay;
        totalInstances++;
    }

    public BaseEmployee(String name, String email, Role role, double basePay) {
        this(0, name, email, role, basePay);
    }

    // Method Overloading (Compile-time Polymorphism)
    public double calculateSalary(double extraBonus) {
        return calculateSalary() + extraBonus;
    }

    public static int getTotalInstances() { return totalInstances; }
    public int getId() { return this.id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return this.name; }
    public String getEmail() { return this.email; }
    public Role getRole() { return this.role; }
    public double getBasePay() { return this.basePay; }
    public abstract double getAllowanceOrHours();

    public String toJson() {
        return String.format(
            "{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\",\"role\":\"%s\",\"department\":\"%s\",\"type\":\"%s\",\"basePay\":%.2f,\"extra\":%.2f,\"netSalary\":%.2f}",
            id, name, email, role.name(), role.getDepartment(), getEmployeeType(), basePay, getAllowanceOrHours(), calculateSalary()
        );
    }
}
