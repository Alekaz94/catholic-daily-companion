/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion;

import com.alexandros.dailycompanion.initializer.DataSeeder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CatholicDailyCompanionApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatholicDailyCompanionApplication.class, args);
	}

	@Bean
	@Order(1)
	public CommandLineRunner seedData(DataSeeder dataSeeder) {
		return args -> {
			dataSeeder.seedSaintsIfEmpty();
		};
	}
}
