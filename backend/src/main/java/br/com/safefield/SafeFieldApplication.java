package br.com.safefield;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
public class SafeFieldApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
        SpringApplication.run(SafeFieldApplication.class, args);
    }
}
