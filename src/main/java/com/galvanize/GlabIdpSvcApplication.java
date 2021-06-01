package com.galvanize;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GlabIdpSvcApplication {

	public static void main(String[] args) {
		SpringApplication.run(GlabIdpSvcApplication.class, args);
	}

}
