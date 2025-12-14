package com.kindora.kindora_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.kindora.kindora_backend.model")
@EnableJpaRepositories(basePackages = "com.kindora.kindora_backend.repository")
public class KindoraBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(KindoraBackendApplication.class, args);
    }
}
