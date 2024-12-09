package kalenteridatabase;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;  // Use NoOpPasswordEncoder for plain text passwords

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final MyUserDetailsService myUserDetailsService;

    public SecurityConfig(MyUserDetailsService myUserDetailsService) {
        this.myUserDetailsService = myUserDetailsService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/css/**", "/js/**").permitAll() // Allow static resources
                .antMatchers("/login").permitAll() // Allow access to the login page
                .anyRequest().authenticated() // Require authentication for other pages
                .and()
                .formLogin()
                    .defaultSuccessUrl("/calendar", true) // Redirect after successful login
                    .permitAll()
                .and()
                .logout()
                    .logoutUrl("/logout") // Logout URL
                    .logoutSuccessUrl("/login") // Redirect after logout
                    .permitAll();
    }

    //Käytetään itsetehtyä MyUserDetailsServicea tuon userDetailsServicen tilalla.
    @Override
    @Bean
    protected UserDetailsService userDetailsService() {
        System.out.println("Configuring custom UserDetailsService...");
        return myUserDetailsService;
    }

    //Jos tätä ei ole määritelty, ohjelmaan tulee autentikaatio error.
    //HUOM. että tähän pitää se kustomoitu myUserDetailsService kans survoa.
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(myUserDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    //Tehdään komponentti/bean autentikaatiotyökalusta.
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    //Käyttää tekstiä salasanoissa eikä suojaa niitä.
    //Tätä ei ollut vaatimuksissa, ja helpomman testailun vuoksi päätin käyttää tekstiä.
    //Tämä pitää vaihtaa, jos salasanat haluaa suojata esim. hashiksi.
    @Bean
    public static NoOpPasswordEncoder passwordEncoder() {
        return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
    }
}
