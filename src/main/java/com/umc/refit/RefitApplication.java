package com.umc.refit;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class RefitApplication {

	public static void main(String[] args) {
		SpringApplication.run(RefitApplication.class, args);
	}
}