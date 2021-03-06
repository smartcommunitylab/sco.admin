package it.smartcommunity.admin;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import de.codecentric.boot.admin.config.EnableAdminServer;
import de.codecentric.boot.admin.notify.Notifier;
import de.codecentric.boot.admin.notify.RemindingNotifier;
import de.codecentric.boot.admin.notify.filter.FilteringNotifier;

@SpringBootApplication
@EnableAutoConfiguration
@EnableAdminServer
public class AdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdminApplication.class, args);
	}
	

	@Configuration
	public static class SecurityConfig extends WebSecurityConfigurerAdapter {
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			// Page with login form is served as /login.html and does a POST on /login
			http.formLogin().loginPage("/login.html").loginProcessingUrl("/login").permitAll();
			// The UI does a POST on /logout on logout
			http.logout().logoutUrl("/logout");
			// The ui currently doesn't support csrf
			http.csrf().disable();

			// Requests for the login page and the static assets are allowed
			http.authorizeRequests()
					.antMatchers("/login.html", "/**/*.css", "/img/**", "/third-party/**")
					.permitAll();
			// ... and any other request needs to be authorized
			http.authorizeRequests().antMatchers("/**").authenticated();

			// Enable so that the clients can authenticate via HTTP basic for registering
			http.httpBasic();
		}
	}
	
	@Configuration
	@EnableScheduling
	public class NotifierConfiguration {
	    @Autowired
	    private Notifier notifier;

	    @Bean
	    public FilteringNotifier filteringNotifier() { 
	      return new FilteringNotifier(notifier);
	    }
	    
	    @Bean
	    @Primary
	    public RemindingNotifier remindingNotifier() {
	        RemindingNotifier remindingNotifier = new RemindingNotifier(notifier);
	        remindingNotifier.setReminderPeriod(TimeUnit.MINUTES.toMillis(10)); 
	        return remindingNotifier;
	    }

	    @Scheduled(fixedRate = 60_000L) 
	    public void remind() {
	        remindingNotifier().sendReminders();
	    }
	}
}
