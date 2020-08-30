package com.dailmer.service2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;

@SpringBootApplication
public class Service2Application {

	public static void main(String[] args) {
		SpringApplication.run(Service2Application.class, args);
	}

	@Bean
	public ProtobufHttpMessageConverter protobufHttpMessageConverter() {
		return new ProtobufHttpMessageConverter();
	}
	
	

}
