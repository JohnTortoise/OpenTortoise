package io.github.johntortoise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TortoiseWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(TortoiseWebApplication.class, args);
    }

}