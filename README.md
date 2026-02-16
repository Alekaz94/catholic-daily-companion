# Catholic Daily Companion – Backend

Backend service for the **Catholic Daily Companion** mobile app (React Native + Expo).  
Provides user authentication, rosary streak tracking, daily readings, profile editing, and secure session management.

---

## Features

-  JWT Authentication
-  Refresh Token Support
-  Firebase Identity Integration
- ‍️ User system (registration, profile, updates)
-  Daily spiritual content API
-  Spring Security 6
-  PostgreSQL Database

---

## Tech Stack

| Category       | Technology                    |
|----------------|-------------------------------|
| Language       | Java 17                       |
| Framework      | Spring Boot 3.5.x             |
| Security       | Spring Security 6, JWT (jjwt) |
| Database       | PostgreSQL                    |
| Build Tool     | Maven                         |

---

## Getting Started (Local Development)

### 1️⃣ Clone the repository
```bash
git clone https://github.com/Alekaz94/catholic-daily-companion.git
cd dailycompanion
```

2️⃣ Create Local Environment File

Inside src/main/resources/, 

create application.properties

```bash
spring.application.name=Catholic Daily Companion

# Local PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/catholic-daily-companion
spring.datasource.username=<postgres>
spring.datasource.password=<yourpassword>
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# JWT
jwt.secret=<local-secret-key>
jwt.accessExpiration=3600
jwt.refreshExpiration=604800

# Admin for seeding
admin.default.email=admin@example.com
admin.default.password=SomeSecurePassword123!

# CORS
app.cors.allowed-origins=http://localhost:8081,http://192.168.0.101:8081

server.port=8080
```
Add the code below to class CatholicDailyCompanionApplication to initialize an admin user.
```bash
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
```

3️⃣ Run the backend

The backend will run at:
 http://localhost:8080

---
## License

This project is licensed under the MIT License.

---

## Author

Created by Alexandros Kazalis

Developer of the Catholic Daily Companion app.