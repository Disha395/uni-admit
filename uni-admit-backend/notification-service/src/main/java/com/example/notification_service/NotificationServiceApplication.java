package com.example.notification_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class,
		org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class
})
public class NotificationServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
	}
}
