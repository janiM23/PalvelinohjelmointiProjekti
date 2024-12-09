package kalenteridatabase;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class MyUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Loading user: " + username); // Debugging log
        User user = userRepository.findByLogin(username);
        if (user == null) {
            System.out.println("User not found: " + username); // Debugging log
            throw new UsernameNotFoundException("User not found");
        }
        System.out.println("User found: " + user.getLogin()); // Debugging log
        return new org.springframework.security.core.userdetails.User(
                user.getLogin(),
                user.getPassword(),  // Assuming the password is in plain text for now
                new ArrayList<>()
        );
    }
}
