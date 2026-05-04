package com.queuesense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling; // 🔥 ADD

@SpringBootApplication
@ComponentScan(basePackages = "com.queuesense")
@EnableScheduling // 🔥 IMPORTANT (real-time update ke liye)
public class QueuesenseApplication {

    public static void main(String[] args) {
        SpringApplication.run(QueuesenseApplication.class, args);
    }
}