package il.ac.afeka.energyservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EnergyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(il.ac.afeka.energyservice.EnergyServiceApplication.class, args);
    }

}
