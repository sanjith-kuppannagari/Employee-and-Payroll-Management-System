package com.payroll.controller;

import com.payroll.dto.ApiResponse;
import com.payroll.entity.BaseEmployee;
import com.payroll.security.AuthService;
import com.payroll.service.PayrollService;
import com.payroll.util.JsonUtil;
import com.sun.net.httpserver.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class PayrollController implements HttpHandler {
    private final PayrollService payrollService = new PayrollService();
    private final AuthService authService = new AuthService();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        Headers h = ex.getResponseHeaders();
        h.add("Access-Control-Allow-Origin", "*");
        h.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        h.add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        String path = ex.getRequestURI().getPath();
        String method = ex.getRequestMethod();
        try {
            if (path.startsWith("/api/")) {
                handleApi(ex, path, method);
            } else {
                serveStaticFile(ex, path);
            }
        } catch (Exception e) {
            sendJson(ex, 400, new ApiResponse<String>(false, e.getMessage()).toJson(null));
        }
    }

    private void handleApi(HttpExchange ex, String path, String method) throws Exception {
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        HashMap<String, String> data = JsonUtil.parseJson(body);
        String query = ex.getRequestURI().getQuery();

        if ("/api/login".equals(path) && "POST".equals(method)) {
            boolean ok = authService.authenticate(data.get("username"), data.get("password"));
            String json = new ApiResponse<String>(ok, ok ? "Login successful" : "Invalid credentials").toJson(ok ? "\"ADMIN_TOKEN\"" : null);
            sendJson(ex, ok ? 200 : 401, json);
        } else if ("/api/employees".equals(path) && "GET".equals(method)) {
            if (query != null && query.startsWith("id=")) {
                int id = Integer.parseInt(query.substring(3));
                BaseEmployee emp = payrollService.getEmployeeById(id);
                sendJson(ex, 200, new ApiResponse<BaseEmployee>(true, "Fetched", emp).toJson(emp.toJson()));
            } else {
                String arr = JsonUtil.toJsonArray(payrollService.getAllEmployees());
                sendJson(ex, 200, new ApiResponse<String>(true, "Employees loaded").toJson(arr));
            }
        } else if ("/api/employees".equals(path) && ("POST".equals(method) || "PUT".equals(method))) {
            int id = Integer.parseInt(data.getOrDefault("id", "0"));
            BaseEmployee emp = payrollService.saveOrUpdate(id, data.get("name"), data.get("email"),
                    data.get("role"), data.get("type"),
                    Double.parseDouble(data.getOrDefault("basePay", "0")),
                    Double.parseDouble(data.getOrDefault("extra", "0")));
            sendJson(ex, 200, new ApiResponse<BaseEmployee>(true, "Employee saved", emp).toJson(emp.toJson()));
        } else if ("/api/employees".equals(path) && "DELETE".equals(method)) {
            int id = Integer.parseInt(query != null && query.startsWith("id=") ? query.substring(3) : data.getOrDefault("id", "0"));
            payrollService.removeEmployee(id);
            sendJson(ex, 200, new ApiResponse<String>(true, "Employee deleted").toJson(null));
        } else if ("/api/payroll/export".equals(path) && "POST".equals(method)) {
            int id = Integer.parseInt(data.getOrDefault("id", "0"));
            BaseEmployee emp = payrollService.getEmployeeById(id);
            String receipt = payrollService.exportPayslip(id);
            String payload = "{\"receipt\":\"" + JsonUtil.escape(receipt) + "\",\"file\":\"payslips/payslip_EMP_" + id + ".txt\",\"employee\":" + emp.toJson() + "}";
            sendJson(ex, 200, new ApiResponse<String>(true, "Payslip exported via Thread & FileOutputStream").toJson(payload));
        } else {
            sendJson(ex, 404, new ApiResponse<String>(false, "Endpoint not found").toJson(null));
        }
    }

    private void serveStaticFile(HttpExchange ex, String path) throws IOException {
        String filePath = switch (path) {
            case "/", "/login", "/login.html" -> "src/main/resources/templates/auth/login.html";
            case "/dashboard", "/dashboard.html" -> "src/main/resources/templates/admin/dashboard.html";
            case "/payslip", "/payslip.html" -> "src/main/resources/templates/employee/payslip.html";
            case "/css/style.css" -> "src/main/resources/static/css/style.css";
            case "/js/app.js" -> "src/main/resources/static/js/app.js";
            default -> "src/main/resources/templates/auth/login.html";
        };
        File file = new File(filePath);
        if (!file.exists()) {
            ex.sendResponseHeaders(404, -1);
            return;
        }
        String mime = path.endsWith(".css") ? "text/css" : path.endsWith(".js") ? "application/javascript" : "text/html";
        ex.getResponseHeaders().set("Content-Type", mime + "; charset=UTF-8");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] bytes = fis.readAllBytes();
            ex.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
        }
    }

    private void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }
}
