package com.payroll.service;

import com.payroll.entity.*;
import com.payroll.enums.Role;
import com.payroll.exception.*;
import com.payroll.repository.EmployeeRepository;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class PayrollService {
    private final EmployeeRepository repository = new EmployeeRepository();
    private static final Object FILE_LOCK = new Object();

    public ArrayList<BaseEmployee> getAllEmployees() { return repository.findAll(); }

    public BaseEmployee getEmployeeById(int id) throws EmployeeNotFoundException { return repository.findById(id); }

    public BaseEmployee saveOrUpdate(int id, String name, String email, String roleStr, String type, double basePay, double extra)
            throws InvalidSalaryException, SQLException, EmployeeNotFoundException {
        if (basePay <= 0 || extra < 0) {
            throw new InvalidSalaryException("Invalid salary: Base Pay must be > 0 and Bonus/Hours >= 0.");
        }
        Role role = Role.valueOf(roleStr.toUpperCase());
        BaseEmployee emp = "PART_TIME".equalsIgnoreCase(type)
                ? new PartTimeEmployee(id, name, email, role, basePay, (int) extra)
                : new FullTimeEmployee(id, name, email, role, basePay, extra);
        return (id > 0) ? repository.update(emp) : repository.save(emp);
    }

    public void removeEmployee(int id) throws SQLException, EmployeeNotFoundException { repository.deleteById(id); }

    public String exportPayslip(int id) throws EmployeeNotFoundException, IOException, InterruptedException {
        final BaseEmployee emp = repository.findById(id);
        final File dir = new File("payslips");
        if (!dir.exists()) dir.mkdirs();
        final File file = new File(dir, "payslip_EMP_" + emp.getId() + ".txt");

        Thread writerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (FILE_LOCK) {
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(file);
                        fos.write(buildReceiptText(emp).getBytes());
                        fos.flush();
                    } catch (IOException e) {
                        System.err.println("Write error: " + e.getMessage());
                    } finally {
                        if (fos != null) try { fos.close(); } catch (IOException ignored) {}
                    }
                }
            }
        });
        writerThread.start();
        writerThread.join();

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            return new String(fis.readAllBytes());
        } finally {
            if (fis != null) fis.close();
        }
    }

    private String buildReceiptText(BaseEmployee e) {
        String extraLabel = "PART_TIME".equals(e.getEmployeeType()) ? "Hours Worked" : "Performance Bonus";
        return "================================================\n" +
               "          " + BaseEmployee.COMPANY_NAME + "\n" +
               "               OFFICIAL PAYSLIP RECEIPT         \n" +
               "================================================\n" +
               "Employee ID    : EMP-" + e.getId() + "\n" +
               "Full Name      : " + e.getName() + "\n" +
               "Email          : " + e.getEmail() + "\n" +
               "Department     : " + e.getRole().getDepartment() + " (" + e.getRole().name() + ")\n" +
               "Employment Type: " + e.getEmployeeType() + "\n" +
               "------------------------------------------------\n" +
               String.format("Base Rate/Pay  : Rs. %.2f\n%-15s: %.2f\nTax Rate       : %.0f%%\n",
                       e.getBasePay(), extraLabel, e.getAllowanceOrHours(), Payable.TAX_RATE * 100) +
               "------------------------------------------------\n" +
               String.format("NET SALARY     : Rs. %.2f\n", e.calculateSalary()) +
               "================================================\n";
    }
}
