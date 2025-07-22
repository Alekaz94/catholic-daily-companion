package com.alexandros.dailycompanion;

import com.alexandros.dailycompanion.Enum.Roles;
import com.alexandros.dailycompanion.Model.DailyReading;
import com.alexandros.dailycompanion.Model.JournalEntry;
import com.alexandros.dailycompanion.Model.Saint;
import com.alexandros.dailycompanion.Model.User;
import com.alexandros.dailycompanion.Repository.DailyReadingRepository;
import com.alexandros.dailycompanion.Repository.JournalEntryRepository;
import com.alexandros.dailycompanion.Repository.SaintRepository;
import com.alexandros.dailycompanion.Repository.UserRepository;
import com.alexandros.dailycompanion.Security.PasswordUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Optional;

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
			entry.setCreatedAt(LocalDate.now());
			entry.setUpdatedAt(LocalDate.now());
			entry.setUser(adminUser);
			journalEntryRepository.save(entry);
		};
	}

	@Bean
	@Order(3)
	CommandLineRunner initSaints(SaintRepository saintRepository) {
		return args -> {

			Saint saint = new Saint();
			saint.setName("St Example");
			saint.setBirthYear(100);
			saint.setDeathYear(200);
			saint.setFeastDay(MonthDay.of(12, 4));
			saint.setBiography("Sample biography for St Example.");
			saint.setPatronage("Sample patronage");
			saint.setCanonizationYear(300);
			saint.setImageUrl(null);
			saintRepository.save(saint);

			Saint saintTwo = new Saint();
			saintTwo.setName("St Example Two");
			saintTwo.setBirthYear(200);
			saintTwo.setDeathYear(300);
			saintTwo.setFeastDay(MonthDay.of(10, 18));
			saintTwo.setBiography("Sample biography for St Example Two.");
			saintTwo.setPatronage("Sample patronage");
			saintTwo.setCanonizationYear(400);
			saintTwo.setImageUrl(null);
			saintRepository.save(saintTwo);

			Saint saintThree = new Saint();
			saintThree.setName("St Example Two");
			saintThree.setBirthYear(200);
			saintThree.setDeathYear(300);
			saintThree.setFeastDay(MonthDay.of(7, 22));
			saintThree.setBiography("Sample biography for St Example Two.");
			saintThree.setPatronage("Sample patronage");
			saintThree.setCanonizationYear(400);
			saintThree.setImageUrl(null);
			saintRepository.save(saintThree);
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
