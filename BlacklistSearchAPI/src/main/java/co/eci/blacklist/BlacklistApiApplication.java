package co.eci.blacklist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BlacklistApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlacklistApiApplication.class, args);
    }
}
