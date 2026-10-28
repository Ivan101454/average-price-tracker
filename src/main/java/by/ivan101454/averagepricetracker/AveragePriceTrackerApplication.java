package by.ivan101454.averagepricetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class AveragePriceTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AveragePriceTrackerApplication.class, args);
	}

}
