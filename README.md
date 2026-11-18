# Employee & Payroll Management System

A Core Java (Units 1–5) Employee & Payroll Management System built with `com.sun.net.httpserver.HttpServer`, SQLite JDBC, Multithreading, Byte Streams, Collections, and Generics, paired with an automated GitHub (`main`) → Vercel production deployment pipeline.

- **Live Production URL:** https://employee-payroll-system-mocha.vercel.app
- **Demo Credentials:** `admin` / `admin123` (configurable via `ADMIN_USERNAME` and `ADMIN_PASSWORD` environment variables)

## Syllabus Concepts Demonstrated
- **Unit 1 (OOP & Inheritance):** Abstract classes (`Person`, `BaseEmployee`), Interface (`Payable`), Multilevel & Hierarchical inheritance (`FullTimeEmployee`, `PartTimeEmployee`), `this`, `super`, `static`, `final`, packages, access specifiers.
- **Unit 2 (Polymorphism & Exceptions):** Constructor & method overloading, runtime polymorphism (`@Override calculateSalary()`), custom checked exceptions (`InvalidSalaryException`, `EmployeeNotFoundException`).
- **Unit 3 (Multithreading & I/O):** Synchronized background `Thread`/`Runnable` payslip export via `FileOutputStream` and `FileInputStream`.
- **Unit 4 (JDBC & Collections):** Pure SQLite JDBC CRUD (`CREATE TABLE`, `INSERT`, `SELECT`, `UPDATE`, `DELETE`) with `ArrayList`, `HashMap`, and `Iterator`.
- **Unit 5 (Generics):** Generic response wrapper `ApiResponse<T>`.

## Run Locally (Core Java + SQLite)
```bash
mvn clean compile exec:java
```
Open `http://localhost:8080/login.html`.
