package com.disaster.config;

import com.disaster.entity.Role;
import com.disaster.entity.User;
import com.disaster.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Bootstrap for the {@code postgres} (production) profile.
 *
 * {@link DataSeeder} is deliberately excluded from the postgres profile, so a
 * freshly provisioned PostgreSQL database would otherwise contain zero users
 * and nobody — including the admin — could ever log in. This runner creates
 * the well-known demo accounts ONLY when the {@code users} table is completely
 * empty. It never touches, overwrites or repairs an existing production
 * database that already has users.
 */
@Component
@Order(1)
@Profile("postgres")
public class PostgresAdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PostgresAdminSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PostgresAdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Postgres bootstrap skipped: users table already contains accounts.");
            return;
        }
        log.warn("Postgres database has no users; creating bootstrap admin account (admin/admin123). "
                + "Change this password immediately after first login.");
        User admin = new User("admin", "admin@disaster.com",
                passwordEncoder.encode("admin123"), Role.ADMIN);
        userRepository.save(admin);
        log.info("Postgres bootstrap admin created with role {}", admin.getRole());
    }
}
