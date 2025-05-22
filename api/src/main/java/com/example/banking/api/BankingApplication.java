package com.example.banking.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.example.banking.core",
    "com.example.banking.domain",
    "com.example.banking.api"
})
@EntityScan(basePackages = {
    "com.example.banking.domain.account.entity",
    "com.example.banking.domain.transaction.entity"
})
@EnableJpaRepositories(basePackages = {
    "com.example.banking.domain.account.repository",
    "com.example.banking.domain.transaction.repository"
})
public class BankingApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankingApplication.class, args);
    }
} 