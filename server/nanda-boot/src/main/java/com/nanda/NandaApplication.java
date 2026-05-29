package com.nanda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.nanda")
@EnableScheduling
public class NandaApplication {

    public static void main(String[] args) {
        SpringApplication.run(NandaApplication.class, args);
    }
}
