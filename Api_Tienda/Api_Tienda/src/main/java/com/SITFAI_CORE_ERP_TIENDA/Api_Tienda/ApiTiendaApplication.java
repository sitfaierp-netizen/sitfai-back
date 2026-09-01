package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.SITFAI_CORE_ERP_TIENDA")
@ComponentScan(basePackages = {"com.SITFAI_CORE_ERP_TIENDA"})
@EnableJpaRepositories(basePackages = "com.SITFAI_CORE_ERP_TIENDA")
@EntityScan(basePackages = "com.SITFAI_CORE_ERP_TIENDA")
public class ApiTiendaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiTiendaApplication.class, args);
	}

}
