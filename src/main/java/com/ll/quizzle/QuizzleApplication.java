package com.ll.quizzle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class QuizzleApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuizzleApplication.class, args);
    }

}
