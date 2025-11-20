package com.fsm.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main Spring Boot application class for Task Management Microservice
 */
@SpringBootApplication
@EnableAsync
public class TaskServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TaskServiceApplication.class, args);
    }
}
