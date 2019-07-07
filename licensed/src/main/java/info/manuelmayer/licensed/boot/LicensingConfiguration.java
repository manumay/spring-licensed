package info.manuelmayer.licensed.boot;

import java.time.Clock;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;

import info.manuelmayer.licensed.actuator.LicenseHealthIndicator;
import info.manuelmayer.licensed.actuator.LicenseEndpoint;
import info.manuelmayer.licensed.aop.LicensedInterceptor;
import info.manuelmayer.licensed.aop.LicensedPointcutAdvisor;
import info.manuelmayer.licensed.service.LicenseManager;
import info.manuelmayer.licensed.service.LicensingCache;
import info.manuelmayer.licensed.service.LicensingContext;
import info.manuelmayer.licensed.service.LicensingService;
import info.manuelmayer.licensed.service.LicensingServiceImpl;
import info.manuelmayer.licensed.violation.LicenseChecker;

@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(LicensingProperties.class)
@PropertySource("classpath:config/licensing.properties")
public class LicensingConfiguration {
	
	@Bean
	public LicensedInterceptor licensedInterceptor() {
		return new LicensedInterceptor();
	}
	
	@Bean
	public LicensedPointcutAdvisor licensedPointcutAdvisor() {
		return new LicensedPointcutAdvisor();
	}
	
	@Bean
	public LicenseChecker licenseChecker(Clock clock) {
		return new LicenseChecker(clock);
	}
	
	@Bean
	public LicenseHealthIndicator licenseHealthIndicator() {
		return new LicenseHealthIndicator();
	}
	
	@Bean
	public LicensingCache licensingCache() {
		return new LicensingCache();
	}
	
	@Bean
	public LicensingContext licensingContext() {
		return new LicensingContext();
	}
	
	@Bean
	public LicenseEndpoint licensingEndpoint() {
		return new LicenseEndpoint();
	}
	
	@Bean
	public LicenseManager licenseRepository() {
		return new LicenseManager();
	}
	
	@Bean
	public LicensingService licensingService(Clock clock) {
		return new LicensingServiceImpl(clock);
	}
	
}
