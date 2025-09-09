package com.alexandros.dailycompanion;

import com.alexandros.dailycompanion.enums.Roles;
import com.alexandros.dailycompanion.initializer.DataSeeder;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.repository.UserRepository;
import com.alexandros.dailycompanion.security.PasswordUtil;
import org.springframework.beans.factory.annotation.Value;
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
	CommandLineRunner initUsers(UserRepository userRepository,
								@Value("${admin.default.email}") String adminEmail,
								@Value("${admin.default.password:changeMe123}") String adminPassword) {
		return args -> {
			if (userRepository.findByEmail(adminEmail).isEmpty()) {
				User adminUser = new User();
				adminUser.setFirstName("Admin");
				adminUser.setLastName("User");
				adminUser.setEmail(adminEmail);
				adminUser.setPassword(PasswordUtil.hashPassword(adminPassword));
				adminUser.setRole(Roles.ADMIN);
				userRepository.save(adminUser);
				System.out.println("Admin user created: " + adminEmail);
			} else {
				System.out.println("Admin user already exists: " + adminEmail);
			}
		};
	}

	@Bean
	@Order(2)
	public CommandLineRunner seedData(DataSeeder dataSeeder) {
		return args -> {
			dataSeeder.seedSaintsIfEmpty();
		};
	}
}
