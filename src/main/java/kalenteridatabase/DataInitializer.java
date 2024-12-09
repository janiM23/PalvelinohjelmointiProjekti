package kalenteridatabase;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

//Tällä voi laittaa käyttäjiä tietokantaan.
//Kommentit pois alla olevasta komponentista ja ohjelman suoritus, jos haluaa lisätä käyttäjiä.
//@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        //Poistaa vanhat käyttäjät.
        userRepository.deleteAll();

        //Laittaa käyttäjät.
        User user1 = new User();
        user1.setLogin("testuser1");
        user1.setPassword("password1"); //Plain text for NoOpPasswordEncoder

        User user2 = new User();
        user2.setLogin("testuser2");
        user2.setPassword("password2"); //Plain text for NoOpPasswordEncoder

        userRepository.save(user1);
        userRepository.save(user2);

        System.out.println("Test users inserted!");
    }
}

