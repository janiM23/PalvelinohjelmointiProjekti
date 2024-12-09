package kalenteridatabase;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Tällä voi laittaa käyttäjiä tietokantaan.
// Kommentit pois alla olevasta komponentista ja ohjelman suoritus, jos haluaa lisätä käyttäjiä.
//@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Poistaa vanhat käyttäjät.
        userRepository.deleteAll();

        //Hashatut salasanat.
        String hashedPassword1 = passwordEncoder.encode("password1");
        String hashedPassword2 = passwordEncoder.encode("password2");

        // Laittaa käyttäjät.
        User user1 = new User();
        user1.setLogin("testuser1");
        user1.setPassword(hashedPassword1);

        User user2 = new User();
        user2.setLogin("testuser2");
        user2.setPassword(hashedPassword2);

        userRepository.save(user1);
        userRepository.save(user2);

        System.out.println("Test users inserted!");
    }
}