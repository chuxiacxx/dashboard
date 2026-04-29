package com.fusemi.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DashboardApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DashboardApplication.class);
        app.setAdditionalProfiles("local");
        app.run(args);
    }

}
