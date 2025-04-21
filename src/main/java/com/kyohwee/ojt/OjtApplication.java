package com.kyohwee.ojt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 이게 있어야 @Scheduled가 작동함!
public class OjtApplication {

	public static void main(String[] args) {
		SpringApplication.run(OjtApplication.class, args);
	}

}
