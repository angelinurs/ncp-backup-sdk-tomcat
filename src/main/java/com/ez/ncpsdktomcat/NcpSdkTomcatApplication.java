package com.ez.ncpsdktomcat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NcpSdkTomcatApplication {

	public static void main(String[] args) {
		SpringApplication.run(NcpSdkTomcatApplication.class, args);
	}
}
