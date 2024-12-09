package kalenteridatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


import java.util.ArrayList;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/css/**", "/js/**").permitAll() // Permit access to static resources
                .anyRequest().authenticated() // All other requests require authentication
                .and()
                .formLogin()
                .defaultSuccessUrl("/calendar", true) // Redirect to /events after successful login
                .permitAll() // Allow access to the default login page
                .and()
                .logout()
                .logoutUrl("/logout") // URL for logout
                .logoutSuccessUrl("/login?logout") // Redirect to login page after logout
                .permitAll();
    }

    @Bean
    @Override
    protected UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findByLogin(username);
            if (user == null) {
                throw new UsernameNotFoundException("User not found");
            }
            return new org.springframework.security.core.userdetails.User(
                    user.getLogin(),
                    user.getPassword(),
                    new ArrayList<>() // No roles for now
            );
        };
    }
}



