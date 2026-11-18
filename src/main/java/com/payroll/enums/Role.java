package com.payroll.enums;

public enum Role {
    ENGINEERING("Engineering & R&D"),
    HR("Human Resources"),
    FINANCE("Finance & Accounts"),
    MANAGEMENT("Executive Management"),
    OPERATIONS("Operations & Support");

    private final String department;

    Role(String department) {
        this.department = department;
    }

    public String getDepartment() {
        return this.department;
    }
}
