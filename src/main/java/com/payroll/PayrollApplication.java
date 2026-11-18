package com.payroll;

import com.payroll.config.DatabaseConfig;
import com.payroll.controller.PayrollController;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class PayrollApplication {
    public static void main(String[] args) throws IOException {
        DatabaseConfig.initDatabase();
        int port = Integer.parseInt(DatabaseConfig.getProperty("server.port", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new PayrollController());
        server.setExecutor(null);
        server.start();
        System.out.println("====================================================");
        System.out.println(" Employee & Payroll System Started on Port " + port);
        System.out.println(" Login UI    : http://localhost:" + port + "/login.html");
        System.out.println(" Dashboard UI: http://localhost:" + port + "/dashboard.html");
        System.out.println("====================================================");
    }
}
