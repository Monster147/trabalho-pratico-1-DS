package pt.lisboapedala;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LisboaPedalaApplication {
    public static void main(String[] args) {
        SpringApplication.run(LisboaPedalaApplication.class, args);
    }
}
