package com.disaster.config;

import com.disaster.entity.*;
import com.disaster.enums.*;
import com.disaster.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * Seeds demo data (including well-known demo credentials) for local
 * development only. Never runs on the production (postgres) profile.
 * {@code @Order(1)} guarantees seeding completes before the AI self-test
 * ({@code @Order(2)}) and other startup runners that depend on seed data.
 */
@Component
@Order(1)
@Profile("!postgres")
public class DataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final DisasterRepository disasterRepository;
    private final PasswordEncoder passwordEncoder;
    private final HospitalRepository hospitalRepository;
    private final ShelterRepository shelterRepository;
    private final VolunteerRepository volunteerRepository;
    private final ResourceRepository resourceRepository;
    private final DroneRepository droneRepository;
    private final NotificationRepository notificationRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final CaseStudyRepository caseStudyRepository;
    private final RescueTeamRepository rescueTeamRepository;
    private final TeamMemberRepository teamMemberRepository;

    public DataSeeder(UserRepository userRepository, DisasterRepository disasterRepository,
                      PasswordEncoder passwordEncoder, HospitalRepository hospitalRepository,
                      ShelterRepository shelterRepository, VolunteerRepository volunteerRepository,
                      ResourceRepository resourceRepository, DroneRepository droneRepository,
                      NotificationRepository notificationRepository,
                      SystemSettingRepository systemSettingRepository,
                      CaseStudyRepository caseStudyRepository,
                      RescueTeamRepository rescueTeamRepository,
                      TeamMemberRepository teamMemberRepository) {
        this.userRepository = userRepository;
        this.disasterRepository = disasterRepository;
        this.passwordEncoder = passwordEncoder;
        this.hospitalRepository = hospitalRepository;
        this.shelterRepository = shelterRepository;
        this.volunteerRepository = volunteerRepository;
        this.resourceRepository = resourceRepository;
        this.droneRepository = droneRepository;
        this.notificationRepository = notificationRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.caseStudyRepository = caseStudyRepository;
        this.rescueTeamRepository = rescueTeamRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @Override
    public void run(String... args) {
        // Well-known demo accounts are created idempotently (per user) so that
        // login never fails because the seed guard skipped them on a partially
        // seeded database. The rest of the demo data is only seeded once.
        User admin = createUserIfMissing("admin", "admin@disaster.com", "admin123", Role.ADMIN);
        User user = createUserIfMissing("user", "user@disaster.com", "user123", Role.USER);
        User dmo = createUserIfMissing("dmo", "dmo@disaster.com", "dmo123", Role.DMO);
        User police = createUserIfMissing("police", "police@disaster.com", "police123", Role.POLICE);
        User fire = createUserIfMissing("fire", "fire@disaster.com", "fire123", Role.FIRE_DEPARTMENT);
        User hospital = createUserIfMissing("hospital", "hospital@disaster.com", "hospital123", Role.HOSPITAL_STAFF);
        User rescue = createUserIfMissing("rescue", "rescue@disaster.com", "rescue123", Role.RESCUE_TEAM);
        User volunteer = createUserIfMissing("volunteer", "volunteer@disaster.com", "volunteer123", Role.VOLUNTEER_COORDINATOR);
        User ngo = createUserIfMissing("ngo", "ngo@disaster.com", "ngo123", Role.NGO_COORDINATOR);

        log.info("Demo accounts ready. Admin login: admin / admin123 (BCrypt verified: {})",
                passwordEncoder.matches("admin123", admin.getPassword()));

        if (disasterRepository.count() > 0) return;

        // Disasters
        Disaster d1 = new Disaster();
        d1.setDisasterType("Flood"); d1.setDescription("Severe flooding in low-lying areas. Many homes submerged.");
        d1.setSeverity("High"); d1.setLocation("Mumbai, Maharashtra");
        d1.setLatitude(19.0760); d1.setLongitude(72.8777);
        d1.setDate(LocalDateTime.now().minusDays(2)); d1.setStatus(DisasterStatus.IN_PROGRESS); d1.setUser(admin);
        disasterRepository.save(d1);

        Disaster d2 = new Disaster();
        d2.setDisasterType("Earthquake"); d2.setDescription("Moderate earthquake tremors felt across the region.");
        d2.setSeverity("Critical"); d2.setLocation("Guwahati, Assam");
        d2.setLatitude(26.1445); d2.setLongitude(91.7362);
        d2.setDate(LocalDateTime.now().minusDays(1)); d2.setStatus(DisasterStatus.ASSIGNED); d2.setUser(admin);
        disasterRepository.save(d2);

        Disaster d3 = new Disaster();
        d3.setDisasterType("Cyclone"); d3.setDescription("Cyclone approaching the eastern coast. Evacuation underway.");
        d3.setSeverity("High"); d3.setLocation("Chennai, Tamil Nadu");
        d3.setLatitude(13.0827); d3.setLongitude(80.2707);
        d3.setDate(LocalDateTime.now()); d3.setStatus(DisasterStatus.PENDING); d3.setUser(user);
        disasterRepository.save(d3);

        Disaster d4 = new Disaster();
        d4.setDisasterType("Wildfire"); d4.setDescription("Forest fire spreading rapidly due to dry conditions.");
        d4.setSeverity("Medium"); d4.setLocation("Dehradun, Uttarakhand");
        d4.setLatitude(30.3165); d4.setLongitude(78.0322);
        d4.setDate(LocalDateTime.now().minusHours(6)); d4.setStatus(DisasterStatus.RESOLVED); d4.setUser(user);
        disasterRepository.save(d4);

        // Hospitals
        hospitalRepository.save(Hospital.builder().name("City General Hospital").availableBeds(120).icuBeds(30)
            .doctorsAvailable(45).emergencyContact("022-23456789").bloodBank(true)
            .latitude(19.0780).longitude(72.8780).address("Mumbai Central").build());
        hospitalRepository.save(Hospital.builder().name("District Medical Center").availableBeds(80).icuBeds(15)
            .doctorsAvailable(25).emergencyContact("0361-2345678").bloodBank(true)
            .latitude(26.1450).longitude(91.7370).address("Guwahati").build());
        hospitalRepository.save(Hospital.builder().name("Coastal Care Hospital").availableBeds(200).icuBeds(40)
            .doctorsAvailable(60).emergencyContact("044-23456789").bloodBank(true)
            .latitude(13.0830).longitude(80.2710).address("Chennai").build());

        // Shelters
        shelterRepository.save(Shelter.builder().name("Community Hall Shelter").capacity(500).occupancy(200)
            .foodAvailable(true).waterAvailable(true).medicalKits(100).powerAvailable(true)
            .contact("9876543210").latitude(19.0800).longitude(72.8800).address("Mumbai").build());
        shelterRepository.save(Shelter.builder().name("School Shelter Camp").capacity(300).occupancy(150)
            .foodAvailable(true).waterAvailable(true).medicalKits(50).powerAvailable(true)
            .contact("9876543211").latitude(26.1470).longitude(91.7390).address("Guwahati").build());
        shelterRepository.save(Shelter.builder().name("Convention Center Shelter").capacity(1000).occupancy(400)
            .foodAvailable(true).waterAvailable(false).medicalKits(200).powerAvailable(true)
            .contact("9876543212").latitude(13.0850).longitude(80.2730).address("Chennai").build());

        // Volunteers
        Volunteer v1 = new Volunteer(); v1.setName("Rahul Sharma"); v1.setEmail("rahul@example.com");
        v1.setPhone("9876543213"); v1.setSkills("First Aid, Rescue"); v1.setAvailable(true);
        v1.setLatitude(19.0820); v1.setLongitude(72.8820); volunteerRepository.save(v1);

        Volunteer v2 = new Volunteer(); v2.setName("Priya Patel"); v2.setEmail("priya@example.com");
        v2.setPhone("9876543214"); v2.setSkills("Medical, Nursing"); v2.setAvailable(true);
        v2.setLatitude(26.1490); v2.setLongitude(91.7410); volunteerRepository.save(v2);

        Volunteer v3 = new Volunteer(); v3.setName("Arun Kumar"); v3.setEmail("arun@example.com");
        v3.setPhone("9876543215"); v3.setSkills("Driving, Logistics"); v3.setAvailable(true);
        v3.setLatitude(13.0870); v3.setLongitude(80.2750); volunteerRepository.save(v3);

        // Resources
        Resource r1 = new Resource(); r1.setResourceType(ResourceType.AMBULANCE); r1.setQuantity(10); r1.setAvailable(true);
        r1.setLocation("Mumbai"); r1.setLatitude(19.0760); r1.setLongitude(72.8777); resourceRepository.save(r1);
        Resource r2 = new Resource(); r2.setResourceType(ResourceType.FIRE_TRUCK); r2.setQuantity(5); r2.setAvailable(true);
        r2.setLocation("Guwahati"); r2.setLatitude(26.1445); r2.setLongitude(91.7362); resourceRepository.save(r2);
        Resource r3 = new Resource(); r3.setResourceType(ResourceType.MEDICAL_TEAM); r3.setQuantity(15); r3.setAvailable(true);
        r3.setLocation("Chennai"); r3.setLatitude(13.0827); r3.setLongitude(80.2707); resourceRepository.save(r3);
        Resource r4 = new Resource(); r4.setResourceType(ResourceType.BOAT); r4.setQuantity(8); r4.setAvailable(true);
        r4.setLocation("Mumbai"); r4.setLatitude(19.0760); r4.setLongitude(72.8777); resourceRepository.save(r4);
        Resource r5 = new Resource(); r5.setResourceType(ResourceType.HELICOPTER); r5.setQuantity(2); r5.setAvailable(true);
        r5.setLocation("Guwahati"); r5.setLatitude(26.1445); r5.setLongitude(91.7362); resourceRepository.save(r5);

        // Drones
        Drone dr1 = new Drone(); dr1.setDroneId("DRN-001"); dr1.setStatus(DroneStatus.AVAILABLE);
        dr1.setBattery(85); dr1.setCameraStatus(true); dr1.setLatitude(19.0760); dr1.setLongitude(72.8777);
        dr1.setMissionStatus(MissionStatus.PENDING); droneRepository.save(dr1);

        Drone dr2 = new Drone(); dr2.setDroneId("DRN-002"); dr2.setStatus(DroneStatus.IN_MISSION);
        dr2.setBattery(60); dr2.setCameraStatus(true); dr2.setLatitude(26.1445); dr2.setLongitude(91.7362);
        dr2.setAssignedDisaster(d2); dr2.setMissionStatus(MissionStatus.IN_PROGRESS); droneRepository.save(dr2);

        Drone dr3 = new Drone(); dr3.setDroneId("DRN-003"); dr3.setStatus(DroneStatus.CHARGING);
        dr3.setBattery(20); dr3.setCameraStatus(true); dr3.setLatitude(13.0827); dr3.setLongitude(80.2707);
        dr3.setMissionStatus(MissionStatus.COMPLETED); droneRepository.save(dr3);

        // System Settings
        SystemSetting ss1 = new SystemSetting(); ss1.setSettingKey("system.name");
        ss1.setSettingValue("AI Disaster Management System"); ss1.setDescription("System display name");
        systemSettingRepository.save(ss1);
        SystemSetting ss2 = new SystemSetting(); ss2.setSettingKey("system.version");
        ss2.setSettingValue("3.0.0"); ss2.setDescription("System version");
        systemSettingRepository.save(ss2);
        SystemSetting ss3 = new SystemSetting(); ss3.setSettingKey("system.maintenance.mode");
        ss3.setSettingValue("false"); ss3.setDescription("Maintenance mode toggle");
        systemSettingRepository.save(ss3);

        // Case Studies
        CaseStudy cs1 = new CaseStudy();
        cs1.setTitle("Mumbai Floods 2023"); cs1.setDisasterType("Flood");
        cs1.setLocation("Mumbai, Maharashtra"); cs1.setDisasterYear(2023); cs1.setSeverity("High");
        cs1.setDescription("Severe flooding in Mumbai metropolitan region");
        cs1.setLessonsLearned("Need better drainage systems, early warning systems, and elevated shelters");
        cs1.setEstimatedDamage(50000000); cs1.setAffectedPopulation(2000000);
        cs1.setResourcesUsed(350); cs1.setResponseTimeHours(4.5); cs1.setRecoveryTimeDays(45);
        cs1.setCreatedAt(LocalDateTime.now().minusDays(1));
        caseStudyRepository.save(cs1);

        CaseStudy cs2 = new CaseStudy();
        cs2.setTitle("Guwahati Earthquake 2022"); cs2.setDisasterType("Earthquake");
        cs2.setLocation("Guwahati, Assam"); cs2.setDisasterYear(2022); cs2.setSeverity("Critical");
        cs2.setDescription("Major earthquake in Northeast India");
        cs2.setLessonsLearned("Strengthen building codes, establish rapid response teams");
        cs2.setEstimatedDamage(500000000); cs2.setAffectedPopulation(500000);
        cs2.setResourcesUsed(500); cs2.setResponseTimeHours(2.0); cs2.setRecoveryTimeDays(120);
        cs2.setCreatedAt(LocalDateTime.now().minusDays(2));
        caseStudyRepository.save(cs2);

        // Rescue Teams
        RescueTeam rt1 = new RescueTeam();
        rt1.setTeamName("Alpha Rescue Unit"); rt1.setTeamLeader("Commander Rajesh");
        rt1.setMembers("Rajesh, Amit, Suresh, Vikram, Deepak");
        rt1.setVehicles("Ambulance, Jeep, Boat"); rt1.setEquipment("First Aid Kits, Ropes, Life Jackets, Flashlights");
        rt1.setStatus("AVAILABLE"); rt1.setLocation("Mumbai"); rt1.setLatitude(19.0760); rt1.setLongitude(72.8777);
        rt1.setContactNumber("9876543001"); rt1.setMemberCount(5);
        rescueTeamRepository.save(rt1);

        RescueTeam rt2 = new RescueTeam();
        rt2.setTeamName("Bravo Disaster Response"); rt2.setTeamLeader("Commander Priya");
        rt2.setMembers("Priya, Arun, Neha, Karan, Rohit, Meera");
        rt2.setVehicles("Fire Truck, Ambulance, Command Van");
        rt2.setEquipment("Thermal Imaging, Cutters, Generators, Medical Kits");
        rt2.setStatus("STANDING_BY"); rt2.setLocation("Guwahati"); rt2.setLatitude(26.1445); rt2.setLongitude(91.7362);
        rt2.setContactNumber("9876543002"); rt2.setMemberCount(6);
        rescueTeamRepository.save(rt2);

        RescueTeam rt3 = new RescueTeam();
        rt3.setTeamName("Charlie Rescue Squad"); rt3.setTeamLeader("Commander Anand");
        rt3.setMembers("Anand, Divya, Sanjay, Pooja");
        rt3.setVehicles("Helicopter, Ambulance"); rt3.setEquipment("Airborne Rescue Kit, Medical Supplies");
        rt3.setStatus("AVAILABLE"); rt3.setLocation("Chennai"); rt3.setLatitude(13.0827); rt3.setLongitude(80.2707);
        rt3.setContactNumber("9876543003"); rt3.setMemberCount(4);
        rescueTeamRepository.save(rt3);

        // Team Members
        TeamMember tm1 = new TeamMember(); tm1.setName("Rajesh Kumar"); tm1.setRole("LEADER");
        tm1.setSpeciality("Rescue Operations"); tm1.setPhone("9876543010"); tm1.setAvailable(true); tm1.setTeam(rt1);
        teamMemberRepository.save(tm1);
        TeamMember tm2 = new TeamMember(); tm2.setName("Amit Singh"); tm2.setRole("MEDIC");
        tm2.setSpeciality("Emergency Medicine"); tm2.setPhone("9876543011"); tm2.setAvailable(true); tm2.setTeam(rt1);
        teamMemberRepository.save(tm2);
        TeamMember tm3 = new TeamMember(); tm3.setName("Priya Sharma"); tm3.setRole("LEADER");
        tm3.setSpeciality("Disaster Management"); tm3.setPhone("9876543012"); tm3.setAvailable(true); tm3.setTeam(rt2);
        teamMemberRepository.save(tm3);
    }

    /**
     * Creates a demo account unless a user with the same username already
     * exists. Ensures demo logins keep working even when the database has
     * been partially seeded or a demo account was previously removed.
     *
     * Self-healing: if an account with the same username exists but its
     * BCrypt hash does NOT match the documented demo password (e.g. a stale
     * hash left over from an older seed, or a row created by another tool),
     * the hash is re-encoded to the documented demo password so the well-known
     * credentials always work on the demo database.
     */
    private User createUserIfMissing(String username, String email, String rawPassword, Role role) {
        return userRepository.findByUsername(username)
                .map(existing -> {
                    if (!passwordEncoder.matches(rawPassword, existing.getPassword())) {
                        existing.setPassword(passwordEncoder.encode(rawPassword));
                        userRepository.save(existing);
                        log.warn("Demo account '{}' had a stale/mismatched password hash; "
                                + "reset it to the documented demo password.", username);
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    User created = userRepository.save(
                            new User(username, email, passwordEncoder.encode(rawPassword), role));
                    log.info("Created demo account '{}' with role {}", username, role);
                    return created;
                });
    }
}
