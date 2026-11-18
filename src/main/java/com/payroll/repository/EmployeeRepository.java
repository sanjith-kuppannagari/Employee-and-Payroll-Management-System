package com.payroll.repository;

import com.payroll.config.DatabaseConfig;
import com.payroll.entity.*;
import com.payroll.enums.Role;
import com.payroll.exception.EmployeeNotFoundException;
import java.sql.*;
import java.util.*;

public class EmployeeRepository {
    private final ArrayList<BaseEmployee> employeeList = new ArrayList<>();
    private final HashMap<Integer, BaseEmployee> employeeCache = new HashMap<>();

    public synchronized ArrayList<BaseEmployee> findAll() {
        employeeList.clear();
        employeeCache.clear();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM employees ORDER BY id ASC")) {
            while (rs.next()) {
                BaseEmployee emp = mapRow(rs);
                employeeList.add(emp);
                employeeCache.put(emp.getId(), emp);
            }
        } catch (SQLException e) { System.err.println("SELECT Error: " + e.getMessage()); }
        return employeeList;
    }

    public BaseEmployee findById(int id) throws EmployeeNotFoundException {
        if (!employeeCache.containsKey(id)) findAll();
        if (!employeeCache.containsKey(id)) throw new EmployeeNotFoundException("Employee ID " + id + " not found.");
        return employeeCache.get(id);
    }

    public BaseEmployee save(BaseEmployee emp) throws SQLException {
        String sql = "INSERT INTO employees (name, email, role, type, base_pay, extra) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindParams(ps, emp);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) emp.setId(keys.getInt(1));
            employeeList.add(emp);
            employeeCache.put(emp.getId(), emp);
            return emp;
        }
    }

    public BaseEmployee update(BaseEmployee emp) throws SQLException, EmployeeNotFoundException {
        String sql = "UPDATE employees SET name=?, email=?, role=?, type=?, base_pay=?, extra=? WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            bindParams(ps, emp);
            ps.setInt(7, emp.getId());
            if (ps.executeUpdate() == 0) throw new EmployeeNotFoundException("Employee ID " + emp.getId() + " not found.");
            findAll();
            return emp;
        }
    }

    public void deleteById(int id) throws SQLException, EmployeeNotFoundException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM employees WHERE id = ?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) throw new EmployeeNotFoundException("Employee ID " + id + " not found.");
            Iterator<BaseEmployee> it = employeeList.iterator();
            while (it.hasNext()) { if (it.next().getId() == id) it.remove(); }
            employeeCache.remove(id);
        }
    }

    private void bindParams(PreparedStatement ps, BaseEmployee emp) throws SQLException {
        ps.setString(1, emp.getName());
        ps.setString(2, emp.getEmail());
        ps.setString(3, emp.getRole().name());
        ps.setString(4, emp.getEmployeeType());
        ps.setDouble(5, emp.getBasePay());
        ps.setDouble(6, emp.getAllowanceOrHours());
    }

    private BaseEmployee mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        Role role = Role.valueOf(rs.getString("role"));
        double base = rs.getDouble("base_pay"), extra = rs.getDouble("extra");
        return "PART_TIME".equals(rs.getString("type"))
                ? new PartTimeEmployee(id, rs.getString("name"), rs.getString("email"), role, base, (int) extra)
                : new FullTimeEmployee(id, rs.getString("name"), rs.getString("email"), role, base, extra);
    }
}
