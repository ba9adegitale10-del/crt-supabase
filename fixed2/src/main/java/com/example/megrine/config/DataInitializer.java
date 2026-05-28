package com.example.megrine.config;

import com.example.megrine.model.*;
import com.example.megrine.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private UserRepository userRepo;
    @Autowired private VolunteerRepository volunteerRepo;
    @Autowired private DonationRepository donationRepo;
    @Autowired private StockItemRepository stockRepo;
    @Autowired private FamilyRepository familyRepo;
    @Autowired private FamilyAidRepository aidRepo;
    @Autowired private EventRepository eventRepo;
    @Autowired private com.example.megrine.repository.TrainingResourceRepository trainingRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepo.count() == 0) {
            User admin = new User(); admin.setUsername("admin"); admin.setPassword(passwordEncoder.encode("admin123")); admin.setRole("ROLE_ADMIN"); admin.setFullName("Administrateur CRT"); admin.setEmail("admin@crt-megrine.tn"); admin.setEnabled(true); userRepo.save(admin);
            User user = new User(); user.setUsername("user"); user.setPassword(passwordEncoder.encode("user123")); user.setRole("ROLE_USER"); user.setFullName("Utilisateur CRT"); user.setEmail("user@crt-megrine.tn"); user.setEnabled(true); userRepo.save(user);
            User membre = new User(); membre.setUsername("membre"); membre.setPassword(passwordEncoder.encode("membre123")); membre.setRole("ROLE_MEMBER"); membre.setFullName("Membre Benevole"); membre.setEmail("membre@crt-megrine.tn"); membre.setEnabled(true); userRepo.save(membre);
        }
        if (volunteerRepo.count() == 0) {
            Volunteer v1 = new Volunteer(); v1.setFirstName("Ahmed"); v1.setLastName("Ben Ali"); v1.setEmail("ahmed@email.com"); v1.setPhone("+216 20 123 456"); v1.setBloodType("A+"); v1.setAddress("Megrine"); v1.setJoinDate(LocalDate.of(2022, 3, 15)); v1.setActive(true); v1.setStatus(Volunteer.VolunteerStatus.ACTIVE); v1.setTotalHours(45); v1.setBadges("BRONZE"); v1.setAvailability("WEEKEND,SOIREE"); volunteerRepo.save(v1);
            Volunteer v2 = new Volunteer(); v2.setFirstName("Fatima"); v2.setLastName("Trabelsi"); v2.setEmail("fatima@email.com"); v2.setPhone("+216 25 234 567"); v2.setBloodType("B+"); v2.setAddress("Rades"); v2.setJoinDate(LocalDate.of(2021, 6, 10)); v2.setActive(true); v2.setStatus(Volunteer.VolunteerStatus.ACTIVE); v2.setTotalHours(120); v2.setBadges("ARGENT"); v2.setAvailability("SAMEDI,DIMANCHE"); volunteerRepo.save(v2);
            Volunteer v3 = new Volunteer(); v3.setFirstName("Mohamed"); v3.setLastName("Gharbi"); v3.setEmail("mohamed@email.com"); v3.setPhone("+216 52 345 678"); v3.setBloodType("O-"); v3.setAddress("Hammam Lif"); v3.setJoinDate(LocalDate.of(2023, 1, 20)); v3.setActive(true); v3.setStatus(Volunteer.VolunteerStatus.ACTIVE); v3.setTotalHours(8); v3.setBadges(""); v3.setAvailability("MATIN"); volunteerRepo.save(v3);
        }
        if (donationRepo.count() == 0) {
            donationRepo.save(new Donation(null, "Societe Alpha", "alpha@email.com", null, new BigDecimal("5000"), Donation.DonationType.MONETARY, "Don annuel", LocalDate.now().minusDays(5), Donation.DonationStatus.RECEIVED));
            donationRepo.save(new Donation(null, "Anonyme", null, null, new BigDecimal("200"), Donation.DonationType.FOOD, "Boites alimentaires", LocalDate.now().minusDays(2), Donation.DonationStatus.RECEIVED));
        }
        if (stockRepo.count() == 0) {
            stockRepo.save(new StockItem(null, "Boites alimentaires", "FOOD", "boite", 145, 20, "Paniers Ramadan", "Entrepot A", LocalDate.now(), StockItem.StockCategory.FOOD));
            stockRepo.save(new StockItem(null, "Couvertures", "CLOTHES", "piece", 80, 10, "Couvertures hiver", "Entrepot B", LocalDate.now(), StockItem.StockCategory.CLOTHES));
            stockRepo.save(new StockItem(null, "Medicaments", "MEDICAL", "boite", 8, 15, "Paracetamol", "Armoire", LocalDate.now(), StockItem.StockCategory.MEDICAL));
        }
        if (familyRepo.count() == 0) {
            Family f1 = new Family(null, "Ben Salah Hedi", "+216 22 111 222", "Cite Megrine, Rue 14", "12345678", 5, "Veuf", "Famille prioritaire", LocalDate.of(2023, 1, 10), Family.FamilyCategory.NEEDY, Family.FamilyStatus.ACTIVE);
            Family f2 = new Family(null, "Trabelsi Fatma", "+216 25 333 444", "Rades Meliane", "87654321", 3, "Divorcee", null, LocalDate.of(2022, 6, 5), Family.FamilyCategory.ORPHAN, Family.FamilyStatus.ACTIVE);
            Family f3 = new Family(null, "Gharbi Mohamed", "+216 52 555 666", "Hammam Lif", "11223344", 7, "Marie", "Handicap moteur", LocalDate.of(2023, 9, 20), Family.FamilyCategory.DISABLED, Family.FamilyStatus.ACTIVE);
            Family f4 = new Family(null, "Mansour Aisha", "+216 58 777 888", "Megrine Centre", "44332211", 2, "Veuve", null, LocalDate.of(2021, 3, 15), Family.FamilyCategory.ELDERLY, Family.FamilyStatus.ACTIVE);
            familyRepo.save(f1); familyRepo.save(f2); familyRepo.save(f3); familyRepo.save(f4);

            aidRepo.save(new FamilyAid(null, f1, "Panier alimentaire", "Panier Ramadan complet", new BigDecimal("150"), 1, "panier", LocalDate.now().minusDays(10), "Ahmed Ben Ali", null, FamilyAid.AidType.FOOD));
            aidRepo.save(new FamilyAid(null, f1, "Aide financiere", "Loyer mois de mars", new BigDecimal("300"), null, null, LocalDate.now().minusDays(30), "Admin CRT", null, FamilyAid.AidType.MONEY));
            aidRepo.save(new FamilyAid(null, f2, "Vetements enfants", "Sac vetements hiver", null, 1, "sac", LocalDate.now().minusDays(5), "Fatima Trabelsi", null, FamilyAid.AidType.CLOTHES));
            aidRepo.save(new FamilyAid(null, f3, "Aide medicale", "Medicaments chroniques", new BigDecimal("80"), null, null, LocalDate.now().minusDays(15), "Dr. Sarra", null, FamilyAid.AidType.MEDICAL));
        }
        if (trainingRepo.count() == 0) {
            trainingRepo.save(new com.example.megrine.model.TrainingResource(null, "Guide Premiers Secours CRT", "Manuel complet des gestes de premiers secours", "https://www.croissant-rouge.tn", "PDF", com.example.megrine.model.TrainingResource.ResourceCategory.PREMIERS_SECOURS, LocalDate.now(), "admin", true));
            trainingRepo.save(new com.example.megrine.model.TrainingResource(null, "Protocole Distribution Alimentaire", "Comment organiser une distribution de paniers alimentaires", null, "LINK", com.example.megrine.model.TrainingResource.ResourceCategory.PROTOCOLE, LocalDate.now(), "admin", true));
            trainingRepo.save(new com.example.megrine.model.TrainingResource(null, "Formation Securite Terrain", "Regles de securite pour les missions humanitaires", "https://www.croissant-rouge.tn", "VIDEO", com.example.megrine.model.TrainingResource.ResourceCategory.SECURITE, LocalDate.now(), "admin", true));
        }
        if (eventRepo.count() == 0) {
            eventRepo.save(new Event(null, "Distribution Ramadan 2026", "Distribution paniers alimentaires aux familles necessiteuses", "Siege CRT Megrine", LocalDate.now().plusDays(5), "Ahmed Ben Ali", "Ahmed Ben Ali, Fatima Trabelsi", "Ben Salah Hedi, Trabelsi Fatma, Gharbi Mohamed", 50, null, Event.EventType.DISTRIBUTION, Event.EventStatus.UPCOMING));
            eventRepo.save(new Event(null, "Campagne Don du Sang", "Collecte de sang au profit des hopitaux", "Hopital Regional Ben Arous", LocalDate.now().plusDays(14), "Dr. Mohamed Gharbi", "Ahmed Ben Ali, Mohamed Gharbi", null, 200, null, Event.EventType.BLOOD_DONATION, Event.EventStatus.UPCOMING));
            eventRepo.save(new Event(null, "Formation Premiers Secours", "Formation gestes de premiers secours", "Salle CRT Megrine", LocalDate.now().minusDays(7), "Karim Bouazizi", "Karim Bouazizi, Sarra Mansour", null, 30, "28 participants effectifs", Event.EventType.TRAINING, Event.EventStatus.COMPLETED));
        }
    }
}
