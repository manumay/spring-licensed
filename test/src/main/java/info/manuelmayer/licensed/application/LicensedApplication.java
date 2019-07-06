package info.manuelmayer.licensed.application;

import java.time.Clock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@Configuration
@EnableWebSecurity
@EnableJpaRepositories
@EntityScan(basePackages = "info.manuelmayer.licensed.model")
public class LicensedApplication {
	
	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone();
	}
	
	public static void main(String[] args) {
		SpringApplication.run(LicensedApplication.class, args);
	}

	@Configuration
	public class SecurityConfig extends WebSecurityConfigurerAdapter {
		
		@Override
	    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
	        auth.inMemoryAuthentication()
			  	.withUser("user1").password(passwordEncoder().encode("password")).roles("USER").and()
			  	.withUser("user2").password(passwordEncoder().encode("password")).roles("USER").and()
			  	.withUser("user3").password(passwordEncoder().encode("password")).roles("USER");
	    }
		
		@Bean
		public SecurityContextImpl securityContext() {
			return new SecurityContextImpl();
		}
		
		@Bean
	    public PasswordEncoder passwordEncoder() {
	        return new BCryptPasswordEncoder();
	    }
		
	}
}
