package com.alexandros.dailycompanion;

import com.alexandros.dailycompanion.dto.SaintDto;
import com.alexandros.dailycompanion.enums.Roles;
import com.alexandros.dailycompanion.initializer.DataSeeder;
import com.alexandros.dailycompanion.model.DailyReading;
import com.alexandros.dailycompanion.model.JournalEntry;
import com.alexandros.dailycompanion.model.Saint;
import com.alexandros.dailycompanion.model.User;
import com.alexandros.dailycompanion.repository.DailyReadingRepository;
import com.alexandros.dailycompanion.repository.JournalEntryRepository;
import com.alexandros.dailycompanion.repository.SaintRepository;
import com.alexandros.dailycompanion.repository.UserRepository;
import com.alexandros.dailycompanion.security.PasswordUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.MonthDayDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SpringBootApplication
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
	CommandLineRunner initJournalEntries(UserRepository userRepository,
										 @Value("${admin.default.email}") String adminEmail,
										 JournalEntryRepository journalEntryRepository) {
		return args -> {
			User adminUser = userRepository.findByEmail(adminEmail).orElseThrow();
			JournalEntry entry = new JournalEntry();
			entry.setTitle("Example Entry");
			entry.setContent("This is an example journal entry.");
			entry.setCreatedAt(LocalDate.of(2024, 5, 10));
			entry.setUpdatedAt(LocalDate.of(2024, 5,12));
			entry.setUser(adminUser);
			journalEntryRepository.save(entry);
		};
	}

	@Bean
	@Order(3)
	public CommandLineRunner seedData(DataSeeder dataSeeder) {
		return args -> {
			dataSeeder.seedSaintsIfEmpty();
		};
	}

	@Bean
	@Order(4)
	CommandLineRunner initReadings(DailyReadingRepository dailyReadingRepository) {
		return args -> {
			DailyReading reading = new DailyReading();
			reading.setCreatedAt(LocalDate.now());
			reading.setFirstReading("Genesis");
			reading.setSecondReading("Romans");
			reading.setPsalm("Psalm 23");
			reading.setGospel("John");
			dailyReadingRepository.save(reading);

			DailyReading readingTwo = new DailyReading();
			readingTwo.setCreatedAt(LocalDate.of(2025, 5, 12));
			readingTwo.setFirstReading("Exodus");
			readingTwo.setSecondReading("Hebrews");
			readingTwo.setPsalm("Psalm 24");
			readingTwo.setGospel("Matthew");
			dailyReadingRepository.save(readingTwo);

			DailyReading readingThree = new DailyReading();
			readingThree.setCreatedAt(LocalDate.of(2025, 2, 10));
			readingThree.setFirstReading("Exodus");
			readingThree.setSecondReading("Hebrews");
			readingThree.setPsalm("Psalm 24");
			readingThree.setGospel("Matthew");
			dailyReadingRepository.save(readingThree);
		};
	}
}
